from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity, verify_jwt_in_request
from models import Destination, InteractionLog
from extensions import db
from groq import Groq
import json
import os
from sqlalchemy import or_
from datetime import datetime, timedelta

destinations_bp = Blueprint('destinations', __name__)
groq_client = Groq(api_key=os.environ.get("GROQ_API_KEY"))


@destinations_bp.route('/destinations', methods=['GET'])
def get_destinations():
    all_destinations = Destination.query.all()
    return jsonify([d.to_dict() for d in all_destinations])


@destinations_bp.route('/destinations/search', methods=['GET'])
def search_destinations():
    country = request.args.get('country', '').strip()
    log_search = request.args.get('log', 'false') == 'true'
    limit = int(request.args.get('limit', 3))
    print(f">>> query reçue : '{country}'")

    if len(country) < 2:
        return jsonify([])

    # ── Étape A : Groq extrait l'intention ────────
    try:
        response = groq_client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[{
                "role": "user",
                "content": f"""Tu es un assistant pour une plateforme de voyage.
L'utilisateur a tapé : "{country}"

Extrais le pays et les mots-clés importants.
Si l'utilisateur mentionne un monument célèbre, identifie le pays correspondant.
Si c'est un continent, liste TOUS ses pays principaux.
IMPORTANT : les pays doivent être en ANGLAIS uniquement.

Réponds UNIQUEMENT en JSON valide, sans markdown, sans explication :
{{"keywords": ["mot1", "mot2"], "countries": ["pays1"]}}

Exemples :
- "pizza" → {{"keywords": ["gastronomie", "Rome"], "countries": ["Italy"]}}
- "tour eiffel" → {{"keywords": ["Paris", "monument"], "countries": ["France"]}}
- "plage soleil" → {{"keywords": ["plage", "mer"], "countries": []}}
- "morocco" → {{"keywords": ["Maroc"], "countries": ["Morocco"]}}
   Continents → pays :
- "europe" → ["France", "Germany", "Italy", "Spain", "Greece"]
- "asie" ou "asia" → ["China", "Japan", "India", "Thailand", "Vietnam"]"""
            }],
            temperature=0.3,
            max_tokens=150
        )


        text = response.choices[0].message.content.strip()

        # Nettoie les backticks si Groq en ajoute
        if text.startswith("```"):
            text = text.split("```")[1]
            if text.startswith("json"):
                text = text[4:]

        extracted = json.loads(text)
        print(f">>> Groq extracted: {extracted}")

    except Exception as e:
        print(f">>> Groq erreur, fallback SQL simple: {e}")
        extracted = {"keywords": [country], "countries": [country]}

    # ── Étape B : Construction des filtres SQL ─────────────────
    countries = extracted.get("countries", [])
    keywords  = extracted.get("keywords", [])

    # Priorité 1 : filtrer par pays si Groq en a trouvé un
    if countries:
        country_filters = []
        for c in countries:
           country_filters.append(Destination.country.ilike(f"%{c}%"))
    # ← Ajoute aussi la requête originale de l'user
        country_filters.append(Destination.country.ilike(f"%{country}%"))
    
        results = Destination.query\
          .filter(or_(*country_filters))\
          .order_by(Destination.avgRating.desc())\
          .all()

    # Priorité 2 : fallback sur mots-clés
    elif keywords:
        kw_filters = []
        for kw in keywords:
            kw_filters.append(Destination.name.ilike(f"%{kw}%"))
            kw_filters.append(Destination.country.ilike(f"%{kw}%"))
            kw_filters.append(Destination.Description.ilike(f"%{kw}%"))

        results = Destination.query\
            .filter(or_(*kw_filters))\
            .order_by(Destination.avgRating.desc())\
            .all()

    else:
        return jsonify([]), 200

    print(f">>> résultats trouvés : {len(results)}")

    # ── Étape C : Log interaction ──────────────────────────────
    if log_search:
        try:
            verify_jwt_in_request(optional=True)
            user_id = get_jwt_identity()

            if user_id and results:
                logs = [
                    InteractionLog(
                        user_id=int(user_id),
                        destination_id=d.id,
                        action='search'
                    )
                    for d in results
                ]
                db.session.bulk_save_objects(logs)
                db.session.commit()

        except Exception:
            pass

    return jsonify([d.to_dict() for d in results[:limit]]), 200


@destinations_bp.route('/destinations/<int:dest_id>', methods=['GET'])
@jwt_required(optional=True)
def get_destination_detail(dest_id):
    dest = Destination.query.get_or_404(dest_id)

    try:
        verify_jwt_in_request(optional=True)
        user_id = get_jwt_identity()
    except Exception:
        user_id = None

    if user_id:
        recent = InteractionLog.query.filter_by(
            user_id=int(user_id),
            destination_id=dest_id,
            action='view'
        ).filter(
            InteractionLog.created_at >= datetime.utcnow() - timedelta(hours=1)
        ).first()

        if not recent:
            log = InteractionLog(
                user_id=int(user_id),
                destination_id=dest_id,
                action='view'
            )
            db.session.add(log)
            db.session.commit()

    return jsonify(dest.to_dict()), 200
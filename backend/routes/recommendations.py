# routes/recommendations.py
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import Favorite, Reservation
from services.data_collector import DataCollector


from services.personnalisation import PersonalizationEngine
from extensions import db

recommendations_bp = Blueprint('recommendations', __name__)

collector = DataCollector()
def get_engine():
    """Instancie le moteur une seule fois."""
    return PersonalizationEngine()

# ─────────────────────────────────────────────────────────────
# GET /api/recommendations
# Retourne les destinations personnalisées pour le user connecté
# Appelé par la page d'accueil Angular au chargement
# ─────────────────────────────────────────────────────────────
@recommendations_bp.route('/api/recommendations', methods=['GET'])
@jwt_required(optional=True)
def get_recommendations():

    user_id = get_jwt_identity()

    # ── User non connecté → destinations populaires ───────────
    if not user_id:
        destinations = collector.get_popular_destinations(limit=12)
        return jsonify({
            "source": "popular",
            "data": [d.to_dict() for d in destinations]
        }), 200

    # ── User connecté → moteur de personnalisation ────────────
    force_refresh = request.args.get('refresh', 'false') == 'true'
    engine        = get_engine()  # instancié ici, pas au démarrage
    result = engine.get_recommendations(user_id, force_refresh=force_refresh)

    return jsonify(result), 200


@recommendations_bp.route('/api/recommendations/test', methods=['GET'])
@jwt_required()
def test_algorithm():

    user_id = get_jwt_identity()

    # Collecte des données
    user_data        = collector.get_user_data(user_id)
    all_destinations = collector.get_all_destinations(
        exclude_ids=user_data['reserved_ids']
    )

    # Algorithme seul — sans LLM
    from services.algorithm_filter import AlgorithmFilter
    algo = AlgorithmFilter()
    candidates = algo.filter_candidates(user_data, all_destinations, top_n=10)
    # print(f"DEBUG candidates: {candidates}")
    # print(f"DEBUG type: {type(candidates)}")
    # print(f"DEBUG user_data: {user_data}")
    # print(f"DEBUG all_destinations count: {len(all_destinations)}")
    
    # Retourne le résultat brut pour inspection
    return jsonify({
        "user_id":   user_id,
        "alpha":     user_data['alpha'],
        "beta":      user_data['beta'],
        "is_new_user": user_data['is_new_user'],
        "total_interactions": user_data['total_interactions'],
        "source":    "algorithm_only",
        "candidates": [
          {
        "id":         c['destination'].id,
        "name":       c['destination'].name,
        "country":    c['destination'].country,
        "type":       c['destination'].type,
        "avgRating":  c['destination'].avgRating,
        "avgCostUSD": c['destination'].avgCostUSD,
        "algo_score": c['algo_score'],
        }
   

    for c in candidates
    
] 
    }), 200


# ─────────────────────────────────────────────────────────────
# POST /api/interactions
# Enregistre une interaction utilisateur dans interaction_logs
# Appelé automatiquement depuis Angular (view, favorite, etc.)
# ─────────────────────────────────────────────────────────────
@recommendations_bp.route('/api/interactions', methods=['POST'])
@jwt_required()
def log_interaction():

    user_id = get_jwt_identity()
    data    = request.get_json()
    # Validation des données reçues
    destination_id = data.get('destination_id')
    action         = data.get('action')

    if not destination_id or not action:
        return jsonify({"error": "destination_id et action sont obligatoires"}), 400

    if action not in ['view', 'favorite', 'reservation', 'cancel']:
        return jsonify({"error": "action invalide"}), 400
    # ── Vérification pour cancel ──────────────────────────────
    if action == 'cancel':
        # Vérifier que la destination est bien réservée ou en favoris
        is_reserved = Reservation.query.filter_by(
            user_id=user_id,
            destination_id=destination_id
        ).first()

        is_favorite = Favorite.query.filter_by(
            user_id=user_id,
            destination_id=destination_id
        ).first()

        if not is_reserved and not is_favorite:
            return jsonify({
                "error": "Vous ne pouvez pas annuler une destination non réservée ou non favorite"
            }), 400
    # Enregistrement de l'interaction
    collector.log_interaction(
        user_id=user_id,
        destination_id=destination_id,
        action=action
    )

    # # Invalider le cache si interaction forte
    if action in ['favorite', 'reservation', 'cancel']:
        from services.cache_service import CacheService
        CacheService().invalidate(user_id)

    return jsonify({"message": "interaction enregistrée"}), 201


# ─────────────────────────────────────────────────────────────
# GET /api/recommendations/stats
# Statistiques des appels LLM — utile pour le rapport PFA
# ─────────────────────────────────────────────────────────────
@recommendations_bp.route('/api/recommendations/stats', methods=['GET'])
@jwt_required()
def get_stats():

    from models import LlmLog
    from sqlalchemy import func

    stats = {
        "total_calls":     LlmLog.query.count(),
        "successful_calls": LlmLog.query.filter_by(success=True).count(),
        "failed_calls":    LlmLog.query.filter_by(success=False).count(),
        "avg_response_time": db.session.query(
                                func.avg(LlmLog.response_time)
                             ).filter_by(success=True).scalar(),
        "avg_tokens_used": db.session.query(
                                func.avg(LlmLog.tokens_used)
                             ).scalar(),
    }

    return jsonify(stats), 200

# routes/users.py
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from extensions import db
from models import User, ActivityLog, Reservation, Destination
from geopy.geocoders import Nominatim
from functools import lru_cache

users_bp = Blueprint('users', __name__)
geolocator = Nominatim(user_agent="travel_app")

@lru_cache(maxsize=100)  # ← cache pour éviter de rappeler l'API à chaque fois
def get_country_coords(country: str):
    try:
        location = geolocator.geocode(country, exactly_one=True, timeout=5)
        if location:
            return [location.latitude, location.longitude]
    except Exception:
        pass
    return None

# GET /users
@users_bp.route('/users', methods=['GET'])
@jwt_required()
def get_users():
    page   = request.args.get('page', 1, type=int)
    limit  = request.args.get('limit', 10, type=int)
    search = request.args.get('search', '')
    role   = request.args.get('role', '')

    query = User.query
    if role:
        query = query.filter(User.role == role)
    if search:
        query = query.filter(
            User.name.ilike(f'%{search}%') | User.email.ilike(f'%{search}%')
        )

    total = query.count()
    users = query.order_by(User.created_at.desc()).paginate(page=page, per_page=limit, error_out=False)

    return jsonify({
        'data': [u.to_dict() for u in users.items],
        'total': total,
        'page': page,
        'pages': users.pages
    }), 200


# PUT /users/<id>/status
@users_bp.route('/users/<int:user_id>/status', methods=['PUT'])
@jwt_required()
def update_user_status(user_id):
    current_user = get_jwt_identity()
    data = request.get_json()

    user = User.query.get_or_404(user_id)
    user.status = data.get('status')
    db.session.commit()

    log = ActivityLog(user_id=int(current_user), action=f'Compte {data.get("status")}', entity_type='user', entity_id=user_id)
    db.session.add(log)
    db.session.commit()

    return jsonify(user.to_dict()), 200


# PUT /users/<id>/role
@users_bp.route('/users/<int:user_id>/role', methods=['PUT'])
@jwt_required()
def update_user_role(user_id):
    current_user = get_jwt_identity()
    data = request.get_json()

    user = User.query.get_or_404(user_id)
    user.role = data.get('role')
    db.session.commit()

    log = ActivityLog(user_id=int(current_user), action=f'Rôle changé en {data.get("role")}', entity_type='user', entity_id=user_id)
    db.session.add(log)
    db.session.commit()

    return jsonify(user.to_dict()), 200


# GET /users/me/dashboard
@users_bp.route('/users/me/dashboard', methods=['GET'])
@jwt_required()
def get_my_dashboard():
    user_id = int(get_jwt_identity())
    user = User.query.get_or_404(user_id)

    reservations = Reservation.query.filter_by(user_id=user_id)\
        .order_by(Reservation.created_at.desc()).all()

    confirmed = [r for r in reservations if r.status == 'confirmée']
    cancelled = [r for r in reservations if r.status == 'annulée']
    pending   = [r for r in reservations if r.status == 'en attente']
    total_spent = round(sum(r.total_amount for r in confirmed), 2)

    res_list = []
    visited_countries = {}  # ← nouveau
    for r in reservations:
        dest = Destination.query.get(r.destination_id)
        res_list.append({
            "id":           r.id,
            "destination":  dest.name if dest else "Inconnu",
            "country":      dest.country if dest else "",
            "check_in":     r.check_in.strftime("%d %b %Y"),
            "check_out":    r.check_out.strftime("%d %b %Y"),
            "total_amount": r.total_amount,
            "status":       r.status,
            "notes":        r.notes or ""
        })
        
         # ← Collecter pays visités (confirmées uniquement)
        if r.status == 'confirmée' and dest and dest.country:
            if dest.country not in visited_countries:
                coords = get_country_coords(dest.country)
                if coords:
                    visited_countries[dest.country] = {
                        "country":      dest.country,
                        "coords":       coords,
                        "destinations": []
                    }
            if dest.country in visited_countries:
                if dest.name not in visited_countries[dest.country]["destinations"]:
                    visited_countries[dest.country]["destinations"].append(dest.name)

    return jsonify({
        "user": {
            "name":         user.name,
            "email":        user.email,
            "segment":      user.segment or "nouveau",
            "member_since": user.created_at.strftime("%B %Y")
        },
        "stats": {
            "total":       len(reservations),
            "confirmed":   len(confirmed),
            "cancelled":   len(cancelled),
            "pending":     len(pending),
            "total_spent": total_spent
        },
        "reservations": res_list,
        "visited_countries": list(visited_countries.values())  # ← ajoutez cette ligne
        
    }), 200
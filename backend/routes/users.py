# routes/users.py
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from extensions import db
from models import User, ActivityLog

users_bp = Blueprint('users', __name__)


# GET /users — avec search, filtrer par rôle, pagination
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


# PUT /users/<id>/status — activer / désactiver / suspendre
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


# PUT /users/<id>/role — changer le rôle
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
# backend/routes/admin_users.py
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from extensions import db
from models import User, ActivityLog, Reservation
from datetime import datetime

dashboard_users_bp = Blueprint('dashboard_users', __name__,url_prefix='/admin')


@dashboard_users_bp.route('/users', methods=['GET'])
@jwt_required()
def get_all_users():
    """
    ADMIN : Liste TOUS les utilisateurs avec pagination et filtres
    """
    # Vérifier que l'utilisateur est admin
    current_user = get_jwt_identity()
    if current_user['role'] != 'admin':
        return jsonify({'error': 'Accès refusé - Admin uniquement'}), 403
    
    page = request.args.get('page', 1, type=int)
    limit = request.args.get('limit', 10, type=int)
    search = request.args.get('search', '')
    role = request.args.get('role', '')
    
    print(f"🔍 Admin recherche users: page={page}, role={role}, search={search}")
    
    query = User.query
    
    # Filtre par rôle
    if role and role.lower() != 'tous':
        query = query.filter(User.role == role.lower())
    
    # Recherche par nom ou email
    if search:
        query = query.filter(
            (User.name.ilike(f'%{search}%')) | 
            (User.email.ilike(f'%{search}%'))
        )
    
    total = query.count()
    users = query.order_by(User.created_at.desc()).paginate(
        page=page, 
        per_page=limit, 
        error_out=False
    )
    
    # Enrichir avec le nombre de réservations
    data = []
    for user in users.items:
        user_dict = user.to_dict()
        # Compter les réservations
        bookings_count = Reservation.query.filter_by(user_id=user.id).count()
        user_dict['bookings'] = bookings_count
        data.append(user_dict)
    
    print(f"✅ {len(data)} utilisateurs trouvés sur {total}")
    
    return jsonify({
        'data': data,
        'total': total,
        'page': page,
        'pages': users.pages
    }), 200


@dashboard_users_bp.route('/users/<int:user_id>', methods=['GET'])
@jwt_required()
def get_user_by_id(user_id):
    """
    ADMIN : Récupère les détails d'un utilisateur spécifique
    """
    current_user = get_jwt_identity()
    if current_user['role'] != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403
    
    user = User.query.get_or_404(user_id)
    user_dict = user.to_dict()
    
    # Ajouter les réservations
    bookings_count = Reservation.query.filter_by(user_id=user.id).count()
    user_dict['bookings'] = bookings_count
    
    return jsonify(user_dict), 200


@dashboard_users_bp.route('/users/<int:user_id>/status', methods=['PUT'])
@jwt_required()
def update_user_status(user_id):
    """
    ADMIN : Change le statut d'un utilisateur (actif, inactif, suspendu)
    """
    current_user = get_jwt_identity()
    if current_user['role'] != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403
    
    data = request.get_json()
    new_status = data.get('status')
    
    if new_status not in ['actif', 'inactif', 'suspendu']:
        return jsonify({'error': 'Statut invalide'}), 400
    
    user = User.query.get_or_404(user_id)
    old_status = user.status
    user.status = new_status
    db.session.commit()
    
    # Log l'activité
    log = ActivityLog(
        user_id=current_user['id'],
        action=f'Statut utilisateur changé: {old_status} → {new_status}',
        entity_type='user',
        entity_id=user_id,
        details=f'Utilisateur: {user.name} ({user.email})'
    )
    db.session.add(log)
    db.session.commit()
    
    print(f"✅ Statut changé pour user {user_id}: {old_status} → {new_status}")
    
    return jsonify(user.to_dict()), 200


@dashboard_users_bp.route('/users/<int:user_id>/role', methods=['PUT'])
@jwt_required()
def update_user_role(user_id):
    """
    ADMIN : Change le rôle d'un utilisateur (admin, agent, client)
    """
    current_user = get_jwt_identity()
    if current_user['role'] != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403
    
    data = request.get_json()
    new_role = data.get('role')
    
    if new_role not in ['admin', 'agent', 'client']:
        return jsonify({'error': 'Rôle invalide'}), 400
    
    user = User.query.get_or_404(user_id)
    old_role = user.role
    user.role = new_role
    db.session.commit()
    
    # Log l'activité
    log = ActivityLog(
        user_id=current_user['id'],
        action=f'Rôle utilisateur changé: {old_role} → {new_role}',
        entity_type='user',
        entity_id=user_id,
        details=f'Utilisateur: {user.name} ({user.email})'
    )
    db.session.add(log)
    db.session.commit()
    
    print(f"✅ Rôle changé pour user {user_id}: {old_role} → {new_role}")
    
    return jsonify(user.to_dict()), 200


@dashboard_users_bp.route('/users/<int:user_id>', methods=['DELETE'])
@jwt_required()
def delete_user(user_id):
    """
    ADMIN : Supprime (désactive) un utilisateur
    """
    current_user = get_jwt_identity()
    if current_user['role'] != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403
    
    user = User.query.get_or_404(user_id)
    user.status = 'inactif'
    db.session.commit()
    
    # Log l'activité
    log = ActivityLog(
        user_id=current_user['id'],
        action=f'Utilisateur désactivé: {user.name}',
        entity_type='user',
        entity_id=user_id
    )
    db.session.add(log)
    db.session.commit()
    
    print(f"✅ User {user_id} désactivé")
    
    return jsonify({'message': 'Utilisateur désactivé avec succès'}), 200


@dashboard_users_bp.route('/users/stats', methods=['GET'])
@jwt_required()
def get_users_stats():
    """
    ADMIN : Statistiques sur les utilisateurs
    """
    current_user = get_jwt_identity()
    if current_user['role'] != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403
    
    total_users = User.query.count()
    active_users = User.query.filter_by(status='actif').count()
    admins = User.query.filter_by(role='admin').count()
    agents = User.query.filter_by(role='agent').count()
    clients = User.query.filter_by(role='client').count()
    
    return jsonify({
        'total': total_users,
        'active': active_users,
        'admins': admins,
        'agents': agents,
        'clients': clients
    }), 200
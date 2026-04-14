# backend/routes/admin_users.py
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity, get_jwt
from extensions import db
from models import User, ActivityLog, Reservation, Favorite, InteractionLog, ContactMessage
from datetime import datetime, timedelta

dashboard_users_bp = Blueprint('dashboard_users', __name__,url_prefix='/admin')


def _recency_score(days: int) -> int:
    # if days is None:
    #     return 0
    # if days <= 7:
    #     return 5
    # if days <= 14:
    #     return 4
    # if days <= 30:
    #     return 3
    # if days <= 60:
    #     return 2
    # if days <= 90:
    #     return 1
    # return 0

    if days is None: return 0
    if days <= 2:   return 5  # Très récent (test facile)
    if days <= 5:   return 4
    if days <= 10:  return 3
    if days <= 20:  return 2
    if days <= 30:  return 1
    return 0


def _frequency_score(count: int) -> int:
    # if count >= 20:
    #     return 5
    # if count >= 12:
    #     return 4
    # if count >= 7:
    #     return 3
    # if count >= 3:
    #     return 2
    # if count >= 1:
    #     return 1
    # return 0

    if count >= 15:  return 5  # Seulement 5 actions pour le max
    if count >= 10:  return 4
    if count >= 5:  return 3
    if count >= 2:  return 2
    return 1


def _monetary_score(amount: float) -> int:
    # if amount >= 2000:
    #     return 5
    # if amount >= 1000:
    #     return 4
    # if amount >= 500:
    #     return 3
    # if amount >= 200:
    #     return 2
    # if amount > 0:
    #     return 1
    # return 0

    if amount >= 500: return 5 # Somme plus facile à atteindre en test
    if amount >= 200: return 4
    if amount >= 100: return 3
    if amount >= 50:  return 2
    if amount > 0:    return 1
    return 0


def _compute_rfm_score(recency_days: int, frequency_count: int, monetary_amount: float) -> float:
    recency = _recency_score(recency_days)
    frequency = _frequency_score(frequency_count)
    monetary = _monetary_score(monetary_amount)
    if recency + frequency + monetary == 0:
        return 0.0
    score = recency * 0.4 + frequency * 0.35 + monetary * 0.25
    return round(score * 20, 2)  # scale to 0-100


def _compute_churn_risk(recency_days: int, frequency_count: int, rfm_score: float) -> str:
    # if recency_days is None or recency_days > 90:
    #     return 'Critique'
    # if recency_days > 45 or rfm_score < 30:
    #     return 'Élevé'
    # if recency_days > 21 or rfm_score < 55:
    #     return 'Moyen'
    # return 'Faible'

    if recency_days is None or recency_days > 20: # Au lieu de 90
        return 'Critique'
    if recency_days > 10 or rfm_score < 30:       # Au lieu de 45
        return 'Élevé'
    if recency_days > 5 or rfm_score < 55:        # Au lieu de 21
        return 'Moyen'
    return 'Faible'

@dashboard_users_bp.route('/users', methods=['GET'])
@jwt_required()
def get_all_users():
    """
    ADMIN : Liste TOUS les utilisateurs avec pagination et filtres
    """
    claims = get_jwt()
    if claims.get('role') != 'admin':
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
    claims = get_jwt()
    if claims.get('role') != 'admin':
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
    claims = get_jwt()
    if claims.get('role') != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403
    current_user_id = int(get_jwt_identity())
    
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
        user_id=current_user_id,
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
    claims = get_jwt()
    if claims.get('role') != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403
    current_user_id = int(get_jwt_identity())
    
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
        user_id=current_user_id,
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
    claims = get_jwt()
    if claims.get('role') != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403
    current_user_id = int(get_jwt_identity())
    
    user = User.query.get_or_404(user_id)
    user.status = 'inactif'
    db.session.commit()
    
    # Log l'activité
    log = ActivityLog(
        user_id=current_user_id,
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
    claims = get_jwt()
    if claims.get('role') != 'admin':
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


######################################################################
## eni zedetha


@dashboard_users_bp.route('/users/scoring', methods=['GET'])
@jwt_required()
def get_users_scoring():
    """
    ADMIN : Scoring CRM des utilisateurs
    Amélioré avec critère de récence, RFM et churn
    """
    claims = get_jwt()
    if claims.get('role') != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403

    users = User.query.all()
    scoring_data = []
    
    # Date limite pour les actions récentes (7 jours - fenêtre de test)
    ##############################################################################
    thirty_days_ago = datetime.utcnow() - timedelta(days=7)

    for user in users:
        # ── Totaux historiques ─────────────────────────────────────
        reservations_total = Reservation.query.filter_by(user_id=user.id).count()
        favorites_total = Favorite.query.filter_by(user_id=user.id).count()
        views_total = InteractionLog.query.filter_by(user_id=user.id, action='view').count()
        contact_total = ContactMessage.query.filter_by(email=user.email).count()
        total_spent = sum(r.total_amount for r in Reservation.query.filter_by(user_id=user.id).all())

        score_historique = (
            reservations_total * 3 +
            favorites_total * 2 +
            views_total * 1 +
            contact_total * 2
        )

        # ── Totaux récents (30 derniers jours) ──────────────────────
        reservations_recent = Reservation.query.filter(
            Reservation.user_id == user.id,
            Reservation.created_at >= thirty_days_ago
        ).count()
        
        favorites_recent = Favorite.query.filter(
            Favorite.user_id == user.id,
            Favorite.created_at >= thirty_days_ago
        ).count()
        
        views_recent = InteractionLog.query.filter(
            InteractionLog.user_id == user.id,
            InteractionLog.action == 'view',
            InteractionLog.created_at >= thirty_days_ago
        ).count()
        
        contact_recent = ContactMessage.query.filter(
            ContactMessage.email == user.email,
            ContactMessage.created_at >= thirty_days_ago
        ).count()

        activity_recent = reservations_recent + favorites_recent + views_recent + contact_recent

        score_recent = (
            reservations_recent * 3 +
            favorites_recent * 2 +
            views_recent * 1 +
            contact_recent * 2
        )

        # ── Dernière activité utilisateur ─────────────────────────
        last_reservation = Reservation.query.filter_by(user_id=user.id).order_by(Reservation.created_at.desc()).first()
        last_favorite = Favorite.query.filter_by(user_id=user.id).order_by(Favorite.created_at.desc()).first()
        last_view = InteractionLog.query.filter_by(user_id=user.id, action='view').order_by(InteractionLog.created_at.desc()).first()
        last_contact = ContactMessage.query.filter_by(email=user.email).order_by(ContactMessage.created_at.desc()).first()

        last_dates = [d.created_at for d in [last_reservation, last_favorite, last_view, last_contact] if d is not None]
        last_activity_at = max(last_dates) if last_dates else None
        recency_days = (datetime.utcnow() - last_activity_at).days if last_activity_at else None

        # ── Calcul RFM / churn ──────────────────────────────────────
        rfm_score = _compute_rfm_score(recency_days, activity_recent, total_spent)
        churn_risk = _compute_churn_risk(recency_days, activity_recent, rfm_score)

        # ── Segment basé sur le score récent + RFM si possible ─────
        # if score_recent >= 20 or rfm_score >= 70:
        #     segment = 'VIP'
        # elif score_recent >= 10 or rfm_score >= 45:
        #     segment = 'Régulier'
        # elif reservations_total > 0 or favorites_total > 0:
        #     segment = 'Nouveau'
        # else:
        #     segment = 'Inactif'

        # ── Segment basé sur des critères ÉQUILIBRÉS (7 jours) ─────
        if rfm_score >= 65 and score_recent >= 12:
            segment = 'VIP'  # Client très actif et engagé
        elif rfm_score >= 45 and score_recent >= 6:
            segment = 'Régulier'  # Client fidèle avec activité modérée
        elif reservations_total > 0 or activity_recent > 0:
            segment = 'Nouveau'  # Au moins une action
        else:
            segment = 'Inactif'  # Aucune activité

        # ── Sauvegarder le segment en base de données ─────
        user.segment = segment
        db.session.commit()

        was_vip = score_historique >= 20

        scoring_data.append({
            'id': user.id,
            'name': user.name,
            'email': user.email,
            'role': user.role,
            'status': user.status,
            'last_activity_at': last_activity_at.isoformat() if last_activity_at else None,
            'recency_days': recency_days,
            'reservations_total': reservations_total,
            'favorites_total': favorites_total,
            'views_total': views_total,
            'contact_total': contact_total,
            'total_spent': round(total_spent, 2),
            'score_historique': min(score_historique, 100),
            'reservations_recent': reservations_recent,
            'favorites_recent': favorites_recent,
            'views_recent': views_recent,
            'contact_recent': contact_recent,
            'score_recent': min(score_recent, 100),
            'activity_recent': activity_recent,
            'rfm_score': min(rfm_score, 100),
            'churn_risk': churn_risk,
            'segment': segment,
            'was_vip': was_vip,
            'created_at': user.created_at.strftime('%Y-%m-%d') if user.created_at else None
        })

    print(f"✅ Scoring calculé pour {len(scoring_data)} utilisateurs")

    return jsonify({
        'data': scoring_data,
        'total': len(scoring_data)
    }), 200




# ══════════════════════════════════════════════════════════════
# À COLLER À LA FIN de ton admin_users.py existant
# ══════════════════════════════════════════════════════════════

@dashboard_users_bp.route('/users/<int:user_id>/crm-profile', methods=['GET'])
@jwt_required()
def get_user_crm_profile(user_id):
    """
    ADMIN — Smart CRM : profil complet d'un client.
    Appelé quand l'admin clique sur un utilisateur dans la page Users.
    """
    from models import Conversation, Message, ConversationSummary

    claims = get_jwt()
    if claims.get('role') != 'admin':
        return jsonify({'error': 'Accès refusé'}), 403

    user = User.query.get_or_404(user_id)

    # ── Calcul RFM rapide ─────────────────────────────────────
    thirty_days_ago    = datetime.utcnow() - timedelta(days=7)
    reservations_total = Reservation.query.filter_by(user_id=user.id).count()
    favorites_total    = Favorite.query.filter_by(user_id=user.id).count()
    contact_total      = ContactMessage.query.filter_by(email=user.email).count()
    total_spent        = sum(
        r.total_amount
        for r in Reservation.query.filter_by(user_id=user.id).all()
    )

    activity_recent = (
        Reservation.query.filter(
            Reservation.user_id == user.id,
            Reservation.created_at >= thirty_days_ago
        ).count()
        + Favorite.query.filter(
            Favorite.user_id == user.id,
            Favorite.created_at >= thirty_days_ago
        ).count()
        + InteractionLog.query.filter(
            InteractionLog.user_id == user.id,
            InteractionLog.action == 'view',
            InteractionLog.created_at >= thirty_days_ago
        ).count()
    )

    # Dernière activité
    last_dates = []
    for obj in [
        Reservation.query.filter_by(user_id=user.id).order_by(Reservation.created_at.desc()).first(),
        Favorite.query.filter_by(user_id=user.id).order_by(Favorite.created_at.desc()).first(),
        ContactMessage.query.filter_by(email=user.email).order_by(ContactMessage.created_at.desc()).first(),
    ]:
        if obj:
            last_dates.append(obj.created_at)

    last_activity_at = max(last_dates) if last_dates else None
    recency_days     = (datetime.utcnow() - last_activity_at).days if last_activity_at else None
    rfm_score        = _compute_rfm_score(recency_days or 999, activity_recent, total_spent)
    churn_risk       = _compute_churn_risk(recency_days or 999, activity_recent, rfm_score)

    # Segment (utilise celui déjà en DB sinon recalcule)
    segment = getattr(user, 'segment', None)
    if not segment:
        if rfm_score >= 65 and activity_recent >= 12:       segment = 'VIP'
        elif rfm_score >= 45 and activity_recent >= 6:      segment = 'Régulier'
        elif reservations_total > 0 or activity_recent > 0: segment = 'Nouveau'
        else:                                                segment = 'Inactif'

    # ── Conversations chat + résumés ──────────────────────────
    conversations = Conversation.query.filter_by(user_id=user.id).order_by(
        Conversation.updated_at.desc()
    ).all()

    chat_summaries = []
    for conv in conversations:
        summary   = ConversationSummary.query.filter_by(conversation_id=conv.id).first()
        msg_count = Message.query.filter_by(conversation_id=conv.id).count()
        chat_summaries.append({
            'conversation_id': conv.id,
            'title':           conv.title,
            'topic':           conv.topic,
            'status':          conv.status,
            'message_count':   msg_count,
            'updated_at':      conv.updated_at.strftime('%d/%m/%Y %H:%M') if conv.updated_at else '',
            'has_summary':     summary is not None,
            'summary':         summary.summary    if summary else None,
            'key_points':      summary.key_points if summary else [],
        })

    # ── Derniers messages de contact ─────────────────────────
    contact_messages = ContactMessage.query.filter_by(email=user.email).order_by(
        ContactMessage.created_at.desc()
    ).limit(5).all()

    contact_history = [{
        'id':         m.id,
        'subject':    m.subject,
        'category':   m.category,
        'sentiment':  m.sentiment,
        'ai_summary': m.ai_summary,
        'created_at': m.created_at.strftime('%d/%m/%Y %H:%M') if m.created_at else '',
    } for m in contact_messages]

    # ── Réservations récentes ────────────────────────────────
    reservations = Reservation.query.filter_by(user_id=user.id).order_by(
        Reservation.created_at.desc()
    ).limit(5).all()

    reservations_list = [{
        'id':          r.id,
        'destination': r.destination.name    if r.destination else 'N/A',
        'country':     r.destination.country if r.destination else '',
        'status':      r.status,
        'amount':      r.total_amount,
        'check_in':    r.check_in.strftime('%d/%m/%Y')  if r.check_in  else '',
        'check_out':   r.check_out.strftime('%d/%m/%Y') if r.check_out else '',
    } for r in reservations]

    return jsonify({
        'success': True,
        'client': {
            'id':            user.id,
            'name':          user.name,
            'email':         user.email,
            'status':        user.status,
            'created_at':    user.created_at.strftime('%d/%m/%Y') if user.created_at else '',
            'segment':       segment,
            'churn_risk':    churn_risk,
            'rfm_score':     round(rfm_score, 1),
            'recency_days':  recency_days,
            'last_activity': f"il y a {recency_days} jour(s)" if recency_days is not None else "Aucune activité",
        },
        'stats': {
            'reservations_total':  reservations_total,
            'total_spent':         round(total_spent, 2),
            'favorites_total':     favorites_total,
            'contact_total':       contact_total,
            'conversations_total': len(conversations),
        },
        'chat_summaries':  chat_summaries,
        'contact_history': contact_history,
        'reservations':    reservations_list,
    }), 200
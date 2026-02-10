# calcule les KPIs directement depuis la DB.
############################################

# routes/dashboard.py
from flask import Blueprint, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from datetime import datetime, timedelta
from sqlalchemy import func, extract
from extensions import db
from models import User, Reservation, Destination, ActivityLog

dashboard_bp = Blueprint('dashboard', __name__)


# ─────────────────────────────────────────
# GET /dashboard/kpis
# Retourne tous les indicateurs clés
# ─────────────────────────────────────────
# @jwt_required()
@dashboard_bp.route('/dashboard/kpis', methods=['GET'])
def get_kpis():
    try:
        # Total réservations
        total_reservations = Reservation.query.count()

        # Revenus totaux (On se base sur toutes les réservations car payment_status n'existe pas encore)
        # Note : Si tu ajoutes payment_status plus tard, ajoute .filter_by(payment_status='payé')
        total_revenue = db.session.query(func.sum(Reservation.total_amount)).scalar() or 0

        # Clients / Users actifs
        active_users = User.query.filter(User.status == 'actif').count()

        # Annulations ce mois
        # now = datetime.utcnow()
        # cancellations_month = Reservation.query.filter(
        #     Reservation.status == 'annulée',
        #     extract('month', Reservation.created_at) == now.month,
        #     extract('year', Reservation.created_at) == now.year
        # ).count()

        # Taux d'annulation
        # reservations_month = Reservation.query.filter(
        #     extract('month', Reservation.created_at) == now.month,
        #     extract('year', Reservation.created_at) == now.year
        # ).count()
        # cancellation_rate = round((cancellations_month / reservations_month * 100), 1) if reservations_month > 0 else 0

        # Clients fidèles (3+ réservations)
        loyal_clients = db.session.query(Reservation.user_id).group_by(
            Reservation.user_id
        ).having(func.count(Reservation.id) >= 3).count()

        # Réservations en attente
        pending = Reservation.query.filter_by(status='en attente').count()

        return jsonify({
            'totalReservations': total_reservations,
            'totalRevenue': float(total_revenue),
            'activeClients': active_users,
            # 'cancellationRate': cancellation_rate,
            'loyalClients': loyal_clients,
            'pendingReservations': pending,
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    

# ─────────────────────────────────────────
# GET /dashboard/revenue-monthly
# Revenus par mois pour le graphique
# ─────────────────────────────────────────
@dashboard_bp.route('/dashboard/revenue-monthly', methods=['GET'])
# @jwt_required()
def get_monthly_revenue():
    # Agrégation des revenus par mois
    results = db.session.query(
        extract('month', Reservation.created_at).label('month'),
        func.sum(Reservation.total_amount).label('revenue'),
        func.count(Reservation.id).label('bookings')
    ).filter(
        extract('year', Reservation.created_at) == datetime.utcnow().year
    ).group_by(
        extract('month', Reservation.created_at)
    ).order_by('month').all()

    months_names = ['Jan','Fév','Mar','Avr','Mai','Jun','Jul','Aoû','Sep','Oct','Nov','Déc']
    data = []
    
    # On initialise les 12 mois à 0 pour le graphique
    for i in range(1, 13):
        month_data = next((r for r in results if int(r.month) == i), None)
        data.append({
            'month': months_names[i-1],
            'revenue': float(month_data.revenue) if month_data else 0,
            'bookings': int(month_data.bookings) if month_data else 0
        })

    return jsonify(data), 200


# ─────────────────────────────────────────
# GET /dashboard/destinations-stats
# Répartition des réservations par destination
# ─────────────────────────────────────────
@dashboard_bp.route('/dashboard/destinations-stats', methods=['GET'])
# @jwt_required()
def get_destinations_stats():
    results = db.session.query(
        Destination.name.label('destination'),
        func.count(Reservation.id).label('total')
    ).join(Reservation, Reservation.destination_id == Destination.id
    ).group_by(Destination.name
    ).order_by(func.count(Reservation.id).desc()
    ).limit(6).all()

    # Calcule le pourcentage
    total = sum(r.total for r in results)
    data = []
    for row in results:
        data.append({
            'name': row.destination,
            'value': round((row.total / total * 100), 1) if total > 0 else 0
        })

    return jsonify(data), 200


# ─────────────────────────────────────────
# GET /dashboard/activity-logs
# Dernières activités du système
# ─────────────────────────────────────────
@dashboard_bp.route('/dashboard/activity-logs', methods=['GET'])
# @jwt_required()
def get_activity_logs():
    logs = ActivityLog.query.order_by(ActivityLog.created_at.desc()).limit(20).all()

    return jsonify([log.to_dict() for log in logs]), 200


# ─────────────────────────────────────────
# GET /dashboard/recent-reservations
# Dernières réservations pour le tableau
# ─────────────────────────────────────────
@dashboard_bp.route('/dashboard/recent-reservations', methods=['GET'])
# @jwt_required()
def get_recent_reservations():
    reservations = Reservation.query.order_by(Reservation.created_at.desc()).limit(10).all()

    return jsonify([r.to_dict() for r in reservations]), 200
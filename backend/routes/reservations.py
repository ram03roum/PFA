# routes/reservations.py
from flask import Blueprint, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from extensions import db
from models import Reservation, User, Destination , ActivityLog
from datetime import datetime

reservations_bp = Blueprint('reservations', __name__)

#################################
######### Vue Admin ############

# GET /reservations — avec search, filter, pagination
@reservations_bp.route('/reservations', methods=['GET'])
# @jwt_required()
def get_reservations():
    page = request.args.get('page', 1, type=int)
    limit = request.args.get('limit', 10, type=int)
    search = request.args.get('search', '')
    status = request.args.get('status', '')

    query = Reservation.query

    # Filtrer par statut
    if status:
        query = query.filter(Reservation.status == status)

    # Recherche par nom client ou destination
    query = query.join(Reservation.user).join(Reservation.destination).filter(
            (User.name.ilike(f'%{search}%')) | (Destination.name.ilike(f'%{search}%'))
        )
    
    total = query.count()
    reservations = query.order_by(Reservation.created_at.desc()).paginate(page=page, per_page=limit, error_out=False)

    return jsonify({
        'data' :[
            {
            'id': r.id,
            'user': r.user.to_dict(),   
            'destination': r.destination.to_dict(),
            'check_in': r.check_in.isoformat(),
            'check_out': r.check_out.isoformat(),
            'total_amount': r.total_amount,
            'status': r.status,
            # Champs simplifiés pour le frontend (affichage tableau)
            'client': r.user.name,
            'destination_name': r.destination.name,
            'dates': f"{r.check_in.strftime('%d/%m')} - {r.check_out.strftime('%d/%m')}",
            'amount': r.total_amount,
            } for r in reservations.items],
        'total': total,
        'page': page,
        'pages': reservations.pages
    }), 200


# POST /reservations — créer une réservation
@reservations_bp.route('/reservations', methods=['POST'])
@jwt_required()
def create_reservation():
    data = request.get_json()
    current_user_id = int(get_jwt_identity())

    reservation = Reservation(
        user_id=current_user_id,
        destination_id=data['destination_id'],
        check_in=datetime.strptime(data['check_in'], '%Y-%m-%d').date(),
        check_out=datetime.strptime(data['check_out'], '%Y-%m-%d').date(),
        total_amount=data['total_amount'],
        notes=data.get('notes', '')
    )
    db.session.add(reservation)
    db.session.commit()

    # Log
    log = ActivityLog(user_id=current_user_id, action='Nouvelle réservation créée', entity_type='reservation', entity_id=reservation.id)
    db.session.add(log)
    db.session.commit()

    return jsonify(reservation.to_dict()), 201


# PUT /reservations/<id>/status — confirmer / refuser
@reservations_bp.route('/reservations/<int:res_id>/status', methods=['PUT'])
@jwt_required()
def update_reservation_status(res_id):
    current_user = get_jwt_identity()
    data = request.get_json()
    new_status = data.get('status')

    reservation = Reservation.query.get_or_404(res_id)
    reservation.status = new_status
    db.session.commit()

    # Log
    log = ActivityLog(user_id=int(current_user), action=f'Réservation {new_status}', entity_type='reservation', entity_id=res_id)
    db.session.add(log)
    db.session.commit()

    return jsonify(reservation.to_dict()), 200


# DELETE /reservations/<id> — annuler
@reservations_bp.route('/reservations/<int:res_id>/cancel', methods=['DELETE'])
@jwt_required()
def cancel_reservation(res_id):
    current_user = get_jwt_identity()
    reservation = Reservation.query.get_or_404(res_id)
    reservation.status = 'annulée'
    # db.session.delete(reservation)
    db.session.commit()

    log = ActivityLog(user_id=int(current_user), action='Réservation annulée', entity_type='reservation', entity_id=res_id)
    db.session.add(log)
    db.session.commit()

    return jsonify({'message': 'Réservation annulée'}), 200
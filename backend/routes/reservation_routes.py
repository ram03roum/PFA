from flask import Blueprint, request, jsonify
from models import ActivityLog, Reservation, Destination, User
from datetime import datetime
from extensions import db
from flask_jwt_extended import jwt_required, get_jwt_identity

client_reservation_bp = Blueprint('client_reservations', __name__, url_prefix='/client')

#################################
######### Vue Client ############
@client_reservation_bp.route('/reservations', methods=['POST'])
@jwt_required()
def create_reservation():
    """
    POST /client/reservations
    Créer une réservation (client)
    """
    try:
        data = request.get_json()
        current_user_id = get_jwt_identity()
        
        # Validation
        if not data.get('destination_id'):
            return jsonify({'error': 'destination_id est requis'}), 400
        if not data.get('check_in'):
            return jsonify({'error': 'check_in est requis'}), 400
        if not data.get('check_out'):
            return jsonify({'error': 'check_out est requis'}), 400
        
        # Vérifier destination
        destination = Destination.query.get(data['destination_id'])
        user = User.query.get(current_user_id)

        log = ActivityLog(user_id=int(current_user_id), action='Création d\'une réservation', entity_type='reservation', entity_id=None)
        db.session.add(log)
        db.session.commit()

        if not destination:
            return jsonify({'error': 'Destination non trouvée'}), 404
        
        # Convertir dates
        check_in = datetime.strptime(data['check_in'], '%Y-%m-%d').date()
        check_out = datetime.strptime(data['check_out'], '%Y-%m-%d').date()
        
        if check_out <= check_in:
            return jsonify({'error': 'La date de départ doit être après la date d\'arrivée'}), 400
        
        # Calculer prix
        nights = (check_out - check_in).days
        price_per_night = getattr(destination, 'avgCostUSD', 100.0)
        total_amount = price_per_night * nights
        
        # Créer réservation
        reservation = Reservation(
            user_id=current_user_id,
            destination_id=data['destination_id'],
            check_in=check_in,
            check_out=check_out,
            total_amount=total_amount,
            notes=data.get('notes', ''),
            status='en attente'
        )
        
        db.session.add(reservation)
        db.session.commit()
        
        return jsonify({
            'message': 'Réservation créée avec succès',
            'reservation': reservation.to_dict()
        }), 201
        
    except ValueError:
        return jsonify({'error': 'Format de date invalide'}), 400
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500


@client_reservation_bp.route('/reservations', methods=['GET'])
@jwt_required()
def get_my_reservations():
    """
    GET /client/reservations
    Mes réservations (client)
    """
    try:
        current_user_id = get_jwt_identity()
        reservations = Reservation.query.filter_by(
            user_id=current_user_id
        ).order_by(Reservation.created_at.desc()).all()
        
        return jsonify([r.to_dict() for r in reservations]), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@client_reservation_bp.route('/reservations/<int:id>', methods=['GET'])
@jwt_required()
def get_reservation_detail(id):
    """
    GET /client/reservations/:id
    Détail d'une réservation (client)
    """
    try:
        current_user_id = get_jwt_identity()
        reservation = Reservation.query.filter_by(
            id=id, 
            user_id=current_user_id
        ).first_or_404()
        
        return jsonify(reservation.to_dict()), 200
    except Exception as e:
        return jsonify({'error': 'Réservation non trouvée'}), 404


@client_reservation_bp.route('/reservations/<int:id>/cancel', methods=['PUT'])
@jwt_required()
def cancel_my_reservation(id):
    """
    PUT /client/reservations/:id/cancel
    Annuler ma réservation (client)
    """
    try:
        current_user_id = get_jwt_identity()
        reservation = Reservation.query.filter_by(
            id=id, 
            user_id=current_user_id
        ).first_or_404()
        
        if reservation.status in ['annulée', 'payée']:
            return jsonify({'error': 'Impossible d\'annuler cette réservation'}), 400
        
        reservation.status = 'annulée'
        db.session.commit()
        
        return jsonify({
            'message': 'Réservation annulée avec succès',
            'reservation': reservation.to_dict()
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500


@client_reservation_bp.route('/calculate-price', methods=['POST'])
def calculate_price():
    """
    POST /client/calculate-price
    Calculer le prix d'une réservation
    """
    try:
        data = request.get_json()
        
        destination = Destination.query.get_or_404(data['destination_id'])
        check_in = datetime.strptime(data['check_in'], '%Y-%m-%d').date()
        check_out = datetime.strptime(data['check_out'], '%Y-%m-%d').date()
        
        nights = (check_out - check_in).days
        price_per_night = getattr(destination, 'price_per_night', 100.0)
        total_amount = price_per_night * nights
        
        return jsonify({
            'nights': nights,
            'price_per_night': price_per_night,
            'total_amount': total_amount
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500
    
@client_reservation_bp.route('/logs', methods=['POST'])
@jwt_required() 
def add_activity_log():
    """
    POST /client/logs
    Ajouter une entrée dans le journal d'activité
    """
    try:
        data = request.get_json()
        current_user_id = get_jwt_identity()
        
        log = ActivityLog(
            user_id=current_user_id,
            action=data['action'],
            entity_type=data['entity_type'],
            entity_id=data['entity_id'],
            details=data.get('details', '')
        )
        
        db.session.add(log)
        db.session.commit()
        
        return jsonify({'message': 'Log ajouté avec succès'}), 201
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

from flask import Blueprint, jsonify
from models import Destination

# Créer le Blueprint
# 'auth' = nom du blueprint (pour le debug)
# __name__ = dit à Flask où se trouve le fichier

destinations_bp = Blueprint('destinations', __name__)

# Maintenant, au lieu de @app.route, on fait @destinations_bp.route

@destinations_bp.route('/destinations', methods=['GET'])
def get_destinations():
    all_destinations = Destination.query.all()
    return jsonify([d.to_dict() for d in all_destinations])

@destinations_bp.route('/destinations/<int:dest_id>', methods=['GET'])
def get_destination_detail(dest_id):
    dest = Destination.query.get_or_404(dest_id)
    return jsonify([dest.to_dict()])
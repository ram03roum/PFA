
from flask import Blueprint, jsonify, request
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

@destinations_bp.route('/destinations/search', methods=['GET'])
def search_destinations():
    query_text = request.args.get('query')
    name = request.args.get('name')
    country = request.args.get('country')
    continent = request.args.get('continent')
    type_ = request.args.get('type')

    query = Destination.query

    if query_text:
        q = f"%{query_text}%"
        query = query.filter(
            (Destination.name.ilike(q)) | (Destination.country.ilike(q))
        )
    if name:
        query = query.filter(Destination.name.ilike(f'%{name}%'))
    if country:
        query = query.filter(Destination.country.ilike(f'%{country}%'))
    if continent:
        query = query.filter(Destination.continent.ilike(f'%{continent}%'))
    if type_:
        query = query.filter(Destination.type.ilike(f'%{type_}%'))

    results = query.all()
    return jsonify([d.to_dict() for d in results])
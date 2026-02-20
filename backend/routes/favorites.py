from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from models import Favorite
from extensions import db

favorites_bp = Blueprint("favorites", __name__)


@favorites_bp.route("/favorites", methods=["GET"])
@jwt_required()
def get_favorites():
    user_id = get_jwt_identity()
    # Récupérer tous les favoris de l'utilisateur
    favorites = Favorite.query.filter_by(user_id=user_id).all()
    # On renvoie juste la liste des IDs de destinations, ex: [5, 11, 20]
    fav_ids = [f.destination_id for f in favorites]
    return jsonify(fav_ids), 200


# Ajouter un favori
@favorites_bp.route("/favorites", methods=["POST"])
@jwt_required()
def add_favorite():
    user_id = get_jwt_identity()
    destination_id = request.json.get("destination_id")

    if not destination_id:
        return jsonify({"error": "destination_id required"}), 400

    # Vérifier existence (évite erreur SQL)
    exists = Favorite.query.filter_by(
        user_id=user_id, destination_id=destination_id
    ).first()

    if exists:
        return jsonify({"message": "Already in favorites"}), 200

    favorite = Favorite(user_id=user_id, destination_id=destination_id)

    db.session.add(favorite)
    db.session.commit()

    return jsonify({"message": "Added to favorites"}), 201


# Dans favorites.py
@favorites_bp.route("/favorites/<int:destination_id>", methods=["DELETE"])
@jwt_required()
def clear_favorites(destination_id):
    user_id = get_jwt_identity()  # récupère l'utilisateur connecté
    #  """Supprimer un favori"""
    favorite = Favorite.query.filter_by(
        user_id=user_id, destination_id=destination_id
    ).first()

    if not favorite:
        return jsonify({"message": "Favori non trouvé"}), 404

    db.session.delete(favorite)
    db.session.commit()

    return jsonify({"message": "Favori supprimé"}), 200

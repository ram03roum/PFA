from flask import Blueprint, request, jsonify
from database import get_db_connection
from flask_jwt_extended import jwt_required, get_jwt_identity


favorites_bp = Blueprint('favorites', __name__)


@favorites_bp.route('/favorites', methods=['GET'])
@jwt_required()
def get_favorites():
    user_id = get_jwt_identity()
    
    # Connexion à ta base de données (ton code backend)
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    
   # On récupère les favoris qui correspondent à cet ID unique
    query = "SELECT destination_id FROM favorites WHERE user_id = %s"
    cursor.execute(query, (user_id,))
    rows = cursor.fetchall()
    
    cursor.close()
    conn.close()

    # On renvoie juste la liste des IDs de destinations, ex: [5, 11, 20]
    fav_ids = [r['destination_id'] for r in rows]
    return jsonify(fav_ids), 200

# Ajouter un favori
@favorites_bp.route('/favorites', methods=['POST'])
@jwt_required()  # <-- AJOUTEZ CECI
def add_favorite():
    data = request.json
    user_id = get_jwt_identity()
    destination_id = data.get('destination_id')

    # 1. On récupère la connexion
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # 2. On prépare et on exécute la requête
        query = "INSERT INTO favorites (user_id, destination_id) VALUES (%s, %s)"
        cursor.execute(query, (user_id, destination_id))

        # 3. TRÈS IMPORTANT : On valide l'écriture dans MySQL
        conn.commit() 
        
        print(f"Favori ajouté : User {user_id}, Destination {destination_id}")
        return jsonify({"status": "success", "message": "Favori enregistré"}), 201

    except Exception as e:
        # En cas de problème (ex: doublon), on annule
        conn.rollback()
        print(f"Erreur SQL : {e}")
        return jsonify({"status": "error", "message": str(e)}), 500
    finally:
        # 4. On ferme toujours la connexion pour libérer les ressources AlwaysData
        cursor.close()
        conn.close()
        
        
        
        # Dans favorites.py
@favorites_bp.route('/favorites/<int:destination_id>', methods=['DELETE', 'OPTIONS'])
def remove_favorite(destination_id):
    # 1. Gérer explicitement OPTIONS pour débloquer Angular (CORS)
    if request.method == 'OPTIONS':
        return jsonify({"message": "CORS preflight OK"}), 200

    # 2. Vérifier manuellement le token pour éviter le crash RuntimeError
    from flask_jwt_extended import verify_jwt_in_request
    try:
        verify_jwt_in_request()
        user_id = get_jwt_identity()
    except Exception as e:
        return jsonify({"status": "error", "message": "Authentification requise"}), 401

    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        # 3. La requête de suppression
        query = "DELETE FROM favorites WHERE user_id = %s AND destination_id = %s"
        cursor.execute(query, (user_id, destination_id))
        conn.commit()

        # 4. Vérification cruciale : est-ce qu'une ligne a vraiment été effacée ?
        if cursor.rowcount == 0:
            return jsonify({
                "status": "warning", 
                "message": f"Aucun favori trouvé pour l'user {user_id} et destination {destination_id}"
            }), 404

        return jsonify({"status": "success", "message": "Favori supprimé"}), 200
    except Exception as e:
        conn.rollback()
        return jsonify({"status": "error", "message": str(e)}), 500
    finally:
        cursor.close()
        conn.close()
        
        
@jwt_required()
def clear_favorites():
    user_id = get_jwt_identity()  # récupère l'utilisateur connecté
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        query = "DELETE FROM favorites WHERE user_id = %s"
        cursor.execute(query, (user_id,))
        conn.commit()
        return jsonify({"status": "success", "message": "Tous les favoris ont été supprimés"}), 200
    except Exception as e:
        conn.rollback()
        return jsonify({"status": "error", "message": str(e)}), 500
    finally:
        cursor.close()
        conn.close()
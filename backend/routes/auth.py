from flask import Blueprint, request, jsonify
from werkzeug.security import generate_password_hash, check_password_hash
from database import get_db_connection  # <-- import propre
# --- AJOUTE CET IMPORT ---
from flask_jwt_extended import create_access_token
# Un Blueprint = un module pour regrouper des routes Flask
auth_bp = Blueprint('auth', __name__)

# --- ROUTE INSCRIPTION ---
@auth_bp.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    name = data.get('name')
    email = data.get('email')
    password = data.get('password')

    if not name or not email or not password:
        return jsonify({'error': 'Tous les champs sont requis'}), 400

    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    # Vérifier si l'utilisateur existe déjà
    cursor.execute("SELECT * FROM users WHERE email=%s", (email,))
    existing_user = cursor.fetchone()
    if existing_user:
        conn.close()
        return jsonify({"message": "Email déjà utilisé"}), 400

    # Hasher le mot de passe
    hashed_pw = generate_password_hash(password)

    # Insérer l'utilisateur
    try:
        cursor.execute(
            "INSERT INTO users (name, email, password) VALUES (%s, %s, %s)",
            (name, email, hashed_pw)
        )
        conn.commit()
        user_id = cursor.lastrowid
    except Exception as e:
        conn.close()
        return jsonify({"message": str(e)}), 500

    conn.close()

    return jsonify({
        "message": "Inscription réussie",
        "user": {"id": user_id, "name": name, "email": email}
    }), 201
    
    

# --- ROUTE LOGIN ---
@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    email = data.get('email')
    password = data.get('password')

    if not email or not password:
        return jsonify({'error': 'Email et mot de passe requis'}), 400

    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT * FROM users WHERE email=%s", (email,))
    user = cursor.fetchone()
    conn.close()

    if not user or not check_password_hash(user['password'], password):
        return jsonify({'error': 'Email ou mot de passe incorrect'}), 401
   
    # On crée le token en utilisant l'ID de l'utilisateur (converti en string)
    # récupérer token
    access_token = create_access_token(identity=str(user['id']))

    return jsonify({
        "message": f"Bienvenue {user['name']} !",
        "access_token": access_token, # On renvoie le token au frontend
        "user": {"id": user['id'], "name": user['name'], "email": user['email']}
    }), 200
# routes/auth.py
from flask import Blueprint, jsonify, request
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity , get_jwt
from werkzeug.security import generate_password_hash
from datetime import datetime
from extensions import db
from models import User, ActivityLog

auth_bp = Blueprint('auth', __name__)


# POST /register
@auth_bp.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    name = data.get('name')
    email = data.get('email')
    password = data.get('password')
    role = data.get('role', 'client')  # Par défaut client
    phone = data.get('phone')
    if not name or not email or not password:
        return jsonify({'error': 'Tous les champs sont requis'}), 400

    existing = User.query.filter_by(email=email).first()
    if existing:
        return jsonify({'error': 'Email déjà utilisé'}), 400

    hashed = generate_password_hash(password)
    user = User(email=email, password=hashed, name=name, role=role, status='actif', phone=phone)
    db.session.add(user)
    db.session.commit()

    return jsonify({'message': 'Inscription réussie', 'user': {'id': user.id, 'name': user.name, 'role': user.role}}), 201

# POST /login
@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    email = data.get('email')
    password = data.get('password')

    if not email or not password:
        return jsonify({'error': 'Email et mot de passe requis'}), 400
    # Chercher l'utilisateur
    user = User.query.filter_by(email=email).first()

    if user and user.check_password(password):
        # Génère le JWT
        token = create_access_token(identity=str(user.id), additional_claims={'role': user.role}, expires_delta=None)

        # Met à jour last_login
        user.last_login = datetime.utcnow()
        db.session.commit()

        # Log l'activité
        log = ActivityLog(user_id=user.id, action='Connexion réussie', entity_type='user', entity_id=user.id)
        db.session.add(log)
        db.session.commit()

        return jsonify({
            'message': 'Login successful',
            'token': token,
            'user': {'id': user.id, 'name': user.name, 'email': user.email, 'role': user.role}
        }), 200

    return jsonify({'error': 'Email ou mot de passe incorrect'}), 401


# GET /me — récupère l'utilisateur connecté
@auth_bp.route('/me', methods=['GET'])
@jwt_required() # ← Cette route nécessite un token valide
def get_me():
        # Récupérer l'identité depuis le token
    user_id = int(get_jwt_identity())   # ✅ string → int
    claims = get_jwt()                  # pour récupérer le role
    user = User.query.get(user_id)
    if user:
        return jsonify({**user.to_dict(),'role': claims.get('role')}), 200
    return jsonify({'error': 'Utilisateur introuvable'}), 404
import os
from flask_migrate import Migrate
from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from werkzeug.security import generate_password_hash, check_password_hash
from dotenv import load_dotenv
from routes.auth import auth_bp  # On importe le blueprint du fichier auth.py
from routes.favorites import favorites_bp
from flask_jwt_extended import JWTManager
from extensions import db, migrate, jwt
from models import User, Destination, Reservation, ActivityLog 

# from models import User, Destination ,Reservation, ActivityLog




# --- CHARGEMENT DU .ENV ---
# On remonte d'un dossier (..) car app.py est dans /backend
load_dotenv(os.path.join(os.path.dirname(__file__), '../.env'))

app = Flask(__name__)

# C'est ICI qu'on définit la config, pas dans le blueprint
# app.config["JWT_SECRET_KEY"] = "ton_secret_key_super_secure"

# Permet au navigateur de faire ses vérifications de sécurité sans token
app.config["JWT_SECRET_KEY"] = os.getenv('SECRET_KEY') # Utilisez votre clé secrète
app.config["JWT_OPTIONS_ARE_TOKEN_REQUIRED"] = False
# On initialise le manager JWT avec l'app


# Configurez CORS pour accepter l'origine Angular et les headers d'authentification
CORS(app, resources={r"/*": {"origins": "http://localhost:4200"}}, 
     supports_credentials=True,
    #  Sans allow_headers=["Authorization"], le navigateur refuse d'envoyer votre token JWT.
     allow_headers=["Content-Type", "Authorization"])


# CORS(app)





# --- CONFIGURATION DYNAMIQUE ---
# On récupère les infos du .env ou on met une valeur par défaut
DB_USER = os.getenv('DB_USER')
DB_PASS = os.getenv('DB_PASSWORD')
DB_HOST = os.getenv('DB_HOST')
DB_NAME = os.getenv('DB_NAME')

# Construction de l'URL pour SQLAlchemy (Alwaysdata)
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}/{DB_NAME}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY')
app.config['JWT_SECRET_KEY'] = os.getenv('SECRET_KEY')


# --RIM--
# --- INITIALISATION  DES EXTENSIONS ---
db.init_app(app)
migrate.init_app(app, db)
jwt.init_app(app)
# --- IMPORT DES MODÈLES (IMPORTANT : après db.init_app) ---
with app.app_context():
    db.create_all()  # Crée les tables si elles n'existent pas
# --- IMPORT DES ROUTES ---
from routes.auth import auth_bp
from routes.dashboard import dashboard_bp
from routes.reservations import reservations_bp
from routes.users import users_bp
from routes.destinations import destinations_bp
from routes.reservation_routes import client_reservation_bp

# --- ENREGISTREMENT DES BLUEPRINTS ---

# "Brancher" le blueprint sur l'application
# On "enregistre" le blueprint dans l'application Flask
app.register_blueprint(auth_bp)  # Maintenant Flask sait que /register et /login existent
app.register_blueprint(favorites_bp)  # On "branche" le blueprint sur l'application
app.register_blueprint(dashboard_bp)
app.register_blueprint(reservations_bp)
app.register_blueprint(users_bp)
app.register_blueprint(destinations_bp)
app.register_blueprint(client_reservation_bp)
# -----
    
# --- ROUTES ---

@app.route("/")
def home():
    return "Backend Flask connecté à Alwaysdata avec succès 🚀"


if __name__ == '__main__':
    app.run(debug=True)
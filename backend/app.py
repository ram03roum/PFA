from datetime import timedelta
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

# Élargir la durée à 24 heures par exemple
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=24)
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
    from models import User, Destination, Reservation, ActivityLog 
# --- IMPORT DES ROUTES ---
from routes.auth import auth_bp
from routes.dashboard import dashboard_bp
from routes.reservations import reservations_bp
from routes.users import users_bp
from routes.destinations import destinations_bp

# --- ENREGISTREMENT DES BLUEPRINTS ---

# "Brancher" le blueprint sur l'application
# On "enregistre" le blueprint dans l'application Flask
app.register_blueprint(auth_bp)  # Maintenant Flask sait que /register et /login existent
app.register_blueprint(favorites_bp)  # On "branche" le blueprint sur l'application
app.register_blueprint(dashboard_bp)
app.register_blueprint(reservations_bp)
app.register_blueprint(users_bp)
app.register_blueprint(destinations_bp)
# -----
    

# migrate = Migrate(app, db)

# # Définition de la table "users"
# class User(db.Model):
#     __tablename__ = 'users'
#     id = db.Column(db.Integer, primary_key=True)
#     email = db.Column(db.String(255), unique=True, nullable=False)
#     password = db.Column(db.String(255), nullable=False)
#     name = db.Column(db.String(255))

#     def check_password(self, password):
#         return check_password_hash(self.password, password)
    
# class Destination(db.Model):
#     __tablename__ = 'destinations'
#     id = db.Column(db.Integer, primary_key=True)
#     name = db.Column(db.String(255), nullable=False)
#     country = db.Column(db.String(255))
#     continent = db.Column(db.String(255))
#     type = db.Column(db.String(255))
#     bestSeason = db.Column(db.String(255))
#     avgRating = db.Column(db.Float)
#     annualVisitors = db.Column(db.Integer)
#     unescoSite = db.Column(db.Boolean)
#     photoURL = db.Column(db.Text)
#     avgCostUSD = db.Column(db.Float)
#     Description = db.Column(db.Text)

#     def to_dict(self):
#         return {
#             'id': self.id,
#             'name': self.name,
#             'country': self.country,
#             'image': self.photoURL,
#             'price': self.avgCostUSD,
#             'category': self.type,
#             'season': self.bestSeason,
#             'rating': self.avgRating,
#             'annual Visitors': self.annualVisitors,
#             'unescoSite': self.unescoSite,
#             'description': self.Description
#         }

# --- ROUTES ---

@app.route("/")
def home():
    return "Backend Flask connecté à Alwaysdata avec succès 🚀"


# @app.route('/destinations', methods=['GET'])
# def get_destinations():
# # On récupère toutes les destinations en base
#     all_destinations = Destination.query.all()
#     # On les transforme en liste de dictionnaires
#     return jsonify([d.to_dict() for d in all_destinations])



# @app.route('/destinations/<int:dest_id>', methods=['GET'])
# def get_destination_detail(dest_id):
#     # .get_or_404() renvoie la destination ou une erreur 404 proprement
#     dest = Destination.query.get(dest_id)
    
#     if dest:
#         # On retourne une liste contenant le dictionnaire (pour garder votre format actuel)
#         return jsonify([dest.to_dict()])
#     else:
#         return jsonify({"error": "Destination not found"}), 404



if __name__ == '__main__':
    app.run(debug=True)
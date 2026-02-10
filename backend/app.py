import os
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from extensions import db, migrate, jwt
from flask_cors import CORS
from flask_jwt_extended import JWTManager
from dotenv import load_dotenv
from flask import Flask
# from models import User, Destination ,Reservation, ActivityLog


load_dotenv(os.path.join(os.path.dirname(__file__), '../.env'))

app = Flask(__name__)
# On autorise spécifiquement le header Authorization
CORS(app)

# --- CONFIGURATION (même chose que avant) ---
DB_USER = os.getenv('DB_USER')
DB_PASS = os.getenv('DB_PASSWORD')
DB_HOST = os.getenv('DB_HOST')
DB_NAME = os.getenv('DB_NAME')

app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}/{DB_NAME}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = os.getenv('SECRET_KEY')
app.config['JWT_SECRET_KEY'] = os.getenv('SECRET_KEY')

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

app.register_blueprint(auth_bp)
app.register_blueprint(dashboard_bp)
app.register_blueprint(reservations_bp)
app.register_blueprint(users_bp)
app.register_blueprint(destinations_bp)

# Maintenant, Flask sait que /login et /register existent


@app.route("/")
def home():
    return "Backend Flask connecté à Alwaysdata avec succès 🚀"

if __name__ == '__main__':
    app.run(debug=True)
# from urllib import request
# from flask import Flask, jsonify
# from flask_sqlalchemy import SQLAlchemy
# from flask_cors import CORS
# import json
# from flask_sqlalchemy import SQLAlchemy
# from werkzeug.security import generate_password_hash, check_password_hash

# app = Flask(__name__)
# CORS(app) # Permet à Angular de parler à Flask

# # Configuration de la connexion MySQL (XAMPP)
# app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:@localhost/travel_agency'
# app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# db = SQLAlchemy(app)

# # Définition de la table "destinations"
# class Destination(db.Model):
#     __tablename__ = 'destinations'
#     id = db.Column(db.Integer, primary_key=True)
#     name = db.Column(db.String(255))
#     country = db.Column(db.String(255))
#     image = db.Column(db.Text)
#     price = db.Column(db.Float)
# # Nouveaux champs pour les détails
#     continent = db.Column(db.String(255))
#     category = db.Column(db.String(255))
#     season = db.Column(db.String(255))
#     rating = db.Column(db.Float)
#     distance = db.Column(db.Float)
#     wifi = db.Column(db.Boolean)
#     description = db.Column(db.Text)
# # Créer la table dans la base de données au lancement
# with app.app_context():
#     db.create_all()
# # Définition de la table "users"
# class User(db.Model):
#     __tablename__ = 'users'
#     id = db.Column(db.Integer, primary_key=True)
#     email = db.Column(db.String(255), unique=True, nullable=False)
#     password = db.Column(db.String(255), nullable=False)
#     name = db.Column(db.String(255))
# # Créer la table dans la base de données au lancement
# with app.app_context():
#     db.create_all()

# # ROUTE 1 : Importer les données du JSON vers MySQL
# @app.route('/api/import', methods=['GET'])
# def import_data():
#     try:
#         with open('data/destinations.json', 'r', encoding='utf-8') as f:
#             data = json.load(f)
        
#         # CORRECTION : Si le JSON est un dictionnaire, on cherche la liste à l'intérieur
#         # Sinon, on utilise la liste directement
#         items_to_import = data if isinstance(data, list) else data.get('destinations', [])

#         for item in items_to_import:
#             if not Destination.query.filter_by(name=item['name']).first():
#                 new_dest = Destination(
#                     name=item['name'],
#                     country=item['country'],
#                     image=item.get('image') or item.get('photoURL'),
#                     price=float(item.get('price') or item.get('avgCostUSD') or 0),
#                     # On ajoute la récupération des nouvelles données ici
#                     continent=item.get('continent'),
#                     category=item.get('category'),
#                     season=item.get('season'),
#                     rating=item.get('rating'),
#                     distance=item.get('distance'),
#                     wifi=item.get('wifi'),
#                     description=item.get('description')
#                 )
#                 db.session.add(new_dest)

#         db.session.commit() # Sauvegarde des changements
#         return jsonify({"message": f"Importation réussie : {len(items_to_import)} éléments traités."})
#     except Exception as e:
#         return jsonify({"error": str(e)}), 500
# # ROUTE 2 : Envoyer les données de MySQL vers Angular
# @app.route('/destinations', methods=['GET'])
# def get_destinations():
#     results = Destination.query.all()
#     # On transforme les objets SQL en format JSON
#     return jsonify([{
#         'id': d.id,
#         'name': d.name,
#         'country': d.country,
#         'image': d.image,
#         'price': d.price,
#         # 'category': d.category,
#         # 'season': d.season,
#         # 'rating': d.rating,
#         # 'distance': d.distance,
#         # 'wifi': d.wifi,
#         # 'description': d.description
#     } for d in results])

# # @app.route('/destinations/<int:dest_id>', methods=['GET'])
# # def get_destination_detail(dest_id):
# #     dest = Destination.query.get(dest_id)
# #     return jsonify({
# #         'id': dest.id,
# #         'name': dest.name,
# #         'country': dest.country,
# #         'image': dest.image,
# #         'price': dest.price,
# #         'continent': dest.continent,
# #         'category': dest.category,
# #         'season': dest.season,
# #         'rating': dest.rating,
# #         'distance': dest.distance,
# #         'wifi': dest.wifi,
# #         'description': dest.description
# #     })

# # ROUTE 3 : Détails d'une destination
# @app.route('/destinations/<int:dest_id>', methods=['GET'])
# def get_destination_detail(dest_id):
#     try:
#         # On récupère la destination
#         dest = Destination.query.get_or_404(dest_id)

#         if not dest:
#             return jsonify({"error": "Pas trouvé"}), 404

#         # On renvoie TOUS les champs
#         return jsonify([{
#         'id': dest.id,
#         'name': dest.name,
#         'country': dest.country,
#         'image': dest.image,
#         'price': dest.price,
#         'category': dest.category,
#         'season': dest.season,
#         'rating': dest.rating,
#         'distance': dest.distance,
#         'wifi': dest.wifi,
#         'description': dest.description
#     }
#         ])
#     except Exception as e:
#         print(f"Erreur SQL : {e}")
#         return jsonify({"error": str(e)}), 500


# # Route 4 : Vérifier le registerment d'un utilisateur
# @app.route('/api/register', methods=['POST'])
# def register():
#     data = request.get_json() # Récupérer les données JSON envoyées par Angular

#     # 1. vérifier email
#     if User.query.filter_by(email=data['email']).first(): # email déjà utilisé
#         return jsonify({"message": "Email déjà utilisé"}), 400

#     # 2. hasher le mot de passe 
#     hashed_pw = generate_password_hash(data['password'])

#     # 3. créer utilisateur
#     user = User(
#         email=data['email'],
#         password=hashed_pw,
#         name=data.get('name')
#     )

#     db.session.add(user)
#     db.session.commit()

#     return jsonify({"message": "Inscription réussie"}), 201

# # Route 5 : Vérifier le login d'un utilisateur
# @app.route('/api/login', methods=['POST'])
# def login():
#     data = request.get_json()

#     user = User.query.filter_by(email=data['email']).first()

#     if user and check_password_hash(user.password, data['password']): #compare sans jamais voir le vrai mot de passe
#         return jsonify({
#             "message": "Connexion réussie",
#             "user": {
#                 "id": user.id,
#                 "email": user.email,
#                 "name": user.name
#             }
#         }), 200

#     return jsonify({"message": "Email ou mot de passe incorrect"}), 401

    

# if __name__ == '__main__':
#     app.run(debug=True)






# import os
# from flask_migrate import Migrate
# from flask import Flask, jsonify, request
# from flask_sqlalchemy import SQLAlchemy
# from flask_cors import CORS
# import mysql.connector
# from werkzeug.security import generate_password_hash, check_password_hash

# # Configuration
# BASE_DIR = os.path.abspath(os.path.dirname(__file__))

# app = Flask(__name__)
# CORS(app) # Activation du pont de sécurité

# # Configuration de la connexion MySQL (XAMPP)
# app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:@localhost/flask_db'
# app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# # Extensions
# db = SQLAlchemy(app)
# migrate = Migrate(app, db)

# def get_db_connection():
#     """
#     Crée et retourne une connexion MySQL pour XAMPP.
#     Ici, l'utilisateur par défaut est 'root' et il n'y a pas de mot de passe.
#     """
#     return mysql.connector.connect(
#         host="localhost",       # Le serveur MySQL local
#         user="root",            # Utilisateur par défaut de XAMPP
#         password="",            # Pas de mot de passe
#         database="flask_db"  # Remplace par le nom de ta base
#     )

# # Définition de la table "users"
# class User(db.Model):
#     __tablename__ = 'users'
#     id = db.Column(db.Integer, primary_key=True)
#     email = db.Column(db.String(255), unique=True, nullable=False)
#     password = db.Column(db.String(255), nullable=False)
#     name = db.Column(db.String(255))
#     #protection de mdp
#     # def set_password(self, password):
#     #     self.password = generate_password_hash(password)

#     def check_password(self, password):
#         return check_password_hash(self.password, password)
    
    

        


# # Route 4 : Vérifier le registerment d'un utilisateur



# @app.route('/register', methods=['POST'])
# def register():
#     data = request.get_json() # Récupérer les données JSON envoyées par Angular
#     # print("Données reçues du Frontend :", data) # <--- AJOUTE CECI
#     name=data.get('name')
#     email=data.get('email')
#     password=data.get('password')
#     if not name or not email or not password :
#         return jsonify({'error': 'champs are required'}), 400
# # On cherche si un utilisateur a déjà cet email  
#     existing_user = User.query.filter_by(email=data['email']).first() # email déjà utilisé
#     if existing_user:    
#         return jsonify({"message": "Email déjà utilisé"}), 400
    
#     # 2. hasher le mot de passe 
#     hashed_pw = generate_password_hash(data['password'])

#     # 3. créer utilisateur
#     user = User(
#         email=data['email'],
#         password=hashed_pw,
#         name=data['name']
#     )
#     # 4. Save user to database in users table via SQLAlchemy

#     db.session.add(user)
#     db.session.commit()

#     return jsonify({"message": "Inscription réussie"}), 201

# # Route 5 : Vérifier le login d'un utilisateur
# @app.route('/login', methods=['POST'])
# def login():
#     data = request.get_json()
#     email = data.get('email')
#     password = data.get('password')
# # 1. Validation (Ton ajout très pro)
#     if not email or not password:
#         return jsonify({'error': 'Username and password are required'}), 400
# # 2. Recherche de l'utilisateur dans la base de données
#     user = User.query.filter_by(email=email).first()
# # 3. Vérification du mot de passe avec hachage 
#     if user.check_password(password):
# # 4. Réponse complète pour le Frontend 
#         return jsonify({
#             'message': 'Login successful',
#             'user': {
#                 'id': user.id,
#                 'name': user.name,
#                 'email': user.email
#             }
#         }), 200
#     else :
#         return jsonify({"message": "Email ou mot de passe incorrect"}), 401

    
#     # except Exception as e:
#     # return jsonify({"error": str(e)}), 500

# if __name__ == '__main__':
#     app.run(debug=True)


import os
from flask_migrate import Migrate
from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from werkzeug.security import generate_password_hash, check_password_hash
from dotenv import load_dotenv

# --- CHARGEMENT DU .ENV ---
# On remonte d'un dossier (..) car app.py est dans /backend
load_dotenv(os.path.join(os.path.dirname(__file__), '../.env'))

app = Flask(__name__)
CORS(app)

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

db = SQLAlchemy(app)
migrate = Migrate(app, db)

# Définition de la table "users"
class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(255), unique=True, nullable=False)
    password = db.Column(db.String(255), nullable=False)
    name = db.Column(db.String(255))

    def check_password(self, password):
        return check_password_hash(self.password, password)
    
class Destination(db.Model):
    __tablename__ = 'destinations'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(255), nullable=False)
    country = db.Column(db.String(255))
    continent = db.Column(db.String(255))
    type = db.Column(db.String(255))
    bestSeason = db.Column(db.String(255))
    avgRating = db.Column(db.Float)
    annualVisitors = db.Column(db.Integer)
    unescoSite = db.Column(db.Boolean)
    photoURL = db.Column(db.Text)
    avgCostUSD = db.Column(db.Float)
    Description = db.Column(db.Text)

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'country': self.country,
            'image': self.photoURL,
            'price': self.avgCostUSD,
            'category': self.type,
            'season': self.bestSeason,
            'rating': self.avgRating,
            'annual Visitors': self.annualVisitors,
            'unescoSite': self.unescoSite,
            'description': self.Description
        }

# --- ROUTES ---

@app.route("/")
def home():
    return "Backend Flask connecté à Alwaysdata avec succès 🚀"


@app.route('/destinations', methods=['GET'])
def get_destinations():
# On récupère toutes les destinations en base
    all_destinations = Destination.query.all()
    # On les transforme en liste de dictionnaires
    return jsonify([d.to_dict() for d in all_destinations])



@app.route('/destinations/<int:dest_id>', methods=['GET'])
def get_destination_detail(dest_id):
    # .get_or_404() renvoie la destination ou une erreur 404 proprement
    dest = Destination.query.get(dest_id)
    
    if dest:
        # On retourne une liste contenant le dictionnaire (pour garder votre format actuel)
        return jsonify([dest.to_dict()])
    else:
        return jsonify({"error": "Destination not found"}), 404

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    name = data.get('name')
    email = data.get('email')
    password = data.get('password')

    if not name or not email or not password:
        return jsonify({'error': 'Tous les champs sont requis'}), 400

    existing_user = User.query.filter_by(email=email).first()
    if existing_user:    
        return jsonify({"message": "Email déjà utilisé"}), 400
    
    hashed_pw = generate_password_hash(password)
    user = User(email=email, password=hashed_pw, name=name)

    db.session.add(user)
    db.session.commit()
    return jsonify({"message": "Inscription réussie"}), 201

@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    email = data.get('email')
    password = data.get('password')

    if not email or not password:
        return jsonify({'error': 'Email et mot de passe requis'}), 400

    user = User.query.filter_by(email=email).first()

    if user and user.check_password(password):
        return jsonify({
            'message': 'Login successful',
            'user': {'id': user.id, 'name': user.name, 'email': user.email}
        }), 200
    else:
        return jsonify({"message": "Email ou mot de passe incorrect"}), 401

if __name__ == '__main__':
    app.run(debug=True)
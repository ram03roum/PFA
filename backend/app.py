from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
import mysql.connector
from werkzeug.security import generate_password_hash, check_password_hash


app = Flask(__name__)
CORS(app) # Activation du pont de sécurité

# Configuration de la connexion MySQL (XAMPP)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:@localhost/flask_db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

def get_db_connection():
    """
    Crée et retourne une connexion MySQL pour XAMPP.
    Ici, l'utilisateur par défaut est 'root' et il n'y a pas de mot de passe.
    """
    return mysql.connector.connect(
        host="localhost",       # Le serveur MySQL local
        user="root",            # Utilisateur par défaut de XAMPP
        password="",            # Pas de mot de passe
        database="flask_db"  # Remplace par le nom de ta base
    )

# Définition de la table "users"
class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(255), unique=True, nullable=False)
    password = db.Column(db.String(255), nullable=False)
    name = db.Column(db.String(255))
# Créer la table dans la base de données au lancement
with app.app_context():
    db.create_all()
    
@app.route("/")
def home():
    return "Backend Flask connecté avec succès 🚀" 

# --- 1️⃣ Charger les destinations au lancement de l'application ---
def load_destinations():
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT id, name, country, continent, type, bestSeason,avgRating, annualVisitors, unescoSite, photoURL, avgCostUSD , Description FROM destinations")
    results = cursor.fetchall()
    cursor.close()
    conn.close()
    return results

# Stockage global des destinations
DESTINATIONS = load_destinations()


# --- 2️⃣ Route pour toutes les destinations ---
@app.route('/destinations', methods=['GET'])
def get_destinations():
    return jsonify(DESTINATIONS)



# ROUTE 3 : Détails d'une destination
@app.route('/destinations/<int:dest_id>', methods=['GET'])
def get_destination_detail(dest_id):
        # On récupère la destination
    dest = next((d for d in DESTINATIONS if d['id'] == dest_id), None)
    if dest:
        return jsonify([{
        'id': dest['id'],
        'name': dest['name'],
        'country': dest['country'],
        'image': dest['photoURL'],
        'price': dest['avgCostUSD'],
        'category': dest['type'],
        'season': dest['bestSeason'],
        'rating': dest['avgRating'],
        'annual Visitors': dest['annualVisitors'],
        'unescoSite': dest['unescoSite'],
        'description': dest['Description']
    }
        ])
    else:
        return jsonify({"error": "Destination not found"}), 404
        


# # Route 4 : Vérifier le registerment d'un utilisateur
@app.route('/api/register', methods=['POST'])
def register():
    data = request.get_json() # Récupérer les données JSON envoyées par Angular

    # 1. vérifier email
    if User.query.filter_by(email=data['email']).first(): # email déjà utilisé
        return jsonify({"message": "Email déjà utilisé"}), 400

    # 2. hasher le mot de passe 
    hashed_pw = generate_password_hash(data['password'])

    # 3. créer utilisateur
    user = User(
        email=data['email'],
        password=hashed_pw,
        name=data.get('name')
    )

    db.session.add(user)
    db.session.commit()

    return jsonify({"message": "Inscription réussie"}), 201

# Route 5 : Vérifier le login d'un utilisateur
@app.route('/api/login', methods=['POST'])
def login():
    data = request.get_json()

    user = User.query.filter_by(email=data['email']).first()

    if user and check_password_hash(user.password, data['password']): #compare sans jamais voir le vrai mot de passe
        return jsonify({
            "message": "Connexion réussie",
            "user": {
                "id": user.id,
                "email": user.email,
                "name": user.name
            }
        }), 200

    return jsonify({"message": "Email ou mot de passe incorrect"}), 401

    
    # except Exception as e:
    # return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
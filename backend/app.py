from flask import Flask, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS # Autorise Angular (port 4200) à parler à Flask (port 5000)

app = Flask(__name__)
CORS(app) # Activation du pont de sécurité

# 1. Connexion à MySQL (XAMPP)
# Format: mysql+pymysql://utilisateur:mot_de_passe@serveur/nom_base
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:@localhost/flask_db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

# 2. Définition du Modèle (Le plan de votre table destinations)
class Destination(db.Model):
    __tablename__ = 'destinations'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(255))
    country = db.Column(db.String(255))
    photoURL = db.Column(db.Text)
    avgCostUSD = db.Column(db.Float)   # description = db.Column(db.Text) # Ajoutez les colonnes selon votre table

# 3. Route API pour envoyer les données à Angular
@app.route('/api/destinations', methods=['GET'])
def get_destinations():
    try:
        # Récupérer toutes les lignes de la table
        query_results = Destination.query.all()
        
        # Transformer les données en format JSON (liste de dictionnaires)
        destinations_list = []
        for d in query_results:
            destinations_list.append({
                'id': d.id,
                'name': d.name,
                'country': d.country,
                'image': d.photoURL,
                'avgCostUSD': d.avgCostUSD
            })
        
        return jsonify(destinations_list)
    
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
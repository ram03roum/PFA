from flask import Flask, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
import json

app = Flask(__name__)
CORS(app) # Permet à Angular de parler à Flask

# Configuration de la connexion MySQL (XAMPP)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:@localhost/travel_agency'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

# Définition de la table "destinations"
class Destination(db.Model):
    __tablename__ = 'destinations'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(255))
    country = db.Column(db.String(255))
    image = db.Column(db.Text)
    price = db.Column(db.Float)
# Nouveaux champs pour les détails
    continent = db.Column(db.String(255))
    category = db.Column(db.String(255))
    season = db.Column(db.String(255))
    rating = db.Column(db.Float)
    distance = db.Column(db.Float)
    wifi = db.Column(db.Boolean)
    description = db.Column(db.Text)
# Créer la table dans la base de données au lancement
with app.app_context():
    db.create_all()

# ROUTE 1 : Importer les données du JSON vers MySQL
@app.route('/api/import', methods=['GET'])
def import_data():
    try:
        with open('data/destinations.json', 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        # CORRECTION : Si le JSON est un dictionnaire, on cherche la liste à l'intérieur
        # Sinon, on utilise la liste directement
        items_to_import = data if isinstance(data, list) else data.get('destinations', [])

        for item in items_to_import:
            if not Destination.query.filter_by(name=item['name']).first():
                new_dest = Destination(
                    name=item['name'],
                    country=item['country'],
                    image=item.get('image') or item.get('photoURL'),
                    price=float(item.get('price') or item.get('avgCostUSD') or 0),
                    # On ajoute la récupération des nouvelles données ici
                    continent=item.get('continent'),
                    category=item.get('category'),
                    season=item.get('season'),
                    rating=item.get('rating'),
                    distance=item.get('distance'),
                    wifi=item.get('wifi'),
                    description=item.get('description')
                )
                db.session.add(new_dest)

        db.session.commit() # Sauvegarde des changements
        return jsonify({"message": f"Importation réussie : {len(items_to_import)} éléments traités."})
    except Exception as e:
        return jsonify({"error": str(e)}), 500
# ROUTE 2 : Envoyer les données de MySQL vers Angular
@app.route('/destinations', methods=['GET'])
def get_destinations():
    results = Destination.query.all()
    # On transforme les objets SQL en format JSON
    return jsonify([{
        'id': d.id,
        'name': d.name,
        'country': d.country,
        'image': d.image,
        'price': d.price,
        # 'category': d.category,
        # 'season': d.season,
        # 'rating': d.rating,
        # 'distance': d.distance,
        # 'wifi': d.wifi,
        # 'description': d.description
    } for d in results])

# @app.route('/destinations/<int:dest_id>', methods=['GET'])
# def get_destination_detail(dest_id):
#     dest = Destination.query.get(dest_id)
#     return jsonify({
#         'id': dest.id,
#         'name': dest.name,
#         'country': dest.country,
#         'image': dest.image,
#         'price': dest.price,
#         'continent': dest.continent,
#         'category': dest.category,
#         'season': dest.season,
#         'rating': dest.rating,
#         'distance': dest.distance,
#         'wifi': dest.wifi,
#         'description': dest.description
#     })

# ROUTE 3 : Détails d'une destination
@app.route('/destinations/<int:dest_id>', methods=['GET'])
def get_destination_detail(dest_id):
    try:
        # On récupère la destination
        dest = Destination.query.get_or_404(dest_id)

        if not dest:
            return jsonify({"error": "Pas trouvé"}), 404

        # On renvoie TOUS les champs
        return jsonify([{
        'id': dest.id,
        'name': dest.name,
        'country': dest.country,
        'image': dest.image,
        'price': dest.price,
        'category': dest.category,
        'season': dest.season,
        'rating': dest.rating,
        'distance': dest.distance,
        'wifi': dest.wifi,
        'description': dest.description
    }
        ])
    except Exception as e:
        print(f"Erreur SQL : {e}")
        return jsonify({"error": str(e)}), 500
    

if __name__ == '__main__':
    app.run(debug=True)
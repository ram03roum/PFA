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
                    price=float(item.get('price') or item.get('avgCostUSD') or 0)
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
        'price': d.price
    } for d in results])

if __name__ == '__main__':
    app.run(debug=True)
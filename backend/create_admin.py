from extensions import db
from models import User
from werkzeug.security import generate_password_hash
from app import app

with app.app_context():
    # Créer un nouvel admin avec mot de passe connu
    existing_admin = User.query.filter_by(email='admin@travel.com').first()
    if existing_admin:
        print("Admin déjà existant: admin@travel.com")
    else:
        hashed = generate_password_hash('admin123')
        admin = User(
            email='admin@travel.com',
            password=hashed,
            name='Administrator',
            role='admin',
            status='actif'
        )
        db.session.add(admin)
        db.session.commit()
        print("✅ Nouvel admin créé avec succès!")
        print("📧 Email: admin@travel.com")
        print("🔑 Mot de passe: admin123")
        print("👤 Rôle: admin")

    # Lister les admins existants
    admins = User.query.filter_by(role='admin').all()
    print(f"\n📋 Admins existants ({len(admins)}):")
    for admin in admins:
        print(f"- {admin.name} ({admin.email})")
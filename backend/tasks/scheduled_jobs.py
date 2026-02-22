from extensions import db
from models import User, Reservation
from services.mail_service import send_automated_email

def check_and_send_reminders(app):
    with app.app_context():
        
        reservations_impayees = Reservation.query.filter_by(status='en attente').all()
        
        if not reservations_impayees:
            print("INFO : Aucune réservation 'en attente' trouvée.")
            return

        for res in reservations_impayees:
            # TEST : on filtre uniquement la réservation qu'on veut tester
            user = User.query.filter_by(name='foufa').first()
            
            if not user or not user.email:
                print(f"SKIP : Pas de user 'foufa' trouvé.")
                continue

            # ✅ On n'envoie que pour la réservation ID=2 pendant les tests
            if res.id != 2:
                continue

            print(f"ENVOI EN COURS : Relance pour {user.email} (Réservation ID: {res.id})")
            send_automated_email(
                recipient=user.email,
                subject="Rappel : Votre réservation attend votre paiement",
                template="rappel_paiement",
                user=user,
                reservation=res
            )
        
        print("Extraction terminée.")
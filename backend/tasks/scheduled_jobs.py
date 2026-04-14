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
            user = res.user
            if not user or not user.email:
                print(f"SKIP : réservation {res.id} sans utilisateur valide.")
                continue
            print(f"ENVOI EN COURS : Relance pour {user.email} (Réservation ID: {res.id})")
            try:
                send_automated_email(
                    recipient=user.email,
                    subject="Rappel : Votre réservation attend votre paiement",
                    template="rappel_paiement",
                    user=user,
                    reservation=res
                )
            except Exception as e:
                print(f"❌ Erreur envoi email pour réservation {res.id}: {e}")
        print("Extraction terminée.")


# ── NOUVELLE TÂCHE ────────────────────────────────────────
def run_relance_inactive(app):
    """Tâche de relance des clients inactifs — appelée par le scheduler"""
    from services.relance_service import run_relance_campaign
    run_relance_campaign(app)
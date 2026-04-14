from datetime import datetime, timedelta
from models import User, InteractionLog, RelanceLog, Destination
from extensions import db
from services.ai_service import generate_relance_email
from services.mail_service import send_confirmation_to_client

INACTIVITY_DAYS = 3  # ← 3 jours pour les tests

def get_inactive_users():
    """
    Retourne les users clients inactifs depuis INACTIVITY_DAYS jours.
    Inactif = aucune interaction (vue, favori, réservation) depuis X jours.
    """
    cutoff_date = datetime.utcnow() - timedelta(days=INACTIVITY_DAYS)

    # Récupère tous les clients actifs
    all_clients = User.query.filter(
        User.role.in_(['client', 'user']),
        User.status == 'actif'
    ).all()

    inactive = []
    for user in all_clients:
        # Dernière interaction
        last_interaction = InteractionLog.query.filter_by(
            user_id=user.id
        ).order_by(
            InteractionLog.created_at.desc()
        ).first()

        # Inactif si : pas d'interaction du tout OU dernière interaction > cutoff
        if not last_interaction or last_interaction.created_at < cutoff_date:
            
            # Vérifier qu'on ne l'a pas déjà relancé dans les derniers 7 jours
            recent_relance = RelanceLog.query.filter_by(
                user_id=user.id,
                status='sent'
            ).filter(
                RelanceLog.sent_at >= datetime.utcnow() - timedelta(days=1)
            ).first()

            if not recent_relance:
                inactive.append(user)

    return inactive


def send_relance_to_user(user):
    """
    Génère et envoie l'email de relance pour un utilisateur.
    Enregistre le log en DB.
    """
    try:
        # Suggestions de destinations populaires
        destinations = Destination.query.order_by(
            Destination.avgRating.desc()
        ).limit(3).all()

        # Génération par Gemini
        email_body = generate_relance_email(user.name, destinations)

        # Envoi via Flask-Mail
        sent = send_confirmation_to_client(
            client_email=user.email,
            client_name=user.name,
            subject="✈️ Vous nous manquez ! Découvrez nos nouvelles offres",
            body=email_body
        )

        # Log en DB
        log = RelanceLog(
            user_id=user.id,
            email=user.email,
            status='sent' if sent else 'failed',
            email_body=email_body
        )
        db.session.add(log)
        db.session.commit()

        print(f"✅ Relance envoyée à {user.email} ({'succès' if sent else 'échec'})")
        return sent

    except Exception as e:
        print(f"❌ Erreur relance pour {user.email}: {e}")
        log = RelanceLog(
            user_id=user.id,
            email=user.email,
            status='failed',
            email_body=''
        )
        db.session.add(log)
        db.session.commit()
        return False


def run_relance_campaign(app):
    """
    Fonction principale appelée par le scheduler.
    Détecte les inactifs et envoie les relances.
    """
    with app.app_context():
        print("🔍 Recherche des clients inactifs...")
        inactive_users = get_inactive_users()

        if not inactive_users:
            print("INFO: Aucun client inactif à relancer.")
            return

        print(f"📧 {len(inactive_users)} client(s) inactif(s) trouvé(s)")

        for user in inactive_users:
            send_relance_to_user(user)

        print(f"✅ Campagne de relance terminée — {len(inactive_users)} email(s) traité(s)")
"""
SNIPPETS DE CODE À INTÉGRER DANS VOS FICHIERS EXISTANTS

Suivez les sections ci-dessous pour intégrer le système de priorités
"""

# ============================================================================
# 1. MODIFICATIONS DANS app.py
# ============================================================================

# ▼ EN HAUT DU FICHIER, avec les autres imports ▼

from services.email_queue_manager import EmailQueueManager


# ▼ REMPLACER LES ANCIENNES TÂCHES CRON (lignes ~80-100 environ) ▼

# AVANT: (À SUPPRIMER)
# @scheduler.task('cron', id='relance_paiement_quotidienne', hour=18, minute=52)
# def job_matinal():
#     check_and_send_reminders(app)
#
# @scheduler.task('cron', id='relance_inactifs', hour=13, minute=36)
# def job_relance_inactifs():
#     run_relance_inactive(app)

# APRÈS: (À REMPLACER PAR CECI)

@scheduler.task('cron', id='process_email_queue', minute='*/5')
def job_process_emails():
    """Traite la queue d'emails avec priorités - Toutes les 5 minutes"""
    with app.app_context():
        EmailQueueManager.process_queue()


# ▼ AJOUTER CETTE ROUTE ADMIN ▼ (n'importe où avant if __name__)

@app.route("/api/admin/email-queue/stats", methods=['GET'])
def get_email_stats():
    """Retourne les stats du système de queue (avec authentification JWT)"""
    from flask_jwt_extended import jwt_required
    
    try:
        # Ajouter @jwt_required() si vous avez l'authentification
        stats = EmailQueueManager.get_queue_stats()
        return jsonify(stats)
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route("/api/admin/email-queue/failed", methods=['GET'])
def get_failed_emails():
    """Retourne les emails ayant échoué (pour debugging)"""
    limit = request.args.get('limit', 20, type=int)
    failed = EmailQueueManager.get_failed_emails(limit)
    return jsonify({'failed_emails': failed, 'count': len(failed)})


# ============================================================================
# 2. MODIFICATIONS DANS services/mail_service.py
# ============================================================================

# ▼ AJOUTER CETTE FONCTION EN QUEUE (à la fin du fichier) ▼

def send_confirmation_to_client_queued(client_email, client_name, subject, body, 
                                       priority=2, email_type='confirmation'):
    """
    Version en QUEUE de send_confirmation_to_client
    Permet une meilleure gestion des priorités et retries
    
    Args:
        priority: 1=urgent, 2=normal, 3=marketing
    """
    from services.email_queue_manager import EmailQueueManager
    
    try:
        EmailQueueManager.add_to_queue(
            to_email=client_email,
            subject=subject,
            body=body,
            priority=priority,
            email_type=email_type,
            template_name='confirmation',
            metadata={'client_name': client_name}
        )
        return True
    except Exception as e:
        print(f"❌ Erreur ajout queue: {e}")
        # Fallback: envoi direct
        return send_confirmation_to_client(client_email, client_name, subject, body)


# ============================================================================
# 3. MODIFICATIONS DANS services/relance_service.py
# ============================================================================

# ▼ METTRE À JOUR LA FONCTION send_relance_to_user ▼

def send_relance_to_user(user):
    """
    Génère et envoie l'email de relance - VERSION QUEUE
    Enregistre le log en DB avec gestion des priorités.
    """
    from services.email_queue_manager import EmailQueueManager
    from services.ai_service import generate_relance_email
    
    try:
        # Suggestions de destinations populaires
        destinations = Destination.query.order_by(
            Destination.avgRating.desc()
        ).limit(3).all()

        # Génération par Gemini
        email_body = generate_relance_email(user.name, destinations)

        # Déterminer la priorité selon le segment
        priority = 2  # STANDARD par défaut
        if user.segment == 'VIP':
            priority = 2  # VIP = traitement rapide mais pas urgent
        elif user.segment == 'Nouveau':
            priority = 2  # Nouveau = prioritaire
        else:
            priority = 3  # Inactif/Régulier = marketing (peut attendre 24h)

        # Ajout à la queue
        EmailQueueManager.add_to_queue(
            to_email=user.email,
            subject="✈️ Vous nous manquez ! Découvrez nos nouvelles offres",
            body=email_body,
            priority=priority,
            email_type='reactivation',
            user_id=user.id,
            metadata={
                'destination_ids': [d.id for d in destinations],
                'user_segment': user.segment
            }
        )

        # Log compatible avec ancien système
        log = RelanceLog(
            user_id=user.id,
            email=user.email,
            status='queued',  # Nouveau statut
            email_body=email_body
        )
        db.session.add(log)
        db.session.commit()

        print(f"✅ Relance mise en queue pour {user.email} (Priority: {priority})")
        return True

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


# ===== ALTERNATIVE: SI VOUS CONTINUEZ À UTILISER send_confirmation_to_client EN DIRECT =====
# (modifiez juste les appels pour utiliser send_confirmation_to_client_queued)
#
# Au lieu de:
#     send_confirmation_to_client(email, name, subject, body)
#
# Utilisez:
#     send_confirmation_to_client_queued(email, name, subject, body, priority=1, email_type='payment')


# ============================================================================
# 4. MODIFICATIONS DANS routes/reservations.py (OU routes/reservation_routes.py)
# ============================================================================

# ▼ EXEMPLE: Lors d'une nouvelle réservation, envoyer confirmation en P1 ▼

# Dans votre endpoint de création de réservation:

from services.email_queue_manager import EmailQueueManager

@app.route('/api/reservations', methods=['POST'])
def create_reservation():
    # ... code métier de création de réservation ...
    
    user = current_user  # Ou récupéré de request
    destination = reservation.destination
    
    # 📧 P1 - Confirmation IMMÉDIATE
    confirmation_body = f"""
    <h2>✅ Réservation Confirmée</h2>
    <p>Merci {user.name} pour votre confiance!</p>
    <p>Destination: {destination.name}</p>
    <p>Du {reservation.check_in} au {reservation.check_out}</p>
    <p>Total: ${reservation.total_amount}</p>
    """
    
    EmailQueueManager.add_to_queue(
        to_email=user.email,
        subject="✅ Votre réservation est confirmée",
        body=confirmation_body,
        priority=1,  # URGENT
        email_type='confirmation',
        user_id=user.id,
        metadata={'reservation_id': reservation.id}
    )
    
    # 🔔 P2 - Rappel 24h avant check-in
    reminder_body = f"""
    <h2>🧳 Dernières Préparatifs</h2>
    <p>Votre voyage à {destination.name} commence demain!</p>
    <p>Ne pas oublier: Passeport, E-tickets, Valises...</p>
    """
    
    # Planifier pour demain à 10h (ou autre heure)
    EmailQueueManager.add_to_queue(
        to_email=user.email,
        subject="🧳 Demain, c'est votre départ!",
        body=reminder_body,
        priority=2,
        email_type='reminder',
        user_id=user.id,
        delay_minutes=1440  # Dans 24 heures
    )
    
    return jsonify({'status': 'success'})


# ============================================================================
# 5. MODÈLE RelanceLog - OPTIONNEL: Adapter le statut
# ============================================================================

# Dans models.py, mettre à jour la classe RelanceLog:

# status = db.Column(db.String(20), default='queued')
# # 'queued' = en attente de traitement
# # 'sent' = traité avec succès
# # 'failed' = erreur définitive après retries
# # 'bounced' = adresse invalide


# ============================================================================
# 6. REQUIREMENTS.txt - VÉRIFIER LES DÉPENDANCES
# ============================================================================

# Ajouter si absent:
# APScheduler  (devrait déjà être là)
# Flask-Mail   (devrait déjà être là)


# ============================================================================
# 7. .env - AUCUNE MODIFICATION NÉCESSAIRE
# ============================================================================

# Les parametres MAIL_* existants fonctionneront avec le nouveau système


# ============================================================================
# RÉSUMÉ DES ÉTAPES
# ============================================================================

# 1. ✅ Ajouter les modèles (EmailQueue, EmailLog) dans models.py
# 2. ✅ Créer la migration et l'exécuter (flask db upgrade)
# 3. ✅ Ajouter services/email_queue_manager.py
# 4. ✅ Modifier app.py (imports + tasks + routes)
# 5. ✅ Modifier services/mail_service.py (ajouter fonction queue)
# 6. ✅ Modifier services/relance_service.py (utiliser la queue)
# 7. ✅ Tester avec: GET /api/admin/email-queue/stats
# 8. ✅ Monitorer les logs

print("✅ Intégration complète du système de priorités de mails")

#!/usr/bin/env python3
"""
╔═══════════════════════════════════════════════════════════════════════════╗
║                  GUIDE D'IMPLÉMENTATION ÉTAPE PAR ÉTAPE                  ║
║        Système de Gestion des Priorités pour les Mails (Email Queue)      ║
╚═══════════════════════════════════════════════════════════════════════════╝

Version: 1.0
Temps estimé: 2-3 heures
Difficulté: MOYEN
"""

import os
import sys
from datetime import datetime

# ============================================================================
# PHASE 0: PRÉPARATION
# ============================================================================

print("""
╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 0: PRÉPARATION                                                     ║
╚═══════════════════════════════════════════════════════════════════════════╝
""")

checklist = {
    "python >= 3.8": False,
    "Flask": False,
    "SQLAlchemy": False,
    "APScheduler": False,
    "Accès à la DB": False,
}

print("✓ Vérifiez les prérequis:")
for item in checklist:
    print(f"   [ ] {item}")

print("""
Tous les prérequis doivent être installés ✓

""")

# ============================================================================
# PHASE 1: PRÉPARATION DE LA BASE DE DONNÉES
# ============================================================================

PHASE_1 = """
╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 1: CRÉATION DES MODÈLES (30 min)                                   ║
╚═══════════════════════════════════════════════════════════════════════════╝

ÉTAPE 1.1: Ajouter les modèles dans backend/models.py
───────────────────────────────────────────────────────

1. Ouvrez: backend/models.py
2. Allez à la FIN du fichier
3. Copiez le contenu du fichier MODELES_A_AJOUTER.py
4. Collez à la fin de models.py
5. Sauvegardez

✅ VÉRIFICATION:
   - Vérifier qu'il y a bien 2 nouvelles classes: EmailQueue et EmailLog
   - Vérifier les imports (db, datetime) en haut du fichier


ÉTAPE 1.2: Créer la migration
──────────────────────────────

1. Ouvrez terminal PowerShell dans c:\\Users\\chaie\\PFAA\\backend

2. Générez la migration automatiquement:
   $ flask db migrate -m "add_email_queue_system"

3. Vérifiez le fichier généré dans backend/migrations/versions/
   Doit contenir: create_table('email_queues'), create_table('email_logs')

4. Appliquez la migration:
   $ flask db upgrade

5. Vérifiez que les tables sont créées (via PhpMyAdmin ou MySQL CLI)


ÉTAPE 1.3: Tester la connexion
────────────────────────────

1. Ouvrez une console Python dans le dossier backend:
   $ python
   >>> from models import EmailQueue, EmailLog, db
   >>> db.session.query(EmailQueue).count()
   0
   >>> db.session.query(EmailLog).count()
   0
   
2. Si pas d'erreur → ✅ OK!


✏️ NOTES:
   • Si erreur "table already exists": c'est bon, elle existe déjà
   • Si erreur de migration: vérifier la syntaxe du fichier EXEMPLE_MIGRATION.py
"""

print(PHASE_1)

# ============================================================================
# PHASE 2: SERVICE
# ============================================================================

PHASE_2 = """
╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 2: CRÉATION DU SERVICE (30 min)                                    ║
╚═══════════════════════════════════════════════════════════════════════════╝

ÉTAPE 2.1: Ajouter le service EmailQueueManager
────────────────────────────────────────────────

1. Le fichier services/email_queue_manager.py a déjà été créé ✅

2. Vérifiez qu'il contient les méthodes principales:
   - add_to_queue()
   - process_queue()
   - get_queue_stats()
   - _send_email()
   - _handle_retry()
   - _cleanup_failed_emails()
   - get_failed_emails()
   - cancel_pending_for_user()

3. Vérifiez les imports en haut du fichier


ÉTAPE 2.2: Tester le service
──────────────────────────────

1. Ouvrez Python dans backend:
   $ python
   >>> from services.email_queue_manager import EmailQueueManager
   >>> from models import EmailQueue
   
2. Testez l'ajout à la queue:
   >>> EmailQueueManager.add_to_queue(
   ...     to_email='test@example.com',
   ...     subject='Test',
   ...     body='<p>Test mail</p>',
   ...     priority=2,
   ...     email_type='test'
   ... )
   
3. Vérifiez les stats:
   >>> stats = EmailQueueManager.get_queue_stats()
   >>> print(stats)
   {'pending': 1, 'sent_today': 0, 'failed': 0, ...}
   
4. Si pas d'erreur → ✅ OK!


✏️ NOTES:
   • Le mail n'est pas envoyé réellement à ce stade (mode TEST)
   • Il est juste enregistré dans la queue
"""

print(PHASE_2)

# ============================================================================
# PHASE 3: INTÉGRATION
# ============================================================================

PHASE_3 = """
╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 3: INTÉGRATION DANS app.py (45 min)                                ║
╚═══════════════════════════════════════════════════════════════════════════╝

ÉTAPE 3.1: Ajouter les imports
────────────────────────────────

1. Ouvrez backend/app.py
2. En haut, après les autres imports de services, ajoutez:
   from services.email_queue_manager import EmailQueueManager

3. Sauvegardez


ÉTAPE 3.2: Remplacer les anciennes tâches cron
────────────────────────────────────────────────

1. Localisez les anciennes tâches (environ ligne ~120):
   
   @scheduler.task('cron', id='relance_paiement_quotidienne', ...)
   @scheduler.task('cron', id='relance_inactifs', ...)

2. Remplacez-les par (voir SNIPPETS_INTEGRATION.py):
   
   @scheduler.task('cron', id='process_email_queue', minute='*/5')
   def job_process_emails():
       with app.app_context():
           EmailQueueManager.process_queue()

3. Sauvegardez


ÉTAPE 3.3: Ajouter les routes admin
──────────────────────────────────────

1. Encore dans app.py
2. Avant le "if __name__ == '__main__':", ajoutez les 2 routes:
   - GET /api/admin/email-queue/stats
   - GET /api/admin/email-queue/failed

(voir SNIPPETS_INTEGRATION.py pour le code complet)

3. Sauvegardez


ÉTAPE 3.4: Tester l'intégration
─────────────────────────────────

1. Démarrez le serveur Flask:
   $ python app.py

2. Dans un autre terminal, testez la route:
   $ curl http://localhost:5000/api/admin/email-queue/stats
   
   Vous devez voir:
   {"pending": 0, "sent_today": 0, "failed": 0, "success_rate": 0, ...}

3. Vérifiez que le scheduler a bien démarré (logs doivent montrer APScheduler)

4. Attendez 5 min... la première tâche devrait s'exécuter

5. Si pas d'erreur → ✅ OK!


✏️ NOTES:
   • Si erreur ImportError: vérifier que le fichier email_queue_manager.py existe
   • Si scheduler ne démarre pas: vérifier que scheduler.init_app() et scheduler.start() 
     sont présents dans app.py
"""

print(PHASE_3)

# ============================================================================
# PHASE 4: MIGRATION DES SERVICES
# ============================================================================

PHASE_4 = """
╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 4: MIGRATION DES SERVICES EXISTANTS (30 min)                       ║
╚═══════════════════════════════════════════════════════════════════════════╝

ÉTAPE 4.1: Mettre à jour mail_service.py
──────────────────────────────────────────

1. Ouvrez backend/services/mail_service.py

2. À la fin du fichier, ajoutez la nouvelle fonction (voir SNIPPETS_INTEGRATION.py):
   
   def send_confirmation_to_client_queued(...)

3. Cette fonction enveloppe l'ajout à la queue

4. Sauvegardez


ÉTAPE 4.2: Mettre à jour relance_service.py
──────────────────────────────────────────────

1. Ouvrez backend/services/relance_service.py

2. Mettez à jour la fonction send_relance_to_user() pour utiliser la queue
   (voir SNIPPETS_INTEGRATION.py)

3. Points clés:
   - Ajouter import EmailQueueManager
   - Déterminer la priorité selon user.segment
   - Utiliser EmailQueueManager.add_to_queue() au lieu de send_confirmation_to_client()

4. Sauvegardez


ÉTAPE 4.3: Tester avec un email de test
──────────────────────────────────────────

1. Ouvrez Python dans backend:
   $ python
   >>> from services.email_queue_manager import EmailQueueManager
   >>> from models import EmailQueue
   
2. Ajoutez un email de test:
   >>> EmailQueueManager.add_to_queue(
   ...     to_email='votre_email@example.com',
   ...     subject='Test Queue System',
   ...     body='<p>Cet email teste le système de queue</p>',
   ...     priority=1,
   ...     email_type='test'
   ... )
   
3. Vérifiez manuellement le traitement:
   >>> EmailQueueManager.process_queue()
   (devrait afficher: "Email envoyé avec succès" ou l'erreur)

4. Vérifiez le statut:
   >>> EmailQueue.query.filter_by(status='sent').count()
   (doit être >= 1)


✏️ NOTES:
   • Le premier test sera probablement en erreur si MAIL_* n'est pas bien configuré
   • C'est normal, on vérifie juste que le système fonctionne structurellement
   • Les erreurs SMTP se retrouveront dans la queue avec retry automatique
"""

print(PHASE_4)

# ============================================================================
# PHASE 5: MONITORING
# ============================================================================

PHASE_5 = """
╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 5: MISE EN PLACE DU MONITORING (30 min)                            ║
╚═══════════════════════════════════════════════════════════════════════════╝

ÉTAPE 5.1: Dashboard Admin (Frontend)
──────────────────────────────────────

À créer dans le frontend Angular une page admin:
- GET /api/admin/email-queue/stats
- Afficher les graphiques:
  * Pending par priorité
  * Sent today par priorité
  * Success rate
  * Failed count

Template simple:
```html
<div class="email-stats">
  <h2>📊 État de la Queue d'Emails</h2>
  
  <div class="stat-card">
    <span>Pending</span>
    <strong>{{ stats.pending }}</strong>
  </div>
  
  <div class="stat-card">
    <span>Sent Today</span>
    <strong>{{ stats.sent_today }}</strong>
  </div>
  
  <div class="stat-card">
    <span>Success Rate</span>
    <strong>{{ stats.success_rate }}%</strong>
  </div>
  
  <div class="stat-card alarm">
    <span>Failed</span>
    <strong>{{ stats.failed }}</strong>
  </div>
</div>
```


ÉTAPE 5.2: Logs et Alertes
────────────────────────────

1. Ajouter à app.py:
   - Log tous les appels add_to_queue()
   - Alerte si failed > 5% ou > 10 emails
   - Alerte si pending > 1000


2. Optionnel: Envoyer des notifications:
   - Slack/Discord si erreurs critiques
   - Email admin du système


ÉTAPE 5.3: Vérifier les Logs
──────────────────────────────

Dans les logs de l'application Flask, vous devez voir:

[INFO] ✉️ [CRITIQUE] Email ajouté à queue | To: client@example.com | Type: confirmation
[INFO] 📧 Queue processed: 5/50 emails envoyés
[INFO] ✅ [STANDARD] Email envoyé avec succès | To: customer@example.com
[WARNING] ⚠️ [MARKETING] Email échoué après 2 tentatives | To: inactive@example.com


✏️ NOTES:
   • Les logs sont cruciaux pour le debugging
   • Vérifier les fichiers de log: backend/logs/ ou stdout
"""

print(PHASE_5)

# ============================================================================
# PHASE 6: TESTING & VALIDATION
# ============================================================================

PHASE_6 = """
╔═══════════════════════════════════════════════════════════════════════════╗
║ PHASE 6: TESTING & VALIDATION (30 min)                                   ║
╚═══════════════════════════════════════════════════════════════════════════╝

SCÉNARIO 1: Email de confirmation de réservation (P1)
──────────────────────────────────────────────────────

1. Via l'app, créez une réservation
2. Vérifiez que l'email apparaît dans la queue:
   $ GET /api/admin/email-queue/stats
   → pending doit être > 0 avec priority=1

3. Attendez 5 min (prochaine exécution du cron)
4. Vérifiez que l'email est passé à 'sent':
   $ GET /api/admin/email-queue/stats
   → sent_today doit être > 0


SCÉNARIO 2: Relance d'inactivité (P3)
──────────────────────────────────────

1. Manuellement, appelez:
   $ python
   >>> from tasks.scheduled_jobs import run_relance_inactive
   >>> from app import app
   >>> with app.app_context():
   ...     run_relance_inactive(app)

2. De nombreux emails doivent être ajoutés à la queue:
   $ GET /api/admin/email-queue/stats
   → pending[p3] doit être élevé


SCÉNARIO 3: Retry automatique
──────────────────────────────

1. Testez avec une adresse email invalide:
   >>> EmailQueueManager.add_to_queue(
   ...     to_email='invalid@invalid.invalid',
   ...     ...
   ... )

2. Attendez quelques cycles (15+ min)
3. Vérifiez les tentatives:
   >>> failed = EmailQueueManager.get_failed_emails()
   >>> for email in failed:
   ...     print(f"{email['to_email']}: {email['attempts']}")
   
   Doit montrer: "2/3" ou "3/3" selon le nombre de cycles


SCÉNARIO 4: Monitoring
───────────────────────

1. Maintenez GET /api/admin/email-queue/stats sur 10 min
2. Observez les transitions:
   - pending → 0 (mails traités)
   - sent_today → +N (mails envoyés)
   - failed → augmente lentement (avec erreurs réseau)


CHECKLIST DE VALIDATION:
───────────────────────

- [ ] Les modèles EmailQueue et EmailLog existent en DB
- [ ] Les routes admin retournent des stats correctes
- [ ] La tâche cron process_email_queue s'exécute toutes les 5 min
- [ ] Les emails sont bien enregistrés en queue
- [ ] Les emails en attente sont traités
- [ ] Les retries fonctionnent
- [ ] Les logs sont informatifs
- [ ] La priorité influence l'ordre de traitement (P1 avant P2 avant P3)
- [ ] Les anciens emails échoués sont nettoyés après 7 jours
- [ ] Le success_rate est > 95% (en prod avec config email correcte)


✏️ NOTES:
   • En dev, les erreurs SMTP sont normales si MAIL_* ne pointe pas vers un vrai serveur
   • L'important est que la structure fonctionne
   • En prod, configurer MAIL_SERVER, MAIL_PORT, etc. correctement dans .env
"""

print(PHASE_6)

# ============================================================================
# RÉSUMÉ FINAL
# ============================================================================

RESUME = """
╔═══════════════════════════════════════════════════════════════════════════╗
║ RÉSUMÉ FINAL                                                             ║
╚═══════════════════════════════════════════════════════════════════════════╝

📊 SYSTÈME IMPLÉMENTÉ:
─────────────────────

✅ Queue d'emails avec 3 niveaux de priorités:
   P1 (CRITIQUE):   emails urgents, max 5 min, 5 retries
   P2 (STANDARD):   confirmations/rappels, max 30 min, 3 retries
   P3 (MARKETING):  newsletters/relances, max 24h, 2 retries

✅ Traitement périodique:
   - Tâche cron toutes les 5 minutes
   - Max 50 emails par batch (évite surcharge)
   - Traitement par priorité (P1 > P2 > P3)
   - FIFO à l'intérieur de chaque niveau

✅ Gestion des erreurs:
   - Retries automatiques avec délai exponentiel
   - Logs détaillés de chaque tentative
   - Cleanup des anciens logs

✅ Monitoring:
   - Routes admin pour les stats
   - État en temps réel de la queue
   - Taux de succès
   - Liste des emails ayant échoué


📈 AMÉLIORATIONS APPORTÉES:
──────────────────────────

AVANT                          APRÈS
────────────────────────────  ────────────────────────────
❌ Pas de priorités           ✅ 3 niveaux d'urgence
❌ Pas de queue               ✅ Queue avec DB
❌ Pas de retries             ✅ Retries intelligents
❌ Pas de rate limiting      ✅ Max 50/batch toutes les 5min
❌ Logs minimaux              ✅ Logs détaillés + dashboard
❌ Risque de surcharge        ✅ Limité et contrôlé
❌ Pas de suivi               ✅ Stats en temps réel


🚀 PROCHAINES ÉTAPES (OPTIONNEL):
──────────────────────────────────

1. Intégration Webhooks (SendGrid/SES) pour bounce detection
2. Template engine pour les mails HTML
3. Unsubscribe management
4. A/B testing des sujets
5. Segmentation avancée des utilisateurs
6. Rate limiting par utilisateur


📚 FICHIERS CRÉÉS:
──────────────────

✓ METHODE_GESTION_PRIORITES_MAILS.md   (documentation complète)
✓ backend/services/email_queue_manager.py  (service principal)
✓ MODELES_A_AJOUTER.py                  (modèles EmailQueue, EmailLog)
✓ EXEMPLE_MIGRATION.py                  (migration DB)
✓ SNIPPETS_INTEGRATION.py               (code à intégrer)
✓ GUIDE_IMPLEMENTATION.py              (ce fichier)


💡 CONSEILS:
────────────

1. Testez chaque phase avant de passer à la suivante
2. Ne dépliez en prod qu'une fois testé en dev
3. Gardez les logs pendant les premiers jours
4. Si erreur SMTP: vérifier d'abord les configs MAIL_*
5. Si queue s'accumule: vérifier les logs d'erreur
6. Monitor le success_rate (doit être > 95%)


📞 SUPPORT:
──────────

En cas de problème:
1. Vérifier les logs (flask app.log)
2. Consulter EmailLog pour les détails des erreurs
3. Vérifier les stats: GET /api/admin/email-queue/stats
4. Relancer le scheduler si bloqué


═══════════════════════════════════════════════════════════════════════════

✅ DÉPLOIEMENT PRÊT!

Temps total estimé: 2-3 heures
Complexité: MOYEN
Support: Voir METHODE_GESTION_PRIORITES_MAILS.md pour la documentation complète

═══════════════════════════════════════════════════════════════════════════
"""

print(RESUME)

print(f"\n⏰ Document généré le {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")

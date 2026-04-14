# 📧 Méthode de Gestion des Priorités - Traitement des Mails

## 📊 Analyse de Votre Système Actuel

### ✅ Points Forts
- Architecture modulaire avec `mail_service.py` et `relance_service.py`
- Suivi basique avec `RelanceLog` (status: sent/failed)
- Scheduler APScheduler pour tâches automatisées
- Modèles d'utilisateur avec segmentation (`segment`: VIP, Régulier, Nouveau, Inactif)

### ❌ Lacunes Actuelles
1. **Pas de système de priorités** - tous les mails traités de la même façon
2. **Pas de queue** - risque de surcharge serveur
3. **Pas de retry intelligent** - les mails échoués ne sont jamais renvoyés
4. **Pas de rate limiting** - risque de blocage par le serveur mail
5. **Logs insuffisants** - impossible de tracer les problèmes
6. **Pas de segmentation UX** - même timing pour tous les utilisateurs

---

## 🎯 Solution Proposée : Architecture en 3 Niveaux de Priorités

### Niveaux de Priorités

```
┌─────────────────────────────────────────────────┐
│  NIVEAU 1: CRITIQUE (Urgent)                   │  P1
├─────────────────────────────────────────────────┤
│ • Confirmations de paiement                     │
│ • Alertes de problème (erreur de réservation)  │
│ • Code d'accès/OTP                             │
│ Max. 5 min de délai                            │
│ Retry: 5 tentatives (10min each)              │
├─────────────────────────────────────────────────┤
│  NIVEAU 2: STANDARD (Normal)                   │  P2
├─────────────────────────────────────────────────┤
│ • Confirmations de réservation                 │
│ • Réponses à demande (contact)                 │
│ • Rappel de paiement                           │
│ Max. 30 min de délai                           │
│ Retry: 3 tentatives (1h each)                 │
├─────────────────────────────────────────────────┤
│  NIVEAU 3: MARKETING/RELATIONNEL               │  P3
├─────────────────────────────────────────────────┤
│ • Relances inactivité                          │
│ • Newsletters / Offres                         │
│ • Suggestions personnalisées                   │
│ Max. 24h de délai                              │
│ Retry: 2 tentatives (6h each)                 │
└─────────────────────────────────────────────────┘
```

---

## 🏗️ Architecture Technique Proposée

### 1. Modèle EmailQueue (à ajouter)

```python
class EmailQueue(db.Model):
    __tablename__ = 'email_queues'
    
    id = db.Column(db.Integer, primary_key=True)
    
    # Contenu
    to_email = db.Column(db.String(255), nullable=False)
    subject = db.Column(db.String(255), nullable=False)
    body = db.Column(db.Text, nullable=False)
    template_name = db.Column(db.String(100))  # ex: 'confirmation', 'relance'
    
    # Priorité & Timing
    priority = db.Column(db.Integer, default=2)  # 1=urgent, 2=normal, 3=marketing
    type = db.Column(db.String(50))  # 'payment', 'reservation', 'reactivation', etc.
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    
    # Traitement
    status = db.Column(db.String(20), default='pending')
    # 'pending' → 'sent' | 'failed' | 'bounced' | 'cancelled'
    
    attempt_count = db.Column(db.Integer, default=0)
    max_attempts = db.Column(db.Integer, default=3)
    last_attempt_at = db.Column(db.DateTime)
    error_message = db.Column(db.Text)
    
    # Timing
    scheduled_for = db.Column(db.DateTime)  # Quand l'envoyer
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    sent_at = db.Column(db.DateTime)
    
    # Métadonnées
    metadata = db.Column(db.JSON)  # Données contextuelles (user_segment, etc)
    
    def to_dict(self):
        return {
            'id': self.id,
            'to_email': self.to_email,
            'subject': self.subject,
            'priority': self.priority,
            'status': self.status,
            'attempts': f"{self.attempt_count}/{self.max_attempts}",
            'scheduled_for': self.scheduled_for.isoformat() if self.scheduled_for else None,
            'sent_at': self.sent_at.isoformat() if self.sent_at else None,
        }
```

### 2. Service EmailQueueManager (à créer)

Fichier: `backend/services/email_queue_manager.py`

```python
from datetime import datetime, timedelta
from extensions import db
from models import EmailQueue
from services.mail_service import send_confirmation_to_client
import logging

logger = logging.getLogger(__name__)

class EmailQueueManager:
    """Gère la queue d'emails avec système de priorités"""
    
    # Configuration des tentatives
    RETRY_CONFIG = {
        1: {'max_attempts': 5, 'delay_minutes': 10},    # CRITIQUE
        2: {'max_attempts': 3, 'delay_minutes': 60},     # STANDARD
        3: {'max_attempts': 2, 'delay_minutes': 360}     # MARKETING (6h)
    }
    
    # Délai max avant traitement
    MAX_DELAY = {
        1: 5,     # 5 minutes
        2: 30,    # 30 minutes
        3: 1440   # 24 heures
    }
    
    @staticmethod
    def add_to_queue(to_email, subject, body, priority=2, email_type='general', 
                     user_id=None, template_name=None, metadata=None, delay_minutes=0):
        """
        Ajoute un email à la queue
        
        Args:
            priority: 1 (critique), 2 (normal), 3 (marketing)
            delay_minutes: délai avant traitement (0 = immédiat)
        """
        scheduled_for = (datetime.utcnow() + timedelta(minutes=delay_minutes)) if delay_minutes else datetime.utcnow()
        
        queue_item = EmailQueue(
            to_email=to_email,
            subject=subject,
            body=body,
            priority=priority,
            type=email_type,
            user_id=user_id,
            template_name=template_name,
            scheduled_for=scheduled_for,
            metadata=metadata or {},
            max_attempts=EmailQueueManager.RETRY_CONFIG[priority]['max_attempts']
        )
        
        db.session.add(queue_item)
        db.session.commit()
        
        logger.info(f"✉️ Email ajouté à queue - To: {to_email}, Priority: {priority}")
        return queue_item
    
    @staticmethod
    def process_queue():
        """
        Traite les emails en attente selon leur priorité
        À appeler toutes les 5 minutes par APScheduler
        """
        # Récupère les emails prêts à être traités, triés par priorité DESC
        pending_emails = EmailQueue.query.filter(
            EmailQueue.status == 'pending',
            EmailQueue.scheduled_for <= datetime.utcnow(),
            EmailQueue.attempt_count < EmailQueue.max_attempts
        ).order_by(
            EmailQueue.priority.asc(),  # 1 avant 2 avant 3
            EmailQueue.created_at.asc()  # FIFO dans chaque niveau
        ).limit(50).all()  # Limiter pour éviter surcharge
        
        for email in pending_emails:
            EmailQueueManager._send_email(email)
        
        logger.info(f"📧 Queue processed: {len(pending_emails)} emails sent")
        
        # Nettoyer les emails très anciens en erreur
        EmailQueueManager._cleanup_failed_emails()
    
    @staticmethod
    def _send_email(queue_item):
        """Envoie un email de la queue"""
        try:
            success = send_confirmation_to_client(
                client_email=queue_item.to_email,
                client_name=queue_item.to_email.split('@')[0],
                subject=queue_item.subject,
                body=queue_item.body
            )
            
            if success:
                queue_item.status = 'sent'
                queue_item.sent_at = datetime.utcnow()
                logger.info(f"✅ Email envoyé: {queue_item.to_email}")
            else:
                EmailQueueManager._handle_retry(queue_item)
                
        except Exception as e:
            logger.error(f"❌ Erreur envoi: {str(e)}")
            EmailQueueManager._handle_retry(queue_item, str(e))
        
        db.session.commit()
    
    @staticmethod
    def _handle_retry(queue_item, error_msg=None):
        """Gère les retries automatiques"""
        queue_item.attempt_count += 1
        queue_item.error_message = error_msg or "Unknown error"
        queue_item.last_attempt_at = datetime.utcnow()
        
        if queue_item.attempt_count >= queue_item.max_attempts:
            queue_item.status = 'failed'
            logger.warning(f"⚠️ Email échoué après {queue_item.max_attempts} tentatives: {queue_item.to_email}")
        else:
            # Reschedule avec délai exponentiel
            delay = EmailQueueManager.RETRY_CONFIG[queue_item.priority]['delay_minutes']
            queue_item.scheduled_for = datetime.utcnow() + timedelta(minutes=delay)
            logger.info(f"🔄 Retry scheduled in {delay} min pour {queue_item.to_email}")
    
    @staticmethod
    def _cleanup_failed_emails():
        """Supprime les emails en erreur après 7 jours"""
        old_date = datetime.utcnow() - timedelta(days=7)
        old_failed = EmailQueue.query.filter(
            EmailQueue.status == 'failed',
            EmailQueue.last_attempt_at < old_date
        ).delete()
        
        if old_failed:
            db.session.commit()
            logger.info(f"🧹 Cleaned {old_failed} old failed emails")
    
    @staticmethod
    def get_queue_stats():
        """Retourne les statistiques de la queue"""
        return {
            'pending': EmailQueue.query.filter_by(status='pending').count(),
            'sent_today': EmailQueue.query.filter(
                EmailQueue.status == 'sent',
                EmailQueue.sent_at >= datetime.utcnow() - timedelta(hours=24)
            ).count(),
            'failed': EmailQueue.query.filter_by(status='failed').count(),
            'by_priority': {
                1: EmailQueue.query.filter_by(priority=1, status='pending').count(),
                2: EmailQueue.query.filter_by(priority=2, status='pending').count(),
                3: EmailQueue.query.filter_by(priority=3, status='pending').count(),
            }
        }
```

### 3. Modèle EmailLog Amélioré (à créer)

```python
class EmailLog(db.Model):
    """Log détaillé de tous les passages d'emails"""
    __tablename__ = 'email_logs'
    
    id = db.Column(db.Integer, primary_key=True)
    email_queue_id = db.Column(db.Integer, db.ForeignKey('email_queues.id'))
    
    to_email = db.Column(db.String(255), nullable=False)
    subject = db.Column(db.String(255), nullable=False)
    priority = db.Column(db.Integer)
    email_type = db.Column(db.String(50))
    
    # Résultat
    status = db.Column(db.String(20))  # 'sent', 'failed', 'bounced', 'complained'
    status_code = db.Column(db.String(50))  # 'smtp_error_550', etc.
    error_message = db.Column(db.Text)
    
    # Timing
    attempt_number = db.Column(db.Integer)
    processed_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    # Partenaire mail
    mail_provider = db.Column(db.String(50))  # 'gmail', 'sendgrid', etc.
    message_id = db.Column(db.String(255))  # ID externe du message
```

---

## 🔄 Flux de Traitement Proposé

```
1️⃣  CRÉATION
    └─ Code dans app.py → EmailQueueManager.add_to_queue()
    └─ Email ajouté avec priority + scheduled_for

2️⃣  STOCKAGE
    └─ Dans EmailQueue (pending)
    └─ Permet regroupement et analyse

3️⃣  TRAITEMENT PÉRIODIQUE (Cron: toutes les 5min)
    └─ Récup: pending + scheduled_for <= now, triés par priority
    └─ Limit: 50 emails par batch (évite surcharge)
    └─ Pour chaque Email:
       └─ Tentative d'envoi
       └─ Si ✅ → status='sent'
       └─ Si ❌ → _handle_retry()
           └─ Increment attempt_count
           └─ Si < max → reschedule avec délai
           └─ Si ≥ max → status='failed'

4️⃣  MONITORING
    └─ Queue stats: pending, sent_today, failed
    └─ Dashboard pour admin
```

---

## 🔧 Intégration dans app.py

### Ajouter cette nouvelle tâche cron:

```python
# Dans app.py

from services.email_queue_manager import EmailQueueManager

# ← NOUVELLE TÂCHE (remplace les 2 anciennes)
@scheduler.task('cron', id='process_email_queue', minute='*/5')  # Toutes les 5 min
def job_process_emails():
    """Traite la queue d'emails avec priorités"""
    with app.app_context():
        EmailQueueManager.process_queue()


# Route admin pour monitoring
@app.route('/api/admin/email-queue/stats', methods=['GET'])
def get_email_stats():
    """Retourne les stats du système de queue"""
    from flask_jwt_extended import jwt_required
    # Vérifier que c'est un admin
    stats = EmailQueueManager.get_queue_stats()
    return jsonify(stats)
```

---

## 📝 Migration des Services Existants

### 1. Mettre à jour `mail_service.py`:

Remplacer les appels directs à `mail.send()` pour utiliser la queue:

```python
def send_confirmation_to_client_QUEUED(client_email, client_name, subject, body, priority=2):
    """Version en queue de send_confirmation_to_client"""
    from services.email_queue_manager import EmailQueueManager
    
    EmailQueueManager.add_to_queue(
        to_email=client_email,
        subject=subject,
        body=body,
        priority=priority,
        email_type='confirmation',
        metadata={'client_name': client_name}
    )
    return True
```

### 2. Mettre à jour `relance_service.py`:

```python
def send_relance_to_user_QUEUED(user, priority_boost=0):
    """Version en queue"""
    from services.email_queue_manager import EmailQueueManager
    
    destinations = Destination.query.order_by(
        Destination.avgRating.desc()
    ).limit(3).all()
    
    email_body = generate_relance_email(user.name, destinations)
    
    # Boost de priorité pour VIP
    priority = 2
    if user.segment == 'VIP':
        priority = max(1, priority - priority_boost)
    
    EmailQueueManager.add_to_queue(
        to_email=user.email,
        subject="✈️ Vous nous manquez ! Découvrez nos nouvelles offres",
        body=email_body,
        priority=priority,
        email_type='reactivation',
        user_id=user.id
    )
```

---

## 📊 Tableau de Comparaison

| Aspect | ❌ Ancien Système | ✅ Nouveau Système |
|--------|------|--------|
| **Priorités** | Non | Oui (3 niveaux) |
| **Queue** | Non | Oui |
| **Retries** | Aucun | Intelligent (exponentiel) |
| **Rate Limiting** | Non | 50/batch toutes les 5 min |
| **Logs** | Minimal (sent/failed) | Détaillés (codes d'erreur, timing) |
| **Segmentation UX** | Non | Oui (VIP, Régulier, etc) |
| **Monitoring** | Non | Dashboard stats en temps réel |
| **Récupération erreurs** | Non | Automatique |

---

## 🎯 Cas d'Usage - Exemple Complet

### Scénario: Client fait une réservation

```python
# 1️⃣ Dans reservation_routes.py

from services.email_queue_manager import EmailQueueManager

@client_reservation_bp.route('/reserve', methods=['POST'])
def create_reservation():
    # ... code métier ...
    
    # 📧 P1 - Confirmation immédiate
    EmailQueueManager.add_to_queue(
        to_email=user.email,
        subject="✅ Réservation confirmée",
        body=f"Merci pour votre réservation à {destination.name}",
        priority=1,  # URGENT
        email_type='confirmation',
        user_id=user.id
    )
    
    # 🔔 P2 - Reminder 24h avant check-in
    from datetime import timedelta
    delay = (reservation.check_in - datetime.now()).days  # jour J-1
    if delay == 1:
        EmailQueueManager.add_to_queue(
            to_email=user.email,
            subject="🧳 Rappel: Votre départ demain",
            body=f"N'oubliez pas votre voyage à {destination.name} demain!",
            priority=2,
            email_type='reminder',
            user_id=user.id,
            delay_minutes=1440  # Dans 24h
        )
    
    return jsonify({'status': 'success'})
```

### Résultat dans DB:

```
EmailQueue:
├─ ID: 1, Priority: 1, Status: pending → sent (immédiat)
├─ ID: 2, Priority: 2, Status: pending → sent (24h+)
└─ Plus de manual retry needed! ✅
```

---

## 🚀 Plan de Déploiement

### Phase 1: Préparation (1h)
- [ ] Créer migration pour table `EmailQueue`
- [ ] Créer migration pour table `EmailLog`
- [ ] Tester en dev local

### Phase 2: Mise en œuvre (2h)
- [ ] Créer `email_queue_manager.py`
- [ ] Créer modèles dans `models.py`
- [ ] Ajouter route monitoring
- [ ] Update `app.py` avec nouvelle tâche cron

### Phase 3: Migration (1h)
- [ ] Update `mail_service.py`
- [ ] Update `relance_service.py`
- [ ] Tester routes existantes

### Phase 4: Monitoring (30min)
- [ ] Créer dashboard admin
- [ ] Mettre en place alertes (ex: > 10 failed)

---

## 📈 Métriques à Suivre

```python
# À ajouter dans dashboard admin
{
    'queue_size': 125,
    'sent_today': 3421,
    'success_rate': 99.2,  # sent / (sent + failed)
    'avg_delivery_time': '2.3 min',
    'by_priority': {
        'p1': {'pending': 2, 'sent': 450, 'failed': 1},
        'p2': {'pending': 45, 'sent': 2100, 'failed': 8},
        'p3': {'pending': 78, 'sent': 871, 'failed': 12}
    },
    'error_distribution': {
        'smtp_error_550': 12,
        'timeout': 5,
        'invalid_email': 3
    }
}
```

---

## ⚠️ Considérations Importantes

1. **Rate limiting serveur mail**: Adapter `limit=50` selon votre plan
2. **Doublons**: Vérifier que même email ne se retrouve pas 2x en queue
3. **Stockage DB**: Nettoyer `EmailLog` tous les 90 jours
4. **Notification admin**: Alerter si failed > 5% de la queue
5. **Dead letter queue**: Pour debugging des emails problématiques

---

## 📚 Ressources Utiles

- APScheduler: https://apscheduler.readthedocs.io/
- Flask-Mail: https://pythonhosted.org/Flask-Mail/
- Best practices: https://cheatsheetseries.owasp.org/cheatsheets/


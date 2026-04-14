"""
FICHIER: À ajouter dans backend/models.py

Modèles pour le système de queue d'emails avec priorités
Copiez ces classes et ajoutez-les à la fin du fichier models.py
"""

# ============================================================================
# À COPIER ET AJOUTER DANS models.py (à la fin du fichier)
# ============================================================================

class EmailQueue(db.Model):
    """Queue d'emails avec système de priorités et retries"""
    __tablename__ = 'email_queues'
    
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    
    # ─── CONTENU ───
    to_email = db.Column(db.String(255), nullable=False, index=True)
    subject = db.Column(db.String(255), nullable=False)
    body = db.Column(db.Text, nullable=False)
    template_name = db.Column(db.String(100))  # ex: 'confirmation', 'relance'
    
    # ─── PRIORITÉ & CLASSIFICATION ───
    priority = db.Column(db.Integer, default=2, index=True)
    # 1 = CRITIQUE (urgent, 5min max)
    # 2 = STANDARD (normal, 30min max)
    # 3 = MARKETING (non-urgent, 24h max)
    
    type = db.Column(db.String(50), index=True)
    # 'payment', 'confirmation', 'reminder', 'reactivation', 'general', etc
    
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), index=True)
    
    # ─── TRAITEMENT & RETRY ───
    status = db.Column(db.String(20), default='pending', index=True)
    # 'pending' = en attente | 'sent' = envoyé | 'failed' = échoué
    # 'bounced' = adresse invalide | 'cancelled' = annulé
    
    attempt_count = db.Column(db.Integer, default=0)
    max_attempts = db.Column(db.Integer, default=3)
    last_attempt_at = db.Column(db.DateTime)
    error_message = db.Column(db.Text)
    
    # ─── TIMING ───
    scheduled_for = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    # Quand l'envoyer
    
    created_at = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    sent_at = db.Column(db.DateTime)
    
    # ─── MÉTADONNÉES ───
    metadata = db.Column(db.JSON)  # Données contextuelles
    
    # Relations
    user = db.relationship('User', backref='email_queue_items')
    logs = db.relationship('EmailLog', backref='queue_item', lazy=True, cascade='all, delete-orphan')
    
    def to_dict(self):
        return {
            'id': self.id,
            'to_email': self.to_email,
            'subject': self.subject,
            'priority': self.priority,
            'type': self.type,
            'status': self.status,
            'attempts': f"{self.attempt_count}/{self.max_attempts}",
            'scheduled_for': self.scheduled_for.isoformat() if self.scheduled_for else None,
            'sent_at': self.sent_at.isoformat() if self.sent_at else None,
            'error': self.error_message,
            'created_at': self.created_at.isoformat() if self.created_at else None,
        }
    
    def __repr__(self):
        return f'<EmailQueue {self.to_email} [{self.status}] P{self.priority}>'


class EmailLog(db.Model):
    """Log détaillé de tous les passages d'emails"""
    __tablename__ = 'email_logs'
    
    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    
    # Référence
    email_queue_id = db.Column(db.Integer, db.ForeignKey('email_queues.id'), index=True)
    
    # Détails de l'email
    to_email = db.Column(db.String(255), nullable=False, index=True)
    subject = db.Column(db.String(255), nullable=False)
    priority = db.Column(db.Integer)
    email_type = db.Column(db.String(50), index=True)
    
    # Résultat du traitement
    status = db.Column(db.String(20), index=True)
    # 'sent' = envoyé avec succès
    # 'failed' = erreur SMTP
    # 'bounced' = adresse invalide
    # 'complained' = spam report
    
    status_code = db.Column(db.String(50))
    # Ex: 'smtp_error_550', 'connection_timeout', 'invalid_email'
    
    error_message = db.Column(db.Text)
    
    # Timing
    attempt_number = db.Column(db.Integer)  # 1, 2, 3... tentative
    processed_at = db.Column(db.DateTime, default=datetime.utcnow, index=True)
    
    # Partenaire mail (si utilisation de SendGrid, SES, etc)
    mail_provider = db.Column(db.String(50), default='gmail')
    message_id = db.Column(db.String(255))  # ID externe du message
    
    def to_dict(self):
        return {
            'id': self.id,
            'to_email': self.to_email,
            'subject': self.subject,
            'priority': self.priority,
            'type': self.email_type,
            'status': self.status,
            'error': self.error_message,
            'attempt': self.attempt_number,
            'processed_at': self.processed_at.isoformat() if self.processed_at else None,
        }
    
    def __repr__(self):
        return f'<EmailLog {self.to_email} [{self.status}] attempt#{self.attempt_number}>'


# ============================================================================
# FIN DU CODE À AJOUTER
# ============================================================================

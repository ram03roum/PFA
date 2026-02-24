from extensions import db
from werkzeug.security import check_password_hash, generate_password_hash
from datetime import datetime

# Définition de la table "users"
class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(255), unique=True, nullable=False)
    password = db.Column(db.String(255), nullable=False)
    name = db.Column(db.String(255))
    role = db.Column(db.String(50), default='user')
    # valeurs : 'admin', 'agent', 'client', 'user'
    status = db.Column(db.String(50), default='actif')
    # valeurs : 'actif', 'inactif', 'suspendu'
    phone = db.Column(db.String(20))
    last_login = db.Column(db.DateTime)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    reservations = db.relationship('Reservation', backref='user', lazy=True)
        
    def check_password(self, password):
        return check_password_hash(self.password, password)
    

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'email': self.email,
            'role': self.role,
            'status': self.status,
            'phone': self.phone,
            'last_login': self.last_login.strftime('%Y-%m-%d %H:%M') if self.last_login else None,
            'created_at': self.created_at.strftime('%Y-%m-%d') if self.created_at else None,
        }
    

class Destination(db.Model):
    __tablename__ = 'destinations'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(255), nullable=False)
    country = db.Column(db.String(255))
    continent = db.Column(db.String(255))
    type = db.Column(db.String(255))
    bestSeason = db.Column(db.String(255))
    avgRating = db.Column(db.Float)
    annualVisitors = db.Column(db.Integer)
    unescoSite = db.Column(db.Boolean)
    photoURL = db.Column(db.Text)
    avgCostUSD = db.Column(db.Float)
    Description = db.Column(db.Text)

    reservations = db.relationship('Reservation', backref='destination', lazy=True)

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'country': self.country,
            'image': self.photoURL,
            'price': self.avgCostUSD,
            'category': self.type,
            'season': self.bestSeason,
            'rating': self.avgRating,
            'annual Visitors': self.annualVisitors,
            'unescoSite': self.unescoSite,
            'description': self.Description
        }

class Reservation(db.Model):
    __tablename__ = 'reservations'
    id = db.Column(db.Integer, primary_key=True)
   
    # relations

    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    destination_id = db.Column(db.Integer,db.ForeignKey('destinations.id'),nullable=False)

    # offer_id = db.Column(db.Integer, db.ForeignKey('offers.id'), nullable=False)

    status = db.Column(db.String(50), default='en attente')
    # valeurs : 'en attente', 'confirmée', 'annulée', 'payée'
    check_in = db.Column(db.Date, nullable=False)

    check_out = db.Column(db.Date, nullable=False)
    total_amount = db.Column(db.Float, nullable=False)

    # payment_status = db.Column(db.String(50), default='impayé')
        # valeurs : 'impayé', 'payé', 'remboursé'
    
    notes = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


    def to_dict(self):
        return {
            'id': self.id,
            'client': self.user.name if self.user else None,
            'destination': self.destination.name if self.destination.name else None,
            'dates': f"{self.check_in.strftime('%d %b')} – {self.check_out.strftime('%d %b')}",
            'amount': self.total_amount,
            'status': self.status,
            'created_at': self.created_at.strftime('%Y-%m-%d') if self.created_at else None
        }
    

class ActivityLog(db.Model):
    __tablename__ = 'activity_logs'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    action = db.Column(db.String(255), nullable=False)
    entity_type = db.Column(db.String(100))
        # ex : 'reservation', 'user', 'offer', 'document'
    entity_id = db.Column(db.Integer)
    details = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    user = db.relationship('User', backref=db.backref('logs', lazy=True))
    
    def to_dict(self):
        return {
            'id': self.id,
            'action': self.action,
            'user': self.user.name if self.user else None,
            'entity_type': self.entity_type,
            'created_at': self.created_at.strftime('%H:%M') if self.created_at else None,
        }
    

        
class Favorite(db.Model):
    __tablename__ = 'favorites'  # <--- Ajoute cette ligne exacte
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer)
    destination_id = db.Column(db.Integer)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    
class InteractionLog(db.Model):
    __tablename__ = 'interaction_logs'

    id             = db.Column(db.Integer, primary_key=True, autoincrement=True)
    user_id        = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    destination_id = db.Column(db.Integer, db.ForeignKey('destinations.id'), nullable=False)
    action         = db.Column(
                        db.Enum('view', 'favorite', 'reservation', 'cancel'),
                        nullable=False
                     )
    created_at     = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    # Relations
    user        = db.relationship('User', backref='interaction_logs')
    destination = db.relationship('Destination', backref='interaction_logs')

    # Poids de chaque action pour le moteur
    ACTION_WEIGHTS = {
        'view':        0.5,
        'favorite':    1.0,
        'reservation': 2.0,
        'cancel':     -1.0,
    }

    def get_weight(self):
        """Retourne le poids de cette interaction pour le scoring."""
        return self.ACTION_WEIGHTS.get(self.action, 0)

    def to_dict(self):
        return {
            "id":             self.id,
            "user_id":        self.user_id,
            "destination_id": self.destination_id,
            "action":         self.action,
            "weight":         self.get_weight(),
            "created_at":     self.created_at.isoformat(),
        }

    def __repr__(self):
        return f'<InteractionLog user={self.user_id} action={self.action} dest={self.destination_id}>'


class LlmLog(db.Model):
    __tablename__ = 'llm_logs'

    id            = db.Column(db.Integer, primary_key=True, autoincrement=True)
    user_id       = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=True)
    tokens_used   = db.Column(db.Integer, default=0)
    response_time = db.Column(db.Float, default=0.0)   # en secondes
    success       = db.Column(db.Boolean, default=True)
    created_at    = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    # Relation (nullable car si user supprimé on garde le log)
    user = db.relationship('User', backref='llm_logs')

    def to_dict(self):
        return {
            "id":            self.id,
            "user_id":       self.user_id,
            "tokens_used":   self.tokens_used,
            "response_time": self.response_time,
            "success":       self.success,
            "created_at":    self.created_at.isoformat(),
        }

    def __repr__(self):
        return f'<LlmLog user={self.user_id} success={self.success} tokens={self.tokens_used}>'
    
class RecommendationCache(db.Model):
    __tablename__ = 'recommendation_cache'

    user_id         = db.Column(db.Integer, db.ForeignKey('users.id'), primary_key=True)
    recommendations = db.Column(db.JSON, nullable=False)
    generated_at    = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)
    expires_at      = db.Column(db.DateTime, nullable=False)

    # Relation
    user = db.relationship('User', backref='recommendation_cache')

    def is_expired(self):
        """Vérifie si le cache a expiré."""
        return datetime.utcnow() > self.expires_at

    def to_dict(self):
        return {
            "user_id":         self.user_id,
            "recommendations": self.recommendations,
            "generated_at":    self.generated_at.isoformat(),
            "expires_at":      self.expires_at.isoformat(),
            "is_expired":      self.is_expired(),
        }

    def __repr__(self):
        return f'<RecommendationCache user={self.user_id} expires={self.expires_at}>'
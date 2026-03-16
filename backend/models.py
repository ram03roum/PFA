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


class Conversation(db.Model):
    __tablename__ = 'conversations'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    title = db.Column(db.String(255), default='Nouvelle conversation')
    topic = db.Column(db.String(100))  # 'reservation', 'billing', 'general'
    status = db.Column(db.String(50), default='open')  # 'open', 'closed', 'resolved'
    
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relations
    user = db.relationship('User', backref=db.backref('conversations', lazy=True))
    messages = db.relationship('Message', backref='conversation', lazy=True, cascade='all, delete-orphan')
    summary = db.relationship('ConversationSummary', uselist=False, backref='conversation', cascade='all, delete-orphan')
    
    def to_dict(self):
        return {
            'id': self.id,
            'title': self.title,
            'topic': self.topic,
            'status': self.status,
            'user_id': self.user_id,
            'created_at': self.created_at.strftime('%Y-%m-%d %H:%M'),
            'message_count': len(self.messages)
        }


class Message(db.Model):
    __tablename__ = 'messages'
    id = db.Column(db.Integer, primary_key=True)
    conversation_id = db.Column(db.Integer, db.ForeignKey('conversations.id'), nullable=False)
    sender_type = db.Column(db.String(50), nullable=False)  # 'user' ou 'ai'
    content = db.Column(db.Text, nullable=False)
    
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'id': self.id,
            'conversation_id': self.conversation_id,
            'sender_type': self.sender_type,
            'content': self.content,
            'created_at': self.created_at.strftime('%H:%M')
        }


class ConversationSummary(db.Model):
    __tablename__ = 'conversation_summaries'
    id = db.Column(db.Integer, primary_key=True)
    conversation_id = db.Column(db.Integer, db.ForeignKey('conversations.id'), unique=True, nullable=False)
    summary = db.Column(db.Text)
    key_points = db.Column(db.JSON)  # ['point1', 'point2', ...]
    generated_at = db.Column(db.DateTime, default=datetime.utcnow)
    
    def to_dict(self):
        return {
            'id': self.id,
            'conversation_id': self.conversation_id,
            'summary': self.summary,
            'key_points': self.key_points,
            'generated_at': self.generated_at.strftime('%Y-%m-%d %H:%M')
        }


class Favorite(db.Model):
    __tablename__ = 'favorites'  # <--- Ajoute cette ligne exacte
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer)
    destination_id = db.Column(db.Integer)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)


from extensions import db 
from datetime import datetime

class UserInteraction(db.Model):
    __tablename__ = 'user_interactions'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    destination_id = db.Column(db.Integer, db.ForeignKey('destinations.id'), nullable=False)
    
    action = db.Column(db.String(50), nullable=False)
    # 'view' | 'search' | 'wishlist' | 'rate'
    
    # Optionnel — uniquement rempli si action = 'rate'
    reservation_id = db.Column(db.Integer, db.ForeignKey('reservations.id'), nullable=True)
    duration_seconds = db.Column(db.Integer, nullable=True)
    rating = db.Column(db.Float, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    # Relations
    user = db.relationship('User', backref='interactions')
    destination = db.relationship('Destination', backref='interactions')
    reservation = db.relationship('Reservation', backref='interactions')

    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'destination_id': self.destination_id,
            'action': self.action,
            'reservation_id': self.reservation_id,
            'duration_seconds': self.duration_seconds,
            'rating': self.rating,
            'created_at': self.created_at.isoformat()
        }
    
class UserPreference(db.Model):
    __tablename__ = 'user_preferences'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False, unique=True)
    categories = db.Column(db.String(200), nullable=True)
    budget_min = db.Column(db.Float, nullable=True)
    budget_max = db.Column(db.Float, nullable=True)
    travel_style = db.Column(db.String(50), nullable=True)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    user = db.relationship('User', backref='preference')

    def to_dict(self):
        return {
            'user_id':      self.user_id,
            'categories':   self.categories.split(',') if self.categories else [],
            'budget_min':   self.budget_min,
            'budget_max':   self.budget_max,
            'travel_style': self.travel_style
        }
    

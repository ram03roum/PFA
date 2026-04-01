# services/data_collector.py
from flask import views

from routes import favorites
from models import db, User, Destination, Reservation, Favorite, InteractionLog

class DataCollector:

    def get_user_data(self, user_id):
        """
        Collecte toutes les données d'un utilisateur
        depuis MySQL pour alimenter le moteur.
        """
        user_id = int(user_id)

        # ── Réservations confirmées (signal fort) ─────────────────
        reservations = InteractionLog.query.filter_by(
            user_id=user_id,
            action='reservation'
        ).all()

        # ── Favoris (signal moyen) ─────────────────────────────────
        favorites = InteractionLog.query.filter_by(
            action='favorite',
            user_id=user_id
        ).all()

        # ── Vues depuis interaction_logs (signal faible) ───────────
        views = InteractionLog.query.filter_by(
            user_id=user_id,
            action='view'
        ).order_by(
            InteractionLog.created_at.desc()
        ).limit(20).all()
        
        
        # ── DEBUG ─────────────────────────────────────────────────
        print(f"DEBUG user_id: {user_id}")
        print(f"DEBUG type user_id: {type(user_id)}")
        print(f"DEBUG reservations count: {len(reservations)}")
        print(f"DEBUG favorites count: {len(favorites)}")
        print(f"DEBUG views count: {len(views)}")
    # ─────────────────────────────────────────────────────────
        # ── IDs déjà réservés (à exclure des recommandations) ──────
        reserved_ids = [r.destination_id for r in reservations]

        # ── Détection cold start ───────────────────────────────────
        total_interactions = len(reservations) + len(favorites)
        is_new_user = total_interactions == 0

        # ── Calcul des poids dynamiques ────────────────────────────
        if total_interactions == 0:
            alpha = 1.0   # Content-Based 100%
            beta  = 0.0   # User-Based désactivé
        elif total_interactions < 5:
            alpha = 0.8   # Content-Based dominant
            beta  = 0.2
        else:
            alpha = 0.5   # Hybride équilibré
            beta  = 0.5

        return {
            "user_id":      user_id,
            "reservations": reservations,
            "favorites":    favorites,
            "views":        views,
            "reserved_ids": reserved_ids,
            "is_new_user":  is_new_user,
            "alpha":        alpha,   # poids Content-Based
            "beta":         beta,    # poids User-Based
            "total_interactions": total_interactions
        }



    def get_all_destinations(self, exclude_ids=[]):
        """
        Retourne toutes les destinations
        sauf celles déjà réservées par l'user.
        """
        if exclude_ids:
            return Destination.query.filter(
                ~Destination.id.in_(exclude_ids)
            ).all()
        return Destination.query.all()



    def get_popular_destinations(self, limit=12):
        """
        Fallback pour les nouveaux utilisateurs.
        Retourne les destinations les mieux notées.
        """
        return Destination.query.order_by(
            Destination.avgRating.desc(),
            Destination.annualVisitors.desc()
        ).limit(limit).all()

    def log_interaction(self, user_id, destination_id, action):
        """
        Enregistre une interaction utilisateur
        dans interaction_logs.
        Appelé depuis les routes Flask.
        """
        # Évite les doublons de views rapprochés (même user, même dest, même heure)
        if action == 'view':
            from datetime import datetime, timedelta
            recent = InteractionLog.query.filter_by(
                user_id=user_id,
                destination_id=destination_id,
                action='view'
            ).filter(
                InteractionLog.created_at >= datetime.utcnow() - timedelta(hours=1)
            ).first()

            if recent:
                return  # déjà loggé dans la dernière heure, on ignore

        log = InteractionLog(
            user_id=user_id,
            destination_id=destination_id,
            action=action
        )
        db.session.add(log)
        db.session.commit()

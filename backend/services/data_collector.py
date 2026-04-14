# services/data_collector.py
from datetime import datetime, timedelta, timezone
from models import db, Destination, InteractionLog

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
        
        # ── Annulations (signal négatif -1.5) ─────────────────────
        cancels = InteractionLog.query.filter_by(
        user_id=user_id,
        action='cancel'
        ).all()
        # ── IDs annulés (destinations à pénaliser) ────────────────
        cancelled_ids = [c.destination_id for c in cancels]
        
        
        # ── DEBUG ─────────────────────────────────────────────────
        print(f"DEBUG user_id: {user_id}")
        print(f"DEBUG type user_id: {type(user_id)}")
        print(f"DEBUG reservations count: {len(reservations)}")
        print(f"DEBUG favorites count: {len(favorites)}")
        print(f"DEBUG views count: {len(views)}")
    # ─────────────────────────────────────────────────────────
        # ── IDs déjà réservés (à exclure des recommandations) ──────
        reserved_ids = [r.destination_id for r in reservations
                        if r.destination_id not in cancelled_ids
]
        # ── Détection cold start ───────────────────────────────────
        total_interactions = len(reservations) + len(favorites) + len(views)
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
            "cancels":          cancels,          # ← nouveau
            "reserved_ids": reserved_ids,
            "cancelled_ids":    cancelled_ids,    # ← nouveau
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
        # Évite les doublons rapprochés (même user, même dest, même action)
        recent = InteractionLog.query.filter_by(
            user_id=user_id,
            destination_id=destination_id,
            action=action
        ).filter(
            InteractionLog.created_at >= datetime.now(timezone.utc) - timedelta(hours=2)
        ).first()

        if recent:
            return  # déjà loggé récemment, on ignore

        log = InteractionLog(
            user_id=user_id,
            destination_id=destination_id,
            action=action
        )
        db.session.add(log)
        db.session.commit()

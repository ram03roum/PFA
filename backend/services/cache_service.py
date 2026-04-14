# services/cache_service.py
import json
from datetime import datetime, timedelta
from models import RecommendationCache
from extensions import db


class CacheService:

    CACHE_DURATION_HOURS = 6

    def get(self, user_id):
        """
        Retourne les recommandations en cache si valides.
        Retourne None si cache inexistant ou expiré.
        """
        try:
            cache = RecommendationCache.query.get(int(user_id))

            if not cache:
                print(f"DEBUG Cache: aucun cache pour user {user_id}")
                return None

            if cache.is_expired():
                print(f"DEBUG Cache: expiré pour user {user_id}")
                db.session.delete(cache)
                db.session.commit()
                return None

            print(f"DEBUG Cache: hit pour user {user_id}")
            return cache.recommendations

        except Exception as e:
            db.session.rollback()
            print(f"DEBUG Cache get erreur: {e}")
            return None

    def set(self, user_id, recommendations):
        """
        Stocke les recommandations en cache pour 6h.
        """
        try:
            expires = datetime.utcnow() + timedelta(hours=self.CACHE_DURATION_HOURS)

            cache = RecommendationCache.query.get(int(user_id))

            if cache:
                cache.recommendations = recommendations
                cache.generated_at    = datetime.utcnow()
                cache.expires_at      = expires
            else:
                cache = RecommendationCache(
                    user_id=int(user_id),
                    recommendations=recommendations,
                    generated_at=datetime.utcnow(),
                    expires_at=expires
                )
                db.session.add(cache)

            db.session.commit()
            print(f"DEBUG Cache: stocké pour user {user_id} jusqu'à {expires}")

        except Exception as e:
            print(f"DEBUG Cache set erreur: {e}")

    def invalidate(self, user_id):
        """
        Supprime le cache d'un utilisateur.
        Appelé après une interaction forte (favori, réservation).
        """
        try:
            cache = RecommendationCache.query.get(int(user_id))
            if cache:
                db.session.delete(cache)
                db.session.commit()
                print(f"DEBUG Cache: invalidé pour user {user_id}")
        except Exception as e:
            db.session.rollback()  # ← empêche la session de rester sale
            print(f"DEBUG Cache invalidate erreur: {e}")
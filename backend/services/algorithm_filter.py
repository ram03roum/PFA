# services/algorithm_filter.py
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.neighbors import NearestNeighbors
from models import Destination, InteractionLog, Favorite, Reservation

class AlgorithmFilter:

    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            max_features=500,   # limite le vocabulaire aux 500 mots les plus importants
            stop_words=None,    # pas de stop words car on a des noms propres
            ngram_range=(1, 2)  # prend en compte les bigrammes ex: "mer méditerranée"
        )

    # ═══════════════════════════════════════════════════════════
    # CONTENT-BASED
    # ═══════════════════════════════════════════════════════════

    def destination_to_text(self, dest):
        """
        Transforme une destination en texte pour TF-IDF.
        Utilise les colonnes existantes de ta table destinations.
        """
        parts = []

        if dest.type:        parts.append(dest.type)
        if dest.country:     parts.append(dest.country)
        if dest.continent:   parts.append(dest.continent)
        if dest.bestSeason:  parts.append(dest.bestSeason)
        if dest.Description: parts.append(dest.Description[:300])

        # UNESCO = signal culturel fort
        if dest.unescoSite:
            parts.append("culture patrimoine historique unesco")

        # Budget converti en texte
        if dest.avgCostUSD:
            if dest.avgCostUSD < 500:
                parts.append("budget economique pas cher accessible")
            elif dest.avgCostUSD < 1500:
                parts.append("budget moyen standard")
            else:
                parts.append("luxe haut de gamme premium")

        return ' '.join(parts)

    def build_user_profile(self, user_data):
        """
        Construit le profil textuel de l'utilisateur
        en pondérant ses interactions.

        Poids :
        - Réservation confirmée = x3 (signal très fort)
        - Favori                = x2 (signal fort)
        - Vue                   = x1 (signal faible)
        """
        profile_tokens = []

        # Réservations → poids 3
        for resa in user_data['reservations']:
            dest = Destination.query.get(resa.destination_id)
            if dest:
                text = self.destination_to_text(dest)
                profile_tokens.extend([text] * 3)

        # Favoris → poids 2
        for fav in user_data['favorites']:
            dest = Destination.query.get(fav.destination_id)
            if dest:
                text = self.destination_to_text(dest)
                profile_tokens.extend([text] * 2)

        # Vues → poids 1
        for view in user_data['views']:
            dest = Destination.query.get(view.destination_id)
            if dest:
                text = self.destination_to_text(dest)
                profile_tokens.append(text)

        return ' '.join(profile_tokens)

    def compute_content_based_scores(self, user_data, all_destinations):
        """
        Calcule la similarité cosinus entre le profil
        de l'utilisateur et chaque destination.
        Retourne un dict {destination_id: score}
        """
        # Cas cold start → pas de profil
        if user_data['is_new_user']:
            return {}

        user_profile = self.build_user_profile(user_data)

        # Corpus = profil user + toutes les destinations
        destination_texts = [
            self.destination_to_text(dest) for dest in all_destinations
        ]
        corpus = [user_profile] + destination_texts

        # Vectorisation TF-IDF
        try:
            tfidf_matrix = self.vectorizer.fit_transform(corpus)
        except ValueError:
            return {}

        # Similarité cosinus entre profil user (index 0) et destinations
        user_vector  = tfidf_matrix[0]
        dest_vectors = tfidf_matrix[1:]
        similarities = cosine_similarity(user_vector, dest_vectors)[0]

        # Retourne un dict {destination_id: score}
        return {
            dest.id: float(similarities[i])
            for i, dest in enumerate(all_destinations)
        }

    # ═══════════════════════════════════════════════════════════
    # USER-BASED (KNN)
    # ═══════════════════════════════════════════════════════════

    def build_interaction_matrix(self):
        """
        Construit la matrice users × destinations
        depuis interaction_logs + favorites + reservations.
        Retourne un DataFrame pandas.

        Exemple :
                  dest_1  dest_2  dest_3
        user_1      2.0     0.0     1.0
        user_2      0.0     3.0     0.5
        user_3      1.0     2.0     0.0
        """
        rows = []

        # Depuis interaction_logs
        logs = InteractionLog.query.all()
        for log in logs:
            rows.append({
                "user_id":        log.user_id,
                "destination_id": log.destination_id,
                "weight":         log.get_weight()
            })

        # Depuis favorites
        favs = Favorite.query.all()
        for fav in favs:
            rows.append({
                "user_id":        fav.user_id,
                "destination_id": fav.destination_id,
                "weight":         1.0
            })

        # Depuis reservations confirmées
        resas = Reservation.query.filter_by(status='confirmed').all()
        for resa in resas:
            rows.append({
                "user_id":        resa.user_id,
                "destination_id": resa.destination_id,
                "weight":         2.0
            })

        if not rows:
            return pd.DataFrame()

        df = pd.DataFrame(rows)

        # Agréger les poids (un user peut avoir plusieurs interactions
        # avec la même destination)
        df = df.groupby(
            ['user_id', 'destination_id']
        )['weight'].sum().reset_index()

        # Pivoter en matrice users × destinations
        matrix = df.pivot(
            index='user_id',
            columns='destination_id',
            values='weight'
        ).fillna(0)

        return matrix

    def compute_user_based_scores(self, user_id, all_destinations):
        """
        Trouve les K utilisateurs les plus similaires
        et retourne les scores des destinations qu'ils ont aimées.
        Retourne un dict {destination_id: score}
        """
        matrix = self.build_interaction_matrix()

        # Pas assez de données
        if matrix.empty or user_id not in matrix.index:
            return {}

        # Nombre de voisins (min 2, max 5)
        n_users    = len(matrix)
        k_neighbors = min(5, max(2, n_users - 1))

        # KNN
        knn = NearestNeighbors(
            n_neighbors=k_neighbors,
            metric='cosine',
            algorithm='brute'
        )
        knn.fit(matrix.values)

        # Vecteur du user actif
        user_index  = matrix.index.get_loc(user_id)
        user_vector = matrix.values[user_index].reshape(1, -1)

        # Trouver les K voisins
        distances, indices = knn.kneighbors(user_vector)

        scores = {}
        for i, neighbor_index in enumerate(indices[0]):
            similarity = 1 - distances[0][i]   # distance cosinus → similarité
            neighbor_id = matrix.index[neighbor_index]

            if neighbor_id == user_id:
                continue

            # Récupère les destinations aimées par ce voisin
            neighbor_row = matrix.iloc[neighbor_index]
            for dest_id, weight in neighbor_row.items():
                if weight > 0:
                    if dest_id not in scores:
                        scores[dest_id] = 0
                    scores[dest_id] += similarity * weight

        # Normaliser entre 0 et 1
        if scores:
            max_score = max(scores.values())
            if max_score > 0:
                scores = {k: v / max_score for k, v in scores.items()}

        return scores

    # ═══════════════════════════════════════════════════════════
    # HYBRIDE — fusion Content-Based + User-Based
    # ═══════════════════════════════════════════════════════════

    def filter_candidates(self, user_data, all_destinations, top_n=15):
      alpha = user_data['alpha']
      beta  = user_data['beta']
      user_id = user_data['user_id']

    # ── Cas cold start ─────────────────────────────────────────
      if user_data['is_new_user']:
        return self._get_popular_candidates(all_destinations, top_n)

    # ── Content-Based scores ───────────────────────────────────
      cb_scores = self.compute_content_based_scores(user_data, all_destinations)

    # ── User-Based scores ──────────────────────────────────────
      ub_scores = {}
      if beta > 0:
        ub_scores = self.compute_user_based_scores(user_id, all_destinations)
    # ── Si les deux scores sont vides → fallback populaire ────
      if not cb_scores and not ub_scores:
        print("DEBUG: scores vides → fallback populaire")
        return self._get_popular_candidates(all_destinations, top_n)

    # ── Combinaison des scores ─────────────────────────────────
      final_scores = {}
      for dest in all_destinations:
        cb = cb_scores.get(dest.id, 0)
        ub = ub_scores.get(dest.id, 0)
        final_scores[dest.id] = alpha * cb + beta * ub

    # ── Tri et retour des top_n ────────────────────────────────
      sorted_ids = sorted(
        final_scores,
        key=lambda x: final_scores[x],
        reverse=True
        )[:top_n]

      dest_dict  = {dest.id: dest for dest in all_destinations}
      candidates = []

      for dest_id in sorted_ids:
        if dest_id in dest_dict:
            candidates.append({
                "destination": dest_dict[dest_id],
                "algo_score":  round(final_scores[dest_id], 4),
                "cb_score":    round(cb_scores.get(dest_id, 0), 4),
                "ub_score":    round(ub_scores.get(dest_id, 0), 4),
            })

    # ── Si résultat vide → fallback ───────────────────────────
      if not candidates:
        return self._get_popular_candidates(all_destinations, top_n)

      return candidates
  
    def _get_popular_candidates(self, destinations, n):  # ← 4 espaces
        sorted_dest = sorted(
            destinations,
            key=lambda d: (d.avgRating or 0, d.annualVisitors or 0),
            reverse=True
        )
        return [
            {
                "destination": d,
                "algo_score":  round((d.avgRating or 0) / 5, 4),
                "cb_score":    0,
                "ub_score":    0,
            }
            for d in sorted_dest[:n]
        ]
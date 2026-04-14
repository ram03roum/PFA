# 📚 Documentation - Système de Recommandations avec LLM

## Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture du système](#architecture-du-système)
3. [Flux de recommandations](#flux-de-recommandations)
4. [Services composants](#services-composants)
5. [Algorithme de filtrage](#algorithme-de-filtrage)
6. [Intégration LLM](#intégration-llm)
7. [Cache et performance](#cache-et-performance)
8. [Monitoring et logs](#monitoring-et-logs)
9. [API Routes](#api-routes)

---

## Vue d'ensemble

Votre système de recommandations est un **moteur hybride sophistiqué** qui combine :

- **Algorithme traditionnel** : Filtrage collaboratif et basé sur le contenu
- **IA (LLM)** : Ranking final et explications personnalisées
- **Cache intelligent** : Performance et scalabilité
- **Logging complet** : Monitoring et optimisation

**Objectif** : Recommander les 10 destinations les plus pertinentes pour chaque utilisateur connecté.

---

## Architecture du système

```
┌──────────────────────────────────────────────────────┐
│               FRONTEND (Angular)                     │
│  Page d'accueil → GET /api/recommendations           │
└──────────────────┬───────────────────────────────────┘
                   │
                   ▼
         ┌─────────────────────┐
         │  BACKEND (Flask)    │
         │  routes/recommendations.py │
         └──────────┬──────────┘
                    │
        ┌───────────▼──────────────────────┐
        │  PersonalizationEngine           │
        │  services/personnalisation.py    │
        └───────────┬──────────────────────┘
                    │
    ┌───────────────┴──────────────┐
    │                              │
    ▼                              ▼
┌─────────────┐           ┌─────────────┐
│ DataCollector│           │ CacheService│
│ (données)   │           │ (cache)     │
└─────┬───────┘           └─────┬───────┘
      │                        │
      ▼                        ▼
┌─────────────┐           ┌─────────────┐
│AlgorithmFilter│         │Recommendation│
│ (filtrage)   │         │   Cache      │
└─────┬───────┘         │   (BD)       │
      │                └─────────────┘
      ▼
┌─────────────┐           ┌─────────────┐
│PromptBuilder│           │  LLMService │
│ (prompt)    │           │   (Groq)    │
└─────┬───────┘           └─────┬───────┘
      │                        │
      └────────────────────────┘
               ▼
         ┌─────────────┐
         │   GROQ API  │
         │ Llama-3.3   │
         │ 70B-versatile│
         └─────────────┘
```

---

## Flux de recommandations

### 1️⃣ Déclenchement

**Utilisateur arrive sur la page d'accueil** → Angular appelle automatiquement :

```javascript
// frontend/src/app/services/dashboard.service.ts
getRecommendations() {
  return this.http.get('/api/recommendations');
}
```

### 2️⃣ Vérification cache

```python
# services/personnalisation.py
def get_recommendations(self, user_id, force_refresh=False):
    if not force_refresh:
        cached = self.cache.get(user_id)
        if cached:
            return {"source": "cache", "data": cached}
```

### 3️⃣ Collecte des données utilisateur

```python
# services/data_collector.py
def get_user_data(self, user_id):
    # Récupère réservations, favoris, vues depuis interaction_logs
    reservations = InteractionLog.query.filter_by(
        user_id=user_id, action='reservation'
    ).all()

    favorites = InteractionLog.query.filter_by(
        user_id=user_id, action='favorite'
    ).all()

    views = InteractionLog.query.filter_by(
        user_id=user_id, action='view'
    ).order_by(InteractionLog.created_at.desc()).limit(20).all()

    # Calcule les poids alpha/beta selon le nombre d'interactions
    total_interactions = len(reservations) + len(favorites)

    if total_interactions == 0:
        alpha, beta = 1.0, 0.0  # 100% Content-Based
    elif total_interactions < 5:
        alpha, beta = 0.8, 0.2  # Content-Based dominant
    else:
        alpha, beta = 0.5, 0.5  # Hybride équilibré

    return {
        "user_id": user_id,
        "reservations": reservations,
        "favorites": favorites,
        "views": views,
        "reserved_ids": [r.destination_id for r in reservations],
        "is_new_user": total_interactions == 0,
        "alpha": alpha,
        "beta": beta,
        "total_interactions": total_interactions
    }
```

### 4️⃣ Filtrage algorithmique (30 candidates)

```python
# services/algorithm_filter.py
def filter_candidates(self, user_data, all_destinations, top_n=30):
    # Combine Content-Based + User-Based
    content_scores = self.compute_content_based_scores(user_data, all_destinations)
    user_scores = self.compute_user_based_scores(user_data['user_id'], all_destinations)

    candidates = []
    for dest in all_destinations:
        # Score hybride = alpha × content + beta × user
        content_score = content_scores.get(dest.id, 0)
        user_score = user_scores.get(dest.id, 0)
        final_score = user_data['alpha'] * content_score + user_data['beta'] * user_score

        candidates.append({
            'destination': dest,
            'algo_score': final_score
        })

    # Retourne les top 30 triées par score décroissant
    return sorted(candidates, key=lambda x: x['algo_score'], reverse=True)[:top_n]
```

### 5️⃣ Construction du prompt pour LLM

```python
# services/prompt_builder.py
def build_ranking_prompt(self, user_data, candidates):
    # Historique utilisateur
    past_destinations = []
    for resa in user_data['reservations']:
        dest = Destination.query.get(resa.destination_id)
        if dest:
            past_destinations.append(f"{dest.name} ({dest.country})")

    # Formatage des 30 candidates
    candidates_text = ""
    for i, cand in enumerate(candidates):
        dest = cand['destination']
        candidates_text += f"""
{i+1}. ID:{dest.id} | {dest.name}
   Pays: {dest.country} ({dest.continent})
   Type: {dest.type}
   Coût/pers: {dest.avgCostUSD}$
   Saison: {dest.bestSeason}
   Note: {dest.avgRating}/5
   UNESCO: {'Oui' if dest.unescoSite else 'Non'}
   Score algo: {cand['algo_score']}
"""

    # Prompt complet envoyé à Groq
    prompt = f"""
Tu es un expert en recommandation de voyages personnalises.
Analyse le profil utilisateur et recommande les 10 meilleures destinations.

PROFIL UTILISATEUR:
- Destinations réservées: {', '.join(past_destinations) or 'Aucune'}
- Destinations favorites: {', '.join(favorites_names) or 'Aucune'}
- Nombre interactions: {user_data['total_interactions']}

DESTINATIONS CANDIDATES:
{candidates_text}

TA TACHE:
1. Analyse le profil de cet utilisateur
2. Sélectionne les 10 destinations les plus adaptées
3. Pour chacune génère une explication courte (2 phrases max) en français

RÈGLES:
- Tu DOIS retourner EXACTEMENT 10 destinations
- Réponds UNIQUEMENT en JSON valide
- L'explication doit être personnalisée

FORMAT OBLIGATOIRE:
{{
  "recommendations": [
    {{
      "destination_id": <id>,
      "rank": <1-10>,
      "llm_score": <6.0-10.0>,
      "explanation": "<2 phrases en français>"
    }}
  ]
}}
"""
    return prompt
```

### 6️⃣ Appel à Groq LLM

```python
# services/llm_service.py
def get_recommendations(self, prompt, user_id):
    response = self.client.chat.completions.create(
        model="llama-3.3-70b-versatile",
        messages=[
            {
                "role": "system",
                "content": "Tu es un moteur de recommandation de voyages. Tu réponds TOUJOURS en JSON valide uniquement."
            },
            {
                "role": "user",
                "content": prompt
            }
        ],
        temperature=0.3,  # Faible pour cohérence
        max_tokens=2000,
        response_format={"type": "json_object"}
    )

    # Log les métriques
    self._log(user_id, tokens_used=response.usage.total_tokens, ...)

    # Parse JSON
    result = json.loads(response.choices[0].message.content)
    return result.get('recommendations', [])
```

### 7️⃣ Fusion des scores

```python
# services/personnalisation.py
def _merge_results(self, candidates, llm_results):
    candidates_dict = {c['destination'].id: c for c in candidates}

    merged = []
    for rec in llm_results:
        dest_id = rec.get('destination_id')
        dest = candidates_dict[dest_id]['destination']
        algo_score = candidates_dict[dest_id]['algo_score']
        llm_score = rec.get('llm_score', 0)

        # Score final = 0.4 × algo + 0.6 × llm
        final_score = round(0.4 * algo_score + 0.6 * (llm_score / 10), 4)

        merged.append({
            "destination_id": dest_id,
            "name": dest.name,
            "country": dest.country,
            "algo_score": round(algo_score, 4),
            "llm_score": round(llm_score, 4),
            "final_score": final_score,
            "explanation": rec.get('explanation', ''),
            "rank": rec.get('rank', 0)
        })

    return sorted(merged, key=lambda x: x['final_score'], reverse=True)
```

### 8️⃣ Mise en cache

```python
# services/cache_service.py
def set(self, user_id, recommendations):
    expires = datetime.utcnow() + timedelta(hours=6)  # Cache 6h

    cache = RecommendationCache(
        user_id=user_id,
        recommendations=recommendations,
        expires_at=expires
    )
    db.session.add(cache)
    db.session.commit()
```

### 9️⃣ Réponse au frontend

```json
{
  "source": "hybrid",
  "data": [
    {
      "destination_id": 15,
      "name": "Barcelona",
      "country": "Spain",
      "algo_score": 0.85,
      "llm_score": 9.2,
      "final_score": 0.552,
      "explanation": "Barcelona est parfaite pour vous qui aimez les destinations culturelles européennes. Avec son architecture unique et sa gastronomie, elle correspond à vos précédentes réservations.",
      "rank": 1
    }
    // ... 9 autres destinations
  ]
}
```

---

## Services composants

### 1️⃣ PersonalizationEngine (services/personnalisation.py)

**Responsabilités :**

- Orchestrateur principal du système
- Gère le flux complet de recommandations
- Combine algorithme + LLM + cache
- Gère les fallbacks

**Méthodes clés :**

- `get_recommendations(user_id, force_refresh=False)` : Point d'entrée
- `_merge_results(candidates, llm_results)` : Fusion des scores
- `_format_algo_fallback(candidates)` : Fallback sans LLM

### 2️⃣ DataCollector (services/data_collector.py)

**Responsabilités :**

- Collecte toutes les données utilisateur depuis la BD
- Calcule les poids alpha/beta selon le profil utilisateur
- Gère les interactions (logs)

**Méthodes clés :**

- `get_user_data(user_id)` : Profil complet utilisateur
- `get_all_destinations(exclude_ids=[])` : Destinations disponibles
- `get_popular_destinations(limit=12)` : Fallback nouveaux utilisateurs
- `log_interaction(user_id, destination_id, action)` : Enregistrement

### 3️⃣ AlgorithmFilter (services/algorithm_filter.py)

**Responsabilités :**

- Filtrage algorithmique hybride
- Content-Based Filtering (TF-IDF + Cosine Similarity)
- User-Based Collaborative Filtering (KNN)

**Méthodes clés :**

- `filter_candidates(user_data, all_destinations, top_n=30)` : Pipeline complet
- `compute_content_based_scores()` : Similarité texte
- `compute_user_based_scores()` : Similarité utilisateurs

### 4️⃣ PromptBuilder (services/prompt_builder.py)

**Responsabilités :**

- Construction des prompts pour le LLM
- Formatage des données utilisateur et candidates
- Gestion du profil budget

**Méthodes clés :**

- `build_ranking_prompt(user_data, candidates)` : Prompt principal
- `_detect_budget_profile(user_data)` : Analyse budget

### 5️⃣ LLMService (services/llm_service.py)

**Responsabilités :**

- Interface avec Groq API
- Gestion des appels LLM
- Logging des métriques

**Méthodes clés :**

- `get_recommendations(prompt, user_id)` : Appel principal
- `_log(user_id, tokens_used, response_time, success)` : Métriques

### 6️⃣ CacheService (services/cache_service.py)

**Responsabilités :**

- Cache des recommandations (6h)
- Invalidation intelligente
- Performance

**Méthodes clés :**

- `get(user_id)` : Récupération cache
- `set(user_id, recommendations)` : Stockage
- `invalidate(user_id)` : Suppression

---

## Algorithme de filtrage

### Content-Based Filtering

**Principe :** Recommande des destinations similaires à celles que l'utilisateur a aimées.

**Étapes :**

1. **Construction profil utilisateur :**

   ```python
   # Pondération par type d'interaction
   reservations × 3  # Signal fort
   favorites × 2     # Signal moyen
   views × 1         # Signal faible
   ```

2. **Vectorisation TF-IDF :**

   ```python
   # Transforme destinations en texte
   def destination_to_text(dest):
       return f"{dest.type} {dest.country} {dest.continent} {dest.bestSeason} {dest.Description[:300]}"

   # Vectorisation
   vectorizer = TfidfVectorizer(max_features=500, ngram_range=(1,2))
   tfidf_matrix = vectorizer.fit_transform(corpus)
   ```

3. **Similarité cosinus :**
   ```python
   user_vector = tfidf_matrix[0]  # Profil utilisateur
   dest_vectors = tfidf_matrix[1:] # Destinations
   similarities = cosine_similarity(user_vector, dest_vectors)[0]
   ```

### User-Based Collaborative Filtering

**Principe :** "Les utilisateurs similaires aiment des destinations similaires"

**Étapes :**

1. **Matrice interactions :**

   ```python
   # Users × Destinations matrix
   # Valeurs = poids des interactions
   matrix = df.pivot(
       index='user_id',
       columns='destination_id',
       values='weight'
   ).fillna(0)
   ```

2. **KNN (K-Nearest Neighbors) :**

   ```python
   # Trouve K utilisateurs similaires
   knn = NearestNeighbors(n_neighbors=k, metric='cosine')
   knn.fit(matrix)

   # Destinations aimées par les voisins
   neighbors_destinations = matrix.iloc[neighbors_indices].sum(axis=0)
   ```

### Score hybride

**Formule finale :**

```
score_final = α × score_content + β × score_user

Où :
- α = poids Content-Based (0.5-1.0)
- β = poids User-Based (0.0-0.5)
- Ajusté selon nombre d'interactions utilisateur
```

---

## Intégration LLM

### Modèle utilisé : Llama-3.3-70B-Versatile

**Configuration :**

```python
model = "llama-3.3-70b-versatile"
temperature = 0.3          # Cohérence
max_tokens = 2000          # Suffisant pour 10 recommandations
response_format = {"type": "json_object"}  # Force JSON
```

### Prompt engineering

**Structure du prompt :**

1. **Rôle système :** "Tu es un expert en recommandation de voyages personnalises"
2. **Profil utilisateur :** Historique réservations/favoris
3. **Candidates :** 30 destinations avec scores algo
4. **Instructions :** "Sélectionne 10 destinations + explications personnalisées"
5. **Format JSON strict :** Structure imposée pour parsing

### Gestion des erreurs

```python
try:
    response = groq_client.chat.completions.create(...)
    result = json.loads(response.choices[0].message.content)
    return result.get('recommendations', [])
except json.JSONDecodeError:
    # LLM a retourné du texte invalide
    return None  # → Fallback algorithme
except Exception as e:
    # Erreur réseau/API
    return None  # → Fallback algorithme
```

### Métriques trackées

**Table `llm_logs` :**

```python
class LlmLog(db.Model):
    user_id = db.Column(db.Integer)
    tokens_used = db.Column(db.Integer)
    response_time = db.Column(db.Float)  # secondes
    success = db.Column(db.Boolean)
    created_at = db.Column(db.DateTime)
```

---

## Cache et performance

### Stratégie de cache

**Durée :** 6 heures
**Invalide quand :**

- Nouvelle réservation
- Nouveau favori
- Action 'cancel'
- `force_refresh=true` (paramètre URL)

### Avantages

- **Performance :** Évite recalculs coûteux
- **Coût :** Réduit appels Groq (cher)
- **UX :** Recommandations instantanées

### Implémentation

```python
class RecommendationCache(db.Model):
    user_id = db.Column(db.Integer, primary_key=True)
    recommendations = db.Column(db.JSON)
    generated_at = db.Column(db.DateTime)
    expires_at = db.Column(db.DateTime)

    def is_expired(self):
        return datetime.utcnow() > self.expires_at
```

---

## Monitoring et logs

### Métriques LLM

**Route :** `GET /api/recommendations/stats`

```json
{
  "total_calls": 1250,
  "successful_calls": 1180,
  "failed_calls": 70,
  "avg_response_time": 2.3,
  "avg_tokens_used": 1450
}
```

### Logs d'interactions

**Table `interaction_logs` :**

```python
class InteractionLog(db.Model):
    user_id = db.Column(db.Integer)
    destination_id = db.Column(db.Integer)
    action = db.Column(db.Enum('view', 'favorite', 'reservation', 'cancel'))
    created_at = db.Column(db.DateTime)

    ACTION_WEIGHTS = {
        'view': 0.5,
        'favorite': 1.0,
        'reservation': 2.0,
        'cancel': -1.0
    }
```

### Debug mode

**Logs console :**

```
DEBUG Engine: user=42 | interactions=8 | alpha=0.5 | beta=0.5
DEBUG Engine: 30 candidates trouvées
DEBUG Engine: LLM OK → 10 recommandations
DEBUG Cache: stocké pour user 42 jusqu'à 2024-01-15 18:30
```

---

## API Routes

### GET /api/recommendations

**Description :** Recommandations personnalisées pour utilisateur connecté

**Authentification :** JWT obligatoire (optional=True)

**Paramètres :**

- `refresh=false` : Force recalcul (ignore cache)

**Réponse :**

```json
{
  "source": "hybrid|cache|algorithm_fallback",
  "data": [
    {
      "destination_id": 15,
      "name": "Barcelona",
      "country": "Spain",
      "continent": "Europe",
      "type": "Cultural",
      "avgRating": 4.2,
      "avgCostUSD": 1200,
      "bestSeason": "Spring",
      "photoURL": "https://...",
      "unescoSite": false,
      "algo_score": 0.85,
      "llm_score": 9.2,
      "final_score": 0.552,
      "explanation": "Barcelona est parfaite pour vous...",
      "rank": 1
    }
  ]
}
```

### GET /api/recommendations/test

**Description :** Debug - Test algorithme seul (sans LLM)

**Authentification :** JWT obligatoire

**Réponse :** Résultats algorithme brut + métriques utilisateur

### POST /api/interactions

**Description :** Log une interaction utilisateur

**Authentification :** JWT obligatoire

**Body :**

```json
{
  "destination_id": 15,
  "action": "view|favorite|reservation|cancel"
}
```

### GET /api/recommendations/stats

**Description :** Statistiques LLM

**Authentification :** JWT obligatoire

**Réponse :** Métriques d'usage du LLM

---

## Variables d'environnement

```bash
# Groq API (LLM)
GROQ_API_KEY=your_groq_api_key_here

# Base de données
DATABASE_URL=postgresql://user:pass@localhost/db

# JWT
JWT_SECRET_KEY=your_jwt_secret
```

---

## Optimisations possibles

### Performance

- **Cache Redis** au lieu de BD pour le cache
- **Batch processing** pour plusieurs utilisateurs
- **Worker queue** (Celery) pour appels LLM asynchrones

### Qualité

- **A/B Testing** : Comparer différentes stratégies
- **Feedback utilisateur** : "Cette recommandation était-elle pertinente?"
- **Reinforcement Learning** : Apprendre des interactions

### Coût

- **Smart caching** : Cache plus long pour utilisateurs stables
- **Model selection** : Modèle plus léger pour cas simples
- **Prompt optimization** : Prompts plus courts

---

## Résumé

**Votre système de recommandations est un hybride sophistiqué :**

1. **Algorithme traditionnel** filtre 30 candidates pertinentes
2. **LLM (Groq)** rank les 10 meilleures + génère explications personnalisées
3. **Cache intelligent** assure performance et scalabilité
4. **Logging complet** permet monitoring et optimisation

**Résultat :** Recommandations hautement personnalisées avec explications convaincantes, tout en maintenant d'excellentes performances.

---

**Fin de la documentation** ✨

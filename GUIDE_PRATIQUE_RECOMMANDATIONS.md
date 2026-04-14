# 🛠️ Guide Pratique - Recommandations LLM

## Table des matières

1. [Exemples d'utilisation](#exemples-dutilisation)
2. [Debug et troubleshooting](#debug-et-troubleshooting)
3. [Tests et validation](#tests-et-validation)
4. [Optimisation performance](#optimisation-performance)
5. [Monitoring en production](#monitoring-en-production)
6. [Cas d'usage courants](#cas-dusage-courants)

---

## Exemples d'utilisation

### 1️⃣ Recommandations pour nouvel utilisateur

**Profil utilisateur :**

- Aucune réservation
- Aucun favori
- Quelques vues récentes

**Processus :**

```python
# DataCollector détecte cold start
user_data = {
    'is_new_user': True,
    'total_interactions': 0,
    'alpha': 1.0,  # 100% Content-Based
    'beta': 0.0    # 0% User-Based
}

# Algorithme seul (pas de profil utilisateur)
# → Destinations populaires ou random
```

**Résultat :** Recommandations générales pour découvrir la plateforme

### 2️⃣ Utilisateur avec quelques interactions

**Profil utilisateur :**

- 2 réservations : Paris (France), Rome (Italie)
- 1 favori : Barcelone (Espagne)
- 5 vues : destinations européennes

**Score hybride :**

```
α = 0.8 (Content-Based dominant)
β = 0.2 (User-Based faible)
```

**Prompt LLM généré :**

```
PROFIL UTILISATEUR :
- Destinations réservées : Paris (France), Rome (Italie)
- Destinations favorites : Barcelone (Espagne)
- Nombre interactions : 3

DESTINATIONS CANDIDATES :
1. ID:15 | Barcelona
   Pays: Spain (Europe)
   Type: Cultural
   Coût/pers: 1200$
   Saison: Spring
   Note: 4.2/5
   UNESCO: Non
   Score algo: 0.85

[... 29 autres candidates ...]

TA TACHE :
Sélectionne les 10 destinations les plus adaptées...
```

### 3️⃣ Utilisateur expérimenté

**Profil utilisateur :**

- 8 réservations (diversifiées)
- 5 favoris
- 20+ vues

**Score hybride :**

```
α = 0.5 (équilibré)
β = 0.5 (équilibré)
```

**Résultat LLM :**

```json
{
  "recommendations": [
    {
      "destination_id": 15,
      "rank": 1,
      "llm_score": 9.2,
      "explanation": "Barcelona est parfaite pour vous qui aimez les destinations culturelles européennes. Avec son architecture unique et sa gastronomie, elle correspond à vos précédentes réservations."
    }
  ]
}
```

**Score final :**

```
algo_score = 0.85
llm_score = 9.2
final_score = 0.4 × 0.85 + 0.6 × (9.2/10) = 0.34 + 0.552 = 0.892
```

---

## Debug et troubleshooting

### 🔍 Commandes debug

**1. Tester l'algorithme seul :**

```bash
# Depuis Postman ou navigateur
GET /api/recommendations/test
Authorization: Bearer <jwt_token>
```

**Réponse exemple :**

```json
{
  "user_id": 42,
  "alpha": 0.5,
  "beta": 0.5,
  "is_new_user": false,
  "total_interactions": 8,
  "source": "algorithm_only",
  "candidates": [
    {
      "id": 15,
      "name": "Barcelona",
      "country": "Spain",
      "algo_score": 0.85
    }
  ]
}
```

**2. Forcer refresh du cache :**

```bash
GET /api/recommendations?refresh=true
```

**3. Voir les stats LLM :**

```bash
GET /api/recommendations/stats
```

**Réponse :**

```json
{
  "total_calls": 1250,
  "successful_calls": 1180,
  "failed_calls": 70,
  "avg_response_time": 2.3,
  "avg_tokens_used": 1450
}
```

### 🐛 Problèmes courants

**1. LLM retourne JSON invalide :**

```
DEBUG LLM JSON invalide: Expecting ',' delimiter: line 1 column 123
```

**Solution :** Fallback automatique vers algorithme seul

**2. Cache ne s'invalide pas :**

```python
# Vérifier les interactions
POST /api/interactions
{
  "destination_id": 15,
  "action": "favorite"
}
```

**3. Scores LLM trop bas :**

- Vérifier que `llm_score` est entre 6.0-10.0
- Auto-correction si < 1.0 (multiplié par 10)

**4. Timeout LLM :**

```
DEBUG LLM erreur: Request timed out
```

**Solution :** Fallback algorithme

### 📊 Logs utiles

**Console Flask :**

```
DEBUG Engine: user=42 | interactions=8 | alpha=0.5 | beta=0.5
DEBUG Engine: 30 candidates trouvées
DEBUG Engine: LLM OK → 10 recommandations
DEBUG Cache: stocké pour user 42 jusqu'à 2024-01-15 18:30
```

**Logs base de données :**

```sql
-- Vérifier interactions récentes
SELECT * FROM interaction_logs
WHERE user_id = 42
ORDER BY created_at DESC LIMIT 10;

-- Vérifier cache
SELECT * FROM recommendation_cache
WHERE user_id = 42;

-- Vérifier logs LLM
SELECT * FROM llm_logs
WHERE user_id = 42
ORDER BY created_at DESC LIMIT 5;
```

---

## Tests et validation

### 🧪 Tests unitaires

**1. Tester DataCollector :**

```python
def test_get_user_data():
    collector = DataCollector()
    user_data = collector.get_user_data(42)

    assert user_data['user_id'] == 42
    assert 'alpha' in user_data
    assert 'beta' in user_data
    assert user_data['alpha'] + user_data['beta'] == 1.0
```

**2. Tester AlgorithmFilter :**

```python
def test_content_based_scores():
    algo = AlgorithmFilter()
    user_data = {'reservations': [], 'favorites': [], 'views': []}
    destinations = [mock_destination()]

    scores = algo.compute_content_based_scores(user_data, destinations)
    assert isinstance(scores, dict)
```

**3. Tester LLM Service :**

```python
def test_llm_recommendations():
    llm = LLMService()
    prompt = "Test prompt"

    # Mock la réponse Groq
    with patch('groq.Groq') as mock_groq:
        mock_response = {
            'choices': [{'message': {'content': '{"recommendations": []}'}}],
            'usage': {'total_tokens': 100}
        }
        mock_groq.return_value.chat.completions.create.return_value = mock_response

        result = llm.get_recommendations(prompt, 42)
        assert result == []
```

### 🔄 Tests d'intégration

**1. Pipeline complet :**

```python
def test_full_recommendation_pipeline():
    engine = PersonalizationEngine()

    # Utilisateur test
    user_id = 42

    # Générer recommandations
    result = engine.get_recommendations(user_id, force_refresh=True)

    # Vérifications
    assert 'source' in result
    assert 'data' in result
    assert len(result['data']) == 10

    for rec in result['data']:
        assert 'destination_id' in rec
        assert 'final_score' in rec
        assert 'explanation' in rec
```

**2. Test cache :**

```python
def test_cache_behavior():
    cache = CacheService()

    # Stocker
    cache.set(42, [{'test': 'data'}])

    # Récupérer
    cached = cache.get(42)
    assert cached == [{'test': 'data'}]

    # Invalider
    cache.invalidate(42)
    assert cache.get(42) is None
```

### 🎯 Tests fonctionnels

**1. Test nouveaux utilisateurs :**

- Créer compte sans interactions
- Vérifier recommandations = destinations populaires
- Source = "popular"

**2. Test utilisateurs actifs :**

- Ajouter réservations/favoris
- Vérifier recommandations personnalisées
- Source = "hybrid"

**3. Test fallback LLM :**

- Désactiver clé GROQ_API_KEY
- Vérifier source = "algorithm_fallback"
- Vérifier 10 recommandations avec scores algo

---

## Optimisation performance

### ⚡ Améliorations cache

**1. Cache Redis (recommandé) :**

```python
# Au lieu de BD, utiliser Redis
import redis

class RedisCacheService:
    def __init__(self):
        self.redis = redis.Redis(host='localhost', port=6379, db=0)

    def get(self, user_id):
        key = f"recommendations:{user_id}"
        data = self.redis.get(key)
        return json.loads(data) if data else None

    def set(self, user_id, recommendations, ttl=21600):  # 6h
        key = f"recommendations:{user_id}"
        self.redis.setex(key, ttl, json.dumps(recommendations))
```

**2. Cache intelligent :**

```python
# Cache plus long pour utilisateurs stables
def get_cache_ttl(user_data):
    interactions = user_data['total_interactions']

    if interactions < 5:
        return 2 * 3600  # 2h pour nouveaux
    elif interactions < 20:
        return 6 * 3600  # 6h pour actifs
    else:
        return 24 * 3600  # 24h pour experts
```

### 🚀 Optimisations LLM

**1. Prompt plus court :**

```python
# Réduire nombre candidates de 30 à 20
candidates = self.algorithm.filter_candidates(user_data, all_destinations, top_n=20)
```

**2. Modèle plus léger :**

```python
# Pour cas simples, utiliser modèle moins coûteux
if user_data['total_interactions'] < 10:
    model = "llama-3.1-8b-instant"  # Plus rapide, moins cher
else:
    model = "llama-3.3-70b-versatile"  # Plus précis
```

**3. Batch processing :**

```python
# Traiter plusieurs utilisateurs en parallèle
def batch_recommendations(user_ids):
    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = [executor.submit(get_recommendations, uid) for uid in user_ids]
        return [f.result() for f in futures]
```

### 📈 Optimisations base de données

**1. Index optimisés :**

```sql
-- Index pour interaction_logs
CREATE INDEX idx_interaction_user_action ON interaction_logs(user_id, action);
CREATE INDEX idx_interaction_created ON interaction_logs(created_at DESC);

-- Index pour llm_logs
CREATE INDEX idx_llm_user ON llm_logs(user_id);
CREATE INDEX idx_llm_success ON llm_logs(success);
```

**2. Requêtes optimisées :**

```python
# Au lieu de multiples queries
def get_user_interactions_optimized(user_id):
    return db.session.query(
        InteractionLog.destination_id,
        InteractionLog.action,
        func.count().label('count')
    ).filter_by(user_id=user_id).group_by(
        InteractionLog.destination_id,
        InteractionLog.action
    ).all()
```

---

## Monitoring en production

### 📊 Métriques clés

**1. Dashboard LLM :**

```python
@app.route('/api/recommendations/stats')
def get_llm_stats():
    stats = db.session.query(
        func.count(LlmLog.id).label('total_calls'),
        func.avg(LlmLog.response_time).label('avg_response_time'),
        func.avg(LlmLog.tokens_used).label('avg_tokens_used'),
        func.sum(case((LlmLog.success == True, 1), else_=0)).label('successful_calls')
    ).first()

    return jsonify({
        'total_calls': stats.total_calls,
        'successful_calls': stats.successful_calls,
        'failed_calls': stats.total_calls - stats.successful_calls,
        'success_rate': round(stats.successful_calls / stats.total_calls * 100, 2),
        'avg_response_time': round(stats.avg_response_time, 2),
        'avg_tokens_used': round(stats.avg_tokens_used, 0)
    })
```

**2. Alertes à surveiller :**

- **Taux succès LLM < 95%**
- **Temps réponse moyen > 5 secondes**
- **Cache hit rate < 70%**
- **Fallback activé > 10% du temps**

### 🔍 Logs structurés

**1. Log LLM détaillé :**

```python
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def log_llm_call(user_id, prompt_length, response_time, success, error=None):
    logger.info(
        "LLM Call",
        extra={
            'user_id': user_id,
            'prompt_length': prompt_length,
            'response_time': response_time,
            'success': success,
            'error': str(error) if error else None,
            'timestamp': datetime.utcnow().isoformat()
        }
    )
```

**2. Métriques temps réel :**

```python
from flask import g
import time

@app.before_request
def start_timer():
    g.start = time.time()

@app.after_request
def log_request(response):
    duration = time.time() - g.start
    logger.info(f"Request {request.path} took {duration:.2f}s")
    return response
```

### 📈 Graphiques recommandés

**1. Performance LLM :**

- Temps réponse moyen (target: < 3s)
- Taux succès (target: > 95%)
- Tokens utilisés moyens

**2. Utilisation cache :**

- Hit rate (target: > 80%)
- Invalidation rate
- Cache size evolution

**3. Qualité recommandations :**

- Score moyen algorithme
- Score moyen LLM
- Distribution scores finaux

---

## Cas d'usage courants

### 🎯 Scénarios utilisateur

**1. Touriste culturel européen :**

```
Historique: Paris, Rome, Barcelone
Recommandations: Amsterdam, Prague, Berlin
Explication: "Ces destinations culturelles européennes s'accordent parfaitement avec vos voyages précédents..."
```

**2. Aventurier nature :**

```
Historique: Nouvelle-Zélande, Canada, Islande
Recommandations: Norvège, Patagonie, Alpes
Explication: "Pour continuer votre aventure en nature, ces destinations sauvages offrent des paysages exceptionnels..."
```

**3. Voyageur budget :**

```
Historique: Thaïlande, Vietnam, Maroc
Recommandations: Cambodge, Laos, Tunisie
Explication: "Ces destinations abordables maintiennent votre budget voyage tout en offrant des expériences riches..."
```

### 🔄 Gestion des saisons

**1. Recommandations saisonnières :**

```python
def get_seasonal_recommendations(user_data, current_season):
    # Booster destinations de la saison actuelle
    seasonal_boost = {
        'winter': ['ski', 'tropical'],
        'summer': ['beach', 'mountain'],
        'spring': ['cultural', 'nature'],
        'autumn': ['wine', 'fall_colors']
    }

    # Ajuster scores selon saison
    for candidate in candidates:
        if candidate['destination'].bestSeason.lower() == current_season:
            candidate['algo_score'] *= 1.2  # +20% pour saison idéale
```

### 🌍 Personnalisation culturelle

**1. Adaptation linguistique :**

```python
def get_user_language(user_id):
    # Détecter langue préférée
    user = User.query.get(user_id)
    return user.preferred_language or 'fr'

def translate_explanation(explanation, target_lang):
    # Utiliser LLM pour traduction
    prompt = f"Traduis en {target_lang}: {explanation}"
    # ... appel LLM pour traduction
```

### 💰 Gestion budget

**1. Profilage automatique :**

```python
def detect_budget_profile(user_data):
    costs = []
    for resa in user_data['reservations']:
        dest = Destination.query.get(resa.destination_id)
        if dest and dest.avgCostUSD:
            costs.append(dest.avgCostUSD)

    if not costs:
        return "Non déterminé"

    avg_cost = sum(costs) / len(costs)

    if avg_cost < 500:
        return "Budget économique"
    elif avg_cost < 1500:
        return "Budget moyen"
    else:
        return "Budget premium"
```

### 🎪 Gestion événements spéciaux

**1. Recommandations événementielles :**

```python
def get_event_based_recommendations():
    current_events = {
        '2024-07-14': 'Bastille Day - Paris',
        '2024-12-25': 'Noël - destinations hivernales',
        '2024-02-14': "Saint Valentin - destinations romantiques"
    }

    today = datetime.now().strftime('%Y-%m-%d')
    if today in current_events:
        # Booster destinations liées à l'événement
        event_destinations = get_event_destinations(current_events[today])
        # Ajuster scores + ajouter explication spéciale
```

---

## Checklist déploiement

### ✅ Pré-déploiement

- [ ] **Clé API Groq** configurée en production
- [ ] **Base de données** indexes optimisés
- [ ] **Cache Redis** configuré (optionnel mais recommandé)
- [ ] **Monitoring** alerts configurés
- [ ] **Tests** unitaires et d'intégration passent
- [ ] **Fallback** testé (LLM indisponible)

### ✅ Post-déploiement

- [ ] **Métriques** LLM monitorées (succès > 95%)
- [ ] **Performance** cache vérifiée (hit rate > 80%)
- [ ] **Logs** analysés pour anomalies
- [ ] **Feedback** utilisateur collecté
- [ ] **A/B tests** planifiés pour optimisations

---

**Fin du guide pratique** 🛠️

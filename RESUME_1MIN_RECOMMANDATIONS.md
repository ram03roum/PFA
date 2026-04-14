# ⚡ Résumé 1 minute - Recommandations LLM

## 🎯 En quoi ça consiste ?

**Système hybride** combinant algorithme traditionnel + IA (Groq) pour recommander des destinations de voyage personnalisées.

## 🏗️ Architecture

```
Frontend → PersonalizationEngine → Cache/Algo/LLM → Recommandations
```

**3 composants principaux :**

- **Algorithme** : Filtrage 30 candidates (content-based + collaborative)
- **LLM (Groq)** : Ranking top 10 + explications personnalisées
- **Cache** : 6h pour performance

## 🔄 Flux simplifié

1. **Cache check** → Si valide, retourner
2. **Collecte données** → Réservations, favoris, vues utilisateur
3. **Algorithme** → 30 candidates avec scores
4. **LLM** → Top 10 avec explications en français
5. **Fusion** → Score final = 40% algo + 60% LLM
6. **Cache** → Stocker 6h

## 📊 Score final

```
Score = 0.4 × score_algorithme + 0.6 × (score_LLM/10)
```

**Exemple :**

- Algo: 0.85
- LLM: 9.2
- Final: 0.34 + 0.552 = **0.892**

## 🎨 Explication LLM

**Prompt français** → **Réponse JSON** avec explication personnalisée :

_"Barcelona est parfaite pour vous qui aimez les destinations culturelles européennes. Avec son architecture unique et sa gastronomie, elle correspond à vos précédentes réservations."_

## 🛡️ Robustesse

- **Fallback automatique** si LLM échoue → Algorithme seul
- **Cache intelligent** → Invalide pour interactions fortes
- **Logging complet** → Métriques performance/coût

## 📈 Métriques clés

- **Temps réponse** : ~2-3 secondes
- **Taux succès LLM** : >95%
- **Cache hit rate** : >80%
- **Coût** : ~1450 tokens/appel

## 🎛️ Configuration

```python
# LLM
model = "llama-3.3-70b-versatile"
temperature = 0.3  # Cohérence
max_tokens = 2000

# Cache
ttl = 6 * 3600  # 6 heures

# Score hybride
alpha = 0.4  # Poids algorithme
beta = 0.6   # Poids LLM
```

## 🚀 Points forts

✅ **Personnalisation avancée** : Profil utilisateur complet
✅ **Explications convaincantes** : LLM génère texte marketing
✅ **Performance** : Cache + fallback garantissent disponibilité
✅ **Évolutif** : Scores ajustables selon stratégie

## 🔧 Debug rapide

```bash
# Test algorithme seul
GET /api/recommendations/test

# Stats LLM
GET /api/recommendations/stats

# Force refresh
GET /api/recommendations?refresh=true
```

## 💡 Innovation

**Hybride unique** : Pas que LLM, pas qu'algorithme → **Le meilleur des deux mondes** avec explications IA et robustesse algorithmique.

---

**Résultat** : Recommandations ultra-personnalisées avec justifications intelligentes, tout en maintenant performance et fiabilité ! 🎯✨

# 📚 Index Documentation - Recommandations LLM

## 📖 Guides disponibles

### 📋 Documentation complète

**[📕 DOCUMENTATION_RECOMMANDATIONS_LLM.md](DOCUMENTATION_RECOMMANDATIONS_LLM.md)**

- Architecture détaillée du système
- Flux de données complet
- Services composants expliqués
- Algorithme hybride (content-based + collaborative)
- Intégration LLM (Groq)
- Cache et performance
- Monitoring et logs
- API Routes

### 📊 Schémas visuels

**[📈 SCHEMAS_VISUELS_RECOMMANDATIONS.md](SCHEMAS_VISUELS_RECOMMANDATIONS.md)**

- Architecture générale (Mermaid)
- Flux de données détaillé
- Algorithme hybride visualisé
- Intégration LLM
- Système de cache
- Base de données (ER diagram)
- Formules de score
- Gestion erreurs/fallback

### 🛠️ Guide pratique

**[🔧 GUIDE_PRATIQUE_RECOMMANDATIONS.md](GUIDE_PRATIQUE_RECOMMANDATIONS.md)**

- Exemples d'utilisation concrets
- Debug et troubleshooting
- Tests et validation
- Optimisation performance
- Monitoring en production
- Cas d'usage courants

### ⚡ Résumé 1 minute

**[⚡ RESUME_1MIN_RECOMMANDATIONS.md](RESUME_1MIN_RECOMMANDATIONS.md)**

- Vue d'ensemble rapide
- Flux simplifié
- Métriques clés
- Points forts

---

## 🎯 Points d'entrée recommandés

### 👶 Nouveau sur le projet ?

1. **[Lire d'abord](RESUME_1MIN_RECOMMANDATIONS.md)** → Comprendre en 1 minute
2. **[Architecture visuelle](SCHEMAS_VISUELS_RECOMMANDATIONS.md)** → Schémas Mermaid
3. **[Guide pratique](GUIDE_PRATIQUE_RECOMMANDATIONS.md)** → Exemples concrets

### 🧑‍💻 Développeur ?

1. **[Documentation complète](DOCUMENTATION_RECOMMANDATIONS_LLM.md)** → Spécifications détaillées
2. **[Guide pratique](GUIDE_PRATIQUE_RECOMMANDATIONS.md)** → Debug et tests
3. **[API Routes](#api-routes)** → Intégration frontend

### 👨‍💼 Product Manager ?

1. **[Résumé](RESUME_1MIN_RECOMMANDATIONS.md)** → Vue d'ensemble
2. **[Métriques](#monitoring-et-logs)** → Performance et qualité
3. **[Cas d'usage](#cas-dusage-courants)** → Scénarios utilisateur

---

## 🔍 Recherche rapide

### Par sujet

| Sujet            | Document                                             | Section                  |
| ---------------- | ---------------------------------------------------- | ------------------------ |
| **Architecture** | [Doc complète](DOCUMENTATION_RECOMMANDATIONS_LLM.md) | Architecture du système  |
| **Algorithme**   | [Doc complète](DOCUMENTATION_RECOMMANDATIONS_LLM.md) | Algorithme de filtrage   |
| **LLM**          | [Doc complète](DOCUMENTATION_RECOMMANDATIONS_LLM.md) | Intégration LLM          |
| **Cache**        | [Doc complète](DOCUMENTATION_RECOMMANDATIONS_LLM.md) | Cache et performance     |
| **Debug**        | [Guide pratique](GUIDE_PRATIQUE_RECOMMANDATIONS.md)  | Debug et troubleshooting |
| **Tests**        | [Guide pratique](GUIDE_PRATIQUE_RECOMMANDATIONS.md)  | Tests et validation      |
| **Performance**  | [Guide pratique](GUIDE_PRATIQUE_RECOMMANDATIONS.md)  | Optimisation performance |
| **Monitoring**   | [Guide pratique](GUIDE_PRATIQUE_RECOMMANDATIONS.md)  | Monitoring en production |

### Par fichier

| Fichier                        | Description             | Usage                                           |
| ------------------------------ | ----------------------- | ----------------------------------------------- |
| `services/personnalisation.py` | Orchestrateur principal | [Doc complète - Services](#services-composants) |
| `services/llm_service.py`      | Interface Groq          | [Doc complète - LLM](#intégration-llm)          |
| `services/algorithm_filter.py` | Filtrage algorithmique  | [Doc complète - Algo](#algorithme-de-filtrage)  |
| `services/prompt_builder.py`   | Construction prompts    | [Doc complète - LLM](#intégration-llm)          |
| `services/cache_service.py`    | Gestion cache           | [Doc complète - Cache](#cache-et-performance)   |
| `routes/recommendations.py`    | API endpoints           | [Doc complète - API](#api-routes)               |

---

## 🚀 Démarrage rapide

### 1. Comprendre le système (5 min)

```bash
# Lire le résumé
cat RESUME_1MIN_RECOMMANDATIONS.md
```

### 2. Voir l'architecture (3 min)

```bash
# Ouvrir les schémas
start SCHEMAS_VISUELS_RECOMMANDATIONS.md
```

### 3. Tester en local (10 min)

```bash
# Lancer l'app
python app.py

# Tester l'API
curl -H "Authorization: Bearer <token>" \
     http://localhost:5000/api/recommendations/test
```

### 4. Debug (5 min)

```bash
# Voir les logs
tail -f logs/flask.log

# Tester le cache
curl "http://localhost:5000/api/recommendations?refresh=true"
```

---

## 📞 Support et questions

### Questions fréquentes

**Q: Comment ça marche si LLM est down ?**
**R:** [Fallback automatique](GUIDE_PRATIQUE_RECOMMANDATIONS.md#fallback-garantit) → Algorithme seul

**Q: Combien ça coûte ?**
**R:** [Métriques LLM](GUIDE_PRATIQUE_RECOMMANDATIONS.md#monitoring-en-production) → ~1450 tokens/appel

**Q: Comment déboguer ?**
**R:** [Debug commands](GUIDE_PRATIQUE_RECOMMANDATIONS.md#commandes-debug) + logs console

**Q: Performance ?**
**R:** [Cache 6h](DOCUMENTATION_RECOMMANDATIONS_LLM.md#cache-et-performance) + réponse ~2-3s

### Contacts

- **Issues GitHub** : Bugs et features
- **Documentation** : Cette doc pour compréhension
- **Logs** : `/api/recommendations/stats` pour métriques

---

## 🔄 Mises à jour

**Dernière mise à jour :** Décembre 2024
**Version :** 1.0
**Couverture :** 100% du système de recommandations

**Prochaines améliorations :**

- [ ] A/B testing framework
- [ ] Cache Redis
- [ ] Métriques temps réel
- [ ] Feedback utilisateur

---

**Prêt à explorer ? Commencez par le [résumé 1 minute](RESUME_1MIN_RECOMMANDATIONS.md) !** ⚡

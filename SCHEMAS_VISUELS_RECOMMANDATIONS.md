# 📊 Schémas Visuels - Système de Recommandations LLM

## Table des matières

1. [Architecture générale](#architecture-générale)
2. [Flux de données détaillé](#flux-de-données-détaillé)
3. [Algorithme hybride](#algorithme-hybride)
4. [Intégration LLM](#intégration-llm)
5. [Système de cache](#système-de-cache)
6. [Base de données](#base-de-données)

---

## Architecture générale

```mermaid
graph TB
    A[👤 Utilisateur] --> B[Frontend Angular]
    B --> C[GET /api/recommendations]

    C --> D[PersonalizationEngine]
    D --> E[CacheService]
    D --> F[DataCollector]
    D --> G[AlgorithmFilter]
    D --> H[LLMService]

    E --> I[(Cache DB)]
    F --> J[(MySQL)]
    G --> J
    H --> K[Groq API]

    D --> L[Recommandations JSON]
    L --> B

    style D fill:#e1f5fe
    style H fill:#f3e5f5
    style E fill:#e8f5e8
```

---

## Flux de données détaillé

```mermaid
sequenceDiagram
    participant U as 👤 Utilisateur
    participant F as Frontend
    participant P as PersonalizationEngine
    participant C as CacheService
    participant D as DataCollector
    participant A as AlgorithmFilter
    participant PB as PromptBuilder
    participant L as LLMService
    participant G as Groq API

    U->>F: Ouvre page d'accueil
    F->>P: get_recommendations(user_id)

    P->>C: Vérifier cache
    alt Cache valide
        C-->>P: Retourner cache
        P-->>F: {source: "cache", data: [...]}
    else Cache expiré/vide
        P->>D: Collecter données utilisateur
        D-->>P: user_data + all_destinations

        P->>A: Filtrer 30 candidates
        A-->>P: candidates[]

        P->>PB: Construire prompt
        PB-->>P: prompt_text

        P->>L: get_recommendations(prompt, user_id)
        L->>G: Appel API Groq
        G-->>L: JSON recommendations
        L-->>P: llm_results[]

        P->>P: Fusionner scores (algo + LLM)
        P->>C: Stocker en cache
        P-->>F: {source: "hybrid", data: [...]}
    end
```

---

## Algorithme hybride

```mermaid
graph TD
    A[Données utilisateur] --> B[Content-Based Filtering]
    A --> C[User-Based Collaborative Filtering]

    B --> D[TF-IDF Vectorization]
    D --> E[Cosine Similarity]
    E --> F[Content Scores]

    C --> G[Interaction Matrix]
    G --> H[KNN Algorithm]
    H --> I[User Scores]

    F --> J[Score hybride]
    I --> J
    J --> K[α × content + β × user]

    K --> L[30 Candidates triées]

    style B fill:#bbdefb
    style C fill:#c8e6c9
    style J fill:#fff3e0
```

**Légende des poids :**

- α (Content-Based) : 0.5 - 1.0
- β (User-Based) : 0.0 - 0.5
- **Ajustés selon nombre d'interactions utilisateur**

---

## Intégration LLM

```mermaid
graph TD
    A[30 Candidates] --> B[PromptBuilder]
    B --> C[Construction prompt]

    C --> D[Profil utilisateur]
    C --> E[Formatage candidates]
    C --> F[Instructions LLM]

    D --> G[Prompt complet]
    E --> G
    F --> G

    G --> H[LLMService]
    H --> I[Groq API]
    I --> J[Llama-3.3-70B]

    J --> K[JSON Response]
    K --> L[10 Recommandations]
    L --> M[Explications personnalisées]

    style H fill:#f3e5f5
    style I fill:#fce4ec
    style J fill:#fce4ec
```

**Configuration LLM :**

```json
{
  "model": "llama-3.3-70b-versatile",
  "temperature": 0.3,
  "max_tokens": 2000,
  "response_format": { "type": "json_object" }
}
```

---

## Système de cache

```mermaid
graph TD
    A[Requête utilisateur] --> B{Cache valide?}
    B -->|Oui| C[Retourner cache]
    B -->|Non| D[Générer recommandations]

    D --> E[Stocker en cache]
    E --> F[6 heures]

    G[Nouvelle interaction] --> H{Action forte?}
    H -->|favorite/reservation/cancel| I[Invalidation cache]
    H -->|view| J[Garder cache]

    style B fill:#e8f5e8
    style E fill:#e8f5e8
    style I fill:#ffebee
```

**Stratégie de cache :**

- **Durée :** 6 heures
- **Invalide pour :** favoris, réservations, annulations
- **Avantages :** Performance + Réduction coût LLM

---

## Base de données

```mermaid
erDiagram
    USERS ||--o{ INTERACTION_LOGS : "interactions"
    USERS ||--o{ RECOMMENDATION_CACHE : "cache"
    USERS ||--o{ LLM_LOGS : "logs_llm"

    DESTINATIONS ||--o{ INTERACTION_LOGS : "vues"
    DESTINATIONS ||--o{ RESERVATIONS : "réservations"

    INTERACTION_LOGS {
        int id PK
        int user_id FK
        int destination_id FK
        enum action "view/favorite/reservation/cancel"
        float weight "0.5-2.0"
        datetime created_at
    }

    RECOMMENDATION_CACHE {
        int user_id PK
        json recommendations
        datetime generated_at
        datetime expires_at
    }

    LLM_LOGS {
        int id PK
        int user_id
        int tokens_used
        float response_time
        bool success
        datetime created_at
    }

    DESTINATIONS {
        int id PK
        string name
        string country
        string type
        float avgRating
        int avgCostUSD
        string bestSeason
        bool unescoSite
    }
```

---

## Score final - Formule détaillée

```mermaid
graph TD
    A[Score algorithme] --> C[0.4 ×]
    B[Score LLM] --> D[0.6 × (llm_score/10)]
    C --> E[Score final]
    D --> E

    F[Score LLM brut] --> G[6.0 - 10.0]
    G --> H[Normalisation /10]
    H --> I[0.6 - 1.0]
    I --> D

    style E fill:#fff3e0
    style C fill:#e3f2fd
    style D fill:#f3e5f5
```

**Exemple concret :**

```
Destination Barcelona:
├── Score algorithme = 0.85
├── Score LLM brut = 9.2
├── Score LLM normalisé = 9.2/10 = 0.92
├── Score final = 0.4×0.85 + 0.6×0.92 = 0.34 + 0.552 = 0.892
```

---

## Gestion des erreurs et fallbacks

```mermaid
graph TD
    A[Appel LLM] --> B{Succès?}
    B -->|Oui| C[Parser JSON]
    B -->|Non| D[Fallback algorithme]

    C --> E{JSON valide?}
    E -->|Oui| F[10 recommandations]
    E -->|Non| D

    D --> G[Top 10 algorithme]
    G --> H[Explication générique]
    H --> I[Score = algo_score]

    F --> J[Fusion scores]
    J --> K[Cache + Retour]

    style D fill:#ffebee
    style G fill:#fff3e0
```

**Fallback garantit :**

- ✅ Toujours 10 recommandations
- ✅ Scores algorithmiques fiables
- ✅ Explications basiques
- ✅ Service continu même si LLM down

---

## Métriques et monitoring

```mermaid
graph TD
    A[LLM Service] --> B[Logging]
    B --> C[llm_logs table]

    C --> D[Tokens utilisés]
    C --> E[Temps réponse]
    C --> F[Taux succès]

    D --> G[GET /api/recommendations/stats]
    E --> G
    F --> G

    H[AlgorithmFilter] --> I[Debug logs]
    I --> J[Console output]

    K[CacheService] --> L[Cache hits/misses]
    L --> J

    style G fill:#e8f5e8
    style J fill:#fff3e0
```

**Métriques trackées :**

- **Performance LLM :** Temps réponse moyen, tokens utilisés
- **Fiabilité :** Taux de succès des appels
- **Cache :** Hit rate, invalidations
- **Algorithme :** Nombre candidates, scores moyens

---

## Flux utilisateur complet

```mermaid
journey
    title Parcours utilisateur - Recommandations
    section Nouveau visiteur
        Arrive sur site : 5 : Utilisateur
        Voit destinations populaires : 4 : Système
    section Utilisateur connecté
        S'inscrit : 5 : Utilisateur
        Première connexion : 5 : Utilisateur
        Voit recommandations cold-start : 4 : Système
        Explore destinations : 4 : Utilisateur
        Ajoute favoris : 5 : Utilisateur
        Cache invalidé : 3 : Système
        Recommandations s'améliorent : 5 : Système
    section Utilisateur expérimenté
        Réserve voyage : 5 : Utilisateur
        Recommandations hyper-personnalisées : 5 : Système
        Feedback positif : 5 : Utilisateur
```

---

## Comparaison stratégies

```mermaid
pie title Stratégies de recommandation
    "Content-Based (α)" : 50
    "User-Based (β)" : 50
    "LLM Ranking" : 60
    "Cache" : 40
```

**Répartition des poids :**

- **Algorithme :** 40% du score final
- **LLM :** 60% du score final
- **Cache :** 100% des performances

---

**Fin des schémas visuels** 📈

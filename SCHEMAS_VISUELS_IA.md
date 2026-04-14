# 🎨 Schémas Visuels - Flux de Données IA

## 1️⃣ Architecture Globale du Projet

```
┌─────────────────────────────────────────────────────────────────┐
│                    VOTRE SITE VOYAGE (PFAA)                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              FRONTEND (Angular)                          │   │
│  │                                                           │   │
│  │  ┌─────────────┐  ┌──────────────┐  ┌─────────────┐    │   │
│  │  │ Page Home   │  │ Page Chat IA │  │ Page Contact│    │   │
│  │  └─────┬───────┘  └──────┬───────┘  └─────┬───────┘    │   │
│  │        │                 │                 │ (formulaire)    │
│  │        └─────────────────┼─────────────────┘              │   │
│  │                          │                                 │   │
│  └──────────────────────────┼─────────────────────────────────┘   │
│                              │                                     │
│                       HTTPS API (REST)                            │
│                              │                                     │
│  ┌──────────────────────────┼─────────────────────────────────┐   │
│  │      BACKEND (Flask)      │                                │   │
│  │                           ▼                                │   │
│  │         ┌────────────────────────────────────────┐        │   │
│  │         │    Routes (routes/)                    │        │   │
│  │         │ ┌──────────────────────────────────┐  │        │   │
│  │         │ │ POST /api/chat/message       ◄──┼──┤◄───┐   │   │
│  │         │ │     ↓ (réponse IA immediat)     │  │    │   │   │
│  │         │ │ POST /api/contact            ◄──┼──┤◄───┼───┤   │
│  │         │ │     ↓ (thread async)            │  │    │   │   │
│  │         │ │ GET /api/admin/messages      ◄──┼──┤◄───┤   │   │
│  │         └──────────────────────────────────┘  │    │   │   │
│  │                                                 │    │   │   │
│  │         ┌────────────────────────────────────┐ │    │   │   │
│  │         │    Services (services/)          │ │ │    │   │   │
│  │         │                                   │ │ │    │   │   │
│  │         │ ┌──────────────────────────────┐ │ │ │    │   │   │
│  │         │ │ ai_service.py                │ │ │ │    │   │   │
│  │         │ │ ├─ get_ai_response()        │◄┼─┤─┤────┘   │   │
│  │         │ │ ├─ classify_message()       │  │ │ │        │   │
│  │         │ │ ├─ generate_auto_email()   │  │ │ │        │   │
│  │         │ │ ├─ summarize_conversation()│  │ │ │        │   │
│  │         │ └──────────────────────────────┘ │ │ │        │   │
│  │         │                                   │ │ │        │   │
│  │         │ ┌──────────────────────────────┐ │ │ │        │   │
│  │         │ │ mail_service.py              │ │ │ │        │   │
│  │         │ │ ├─ send_confirmation()      │ │ │ │        │   │
│  │         │ │ └─ send_reply_to_client()   │ │ │ │        │   │
│  │         │ └──────────────────────────────┘ │ │ │        │   │
│  │         └────────────────────────────────────┘ │ │        │   │
│  │                  ▲          ▲                  │ │        │   │
│  │                  │          │                  │ │        │   │
│  │         ┌────────┤          └────────────────┐ │ │        │   │
│  │         │        │     API Calls              │ │ │        │   │
│  │         ▼        ▼                            ▼ │ │        │   │
│  │    ┌──────────────────────────────────────────┐ │ │        │   │
│  │    │    Database (SQLAlchemy)                 │ │ │        │   │
│  │    │ ├─ users                                 │ │ │        │   │
│  │    │ ├─ conversations                         │ │ │        │   │
│  │    │ ├─ messages                              │ │ │        │   │
│  │    │ ├─ conversation_summaries                │ │ │        │   │
│  │    │ └─ contact_messages                      │ │ │        │   │
│  │    └────────────────────────────────────────────┘ │        │   │
│  │                                                 │        │   │
│  └─────────────────────────────────────────────────┘        │   │
│                                              ▲          │   │   │
│                   ┌──────────────────────────┤          │   │   │
│                   │   EXTERNAL APIs          │          │   │   │
│    ┌──────────────┴────────────┬─────────────┘          │   │   │
│    │                            │                      │   │   │
│    ▼                            ▼                      │   │   │
│ ┌──────────────┐           ┌──────────────┐           │   │   │
│ │ Google       │           │ AlwaysData   │           │   │   │
│ │ Gemini API   │           │ SMTP Server  │           │   │   │
│ │ (IA)         │           │ (Email)      │           │   │   │
│ │              │           │              │           │   │   │
│ │ gemini-2.5   │           │ smtp://      │           │   │   │
│ │ flash        │           │ alwaysdata   │           │   │   │
│ └──────────────┘           └──────────────┘           │   │   │
│                                                        │   │   │
└────────────────────────────────────────────────────────┴───┘───┘
```

---

## 2️⃣ Flux CHAT IA (Temps réel)

```
UTILISATEUR                    FRONTEND                  BACKEND                 BASE DE DONNÉES      GOOGLE GEMINI
   │                             │                           │                         │                    │
   │ 1. "Quelle est la..."       │                           │                         │                    │
   ├────────────────────────────►│                           │                         │                    │
   │                             │ POST /api/chat/message    │                         │                    │
   │                             ├──────────────────────────►│                         │                    │
   │                             │                           │ 2. Vérify JWT          │                    │
   │                             │                           │                         │                    │
   │                             │                           │ 3. Fetch historique    │                    │
   │                             │                           ├────────────────────────►│                    │
   │                             │                           │◄────────────────────────┤ [msg1, msg2...]   │
   │                             │                           │                         │                    │
   │                             │                           │ 4. Analyser question    │                    │
   │                             │                           │ analyze_user_question() ├───────────┐        │
   │                             │                           │                         │           │ JSON   │
   │                             │                           │◄─────────────────────────────────────┤        │
   │                             │                           │ {continent, type, ...} │           │        │
   │                             │                           │                         │                    │
   │                             │                           │ 5. Chercher destinations│                    │
   │                             │                           ├────────────────────────►│ Query SQL          │
   │                             │                           │◄────────────────────────┤ 8 destinations    │
   │                             │                           │                         │                    │
   │                             │                           │ 6. Construire contexte  │                    │
   │                             │                           │ build_context()         │                    │
   │                             │                           │                         │                    │
   │                             │                           │ 7. Appeler Gemini       │                    │
   │                             │                           ├────────────────────────────────────────────►│
   │                             │                           │                         │        Prompt:     │
   │                             │                           │                         │  - Historique     │
   │                             │                           │                         │  - Contexte       │
   │                             │                           │                         │  - Destinations   │
   │                             │                           │◄────────────────────────────────────────────┤
   │                             │                           │      "Voici mes        │     Réponse IA:    │
   │                             │                           │       recommandations"  │  "Je Vous conseil" │
   │                             │                           │                         │                    │
   │                             │                           │ 8. Sauvegarder réponse │                    │
   │                             │                           ├────────────────────────►│ INSERT Message     │
   │                             │                           │◄────────────────────────┤ (sender_type=ai)  │
   │                             │                           │                         │                    │
   │                             │◄──────────────────────────┤                         │                    │
   │◄────────────────────────────┤                           │                         │                    │
   │ 200 OK JSON:                │                           │                         │                    │
   │ {                            │                           │                         │                    │
   │   user_message: {...},       │                           │                         │                    │
   │   ai_message: {...}          │                           │                         │                    │
   │ }                            │                           │                         │                    │
   │                             │                           │                         │                    │
   │ 9. Afficher dans chat       │                           │                         │                    │
   ├────────────────────►│ (display messages)              │                         │                    │
   │                             │                           │                         │                    │

TEMPS ESTIMÉ: 500ms - 2 secondes ⏱️
```

---

## 3️⃣ Flux CONTACT/ADMIN (Asynchrone)

```
UTILISATEUR                FRONTEND              BACKEND PRINCIPAL      THREAD WORKER      BASE DE DONNÉES    GOOGLE GEMINI    EMAIL SERVICE
   │                          │                       │                    │                     │                 │                   │
   │ 1. Remplir formulaire     │                       │                    │                     │                 │                   │
   ├──────────────────────────►│                       │                    │                    │                 │                   │
   │                          │ POST /api/contact     │                    │                    │                 │                   │
   │                          ├──────────────────────►│                    │                    │                 │                   │
   │                          │                       │ 2. Valider données │                    │                 │                   │
   │                          │                       │                    │                    │                 │                   │
   │                          │                       │ 3. Sauvegarder BDD │                    │                 │                   │
   │                          │                       ├───────────────────────────────────────►│ INSERT           │                   │
   │                          │                       │◄───────────────────────────────────────┤ message_id=123  │                   │
   │                          │                       │                    │                    │                 │                   │
   │                          │                       │ 4. Lancer THREAD   │                    │                 │                   │
   │                          │                       ├───────┐ (async)    │                    │                 │                   │
   │                          │                       │       │            │                    │                 │                   │
   │                          │◄──────────────────────┤       │            │                    │                 │                   │
   │◄──────────────────────────┤ 200 ✅               │       │            │                    │                 │                   │
   │ "Message envoyé!"         │ (IMMÉDIAT)           │       │            │                    │                 │                   │
   │ (100ms)                   │                      │       │            │                    │                 │                   │
   │                          │                       │       │            │                    │                 │                   │
   │ ⏳ En arrière-plan...     │                       │       │            │                    │                 │                   │
   │                          │                       │       │            ▼ (started)          │                 │                   │
   │                          │                       │       │            │ 5. Classification  │                 │                   │
   │                          │                       │       │            ├──────────────────────────────────►│ demande_devis      │
   │                          │                       │       │            │ Catégorie?         │ haute priorité?  │                   │
   │                          │                       │       │            │ Sentiment?         │                 │                   │
   │                          │                       │       │            │◄──────────────────────────────────┤ JSON:             │
   │                          │                       │       │            │ {category, priority, sentiment}    │ {"category": ...} │
   │                          │                       │       │            │                    │                 │                   │
   │                          │                       │       │            │ 6. Génération réponse suggérée   │
   │                          │                       │       │            ├──────────────────────────────────►│ "Bonjour..."      │
   │                          │                       │       │            │ Réponse pro pour admin             │                   │
   │                          │                       │       │            │◄──────────────────────────────────┤ text (150 mots)   │
   │                          │                       │       │            │                    │                 │                   │
   │                          │                       │       │            │ 7. Email confirmation client     │
   │                          │                       │       │            ├──────────────────────────────────────────────────────►│
   │                          │                       │       │            │ generate_auto_confirmation()   │ "Merci, on t'appelle..."  │
   │                          │                       │       │            │◄──────────────────────────────────────────────────────┤
   │                          │                       │       │            │ ✅ Email sent                   │ SMTP ✓            │
   │                          │                       │       │            │                    │                 │                   │
   │                          │                       │       │            │ 8. Sauvegarde BD   │                 │                   │
   │                          │                       │       │            ├───────────────────────────────────►│ UPDATE message    │
   │                          │                       │       │            │ category, priority,priority,  │ (ajouter champs)  │
   │                          │                       │       │            │ sentiment, suggested_reply │                       │
   │                          │                       │       │            │◄───────────────────────────────────┤ ✓ Updated        │
   │                          │                       │       │ (THREAD TERMINE ~3-5s)                       │                   │
   │                          │                       │       │            │                    │                 │                   │
   │ ✅ EMAIL REÇU!           │                       │       │            │                    │                 │                   │
   │ (~5-10 secondes)          │                       │       │            │                    │                 │                   │
   │                          │                       │       │            │                    │                 │                   │
   │ L'admin voit:             │                       │       │            │                    │                 │                   │
   ├──────────────────────►│ GET /api/admin/messages   │       │            │                    │                 │                   │
   │                          ├──────────────────────────────────────────────────────────────────────────────►│                   │
   │                          │◄──────────────────────────────────────────────────────────────────────────────┤                   │
   │                          │ [{id: 123, name: "Alice", category: "demande_devis",                           │                   │
   │                          │   priority: "haute", sentiment: "positif",                                     │                   │
   │                          │   suggested_reply: "Bonjour Alice..."}]                                        │                   │
   │                          │                       │       │            │                    │                 │                   │

TEMPS ESTIMÉ:
- Client reçoit réponse: 100ms ✅
- Client reçoit email: 5-10 secondes (parallelisé)
- Admin voit le message: immédiatement (nouvelle requête)
- Traitement total: 3-5s (en background)
```

---

## 4️⃣ Flux détaillé de Classification d'un Message

```
MESSAGE CLIENT REÇU
    │
    │  "Bonjour, je cherche une destination de plage pour 15 personnes
    │   en avril. Pouvez-vous nous envoyer un devis?"
    │
    ▼
┌─────────────────────────────────────────┐
│  GEMINI API - classify_message()        │
│                                          │
│  Prompt:                                │
│  "Analyse ce message client...          │
│   Retourne JSON: category, priority,    │
│   sentiment, ai_summary"                │
└──────────────────┬──────────────────────┘
                   │
                   ▼
        RÉPONSE DE GEMINI:
        {
          "category": "demande_devis",    ← Commercial
          "priority": "haute",            ← 15 personnes = gros client!
          "sentiment": "positif",         ← Politesse et enthousiasme
          "ai_summary": "Groupe cherche   ← Résumé 1 phrase
                         destination plage
                         avril - 15 pers"
        }
        │
        ▼
    SAUVEGARDÉ EN BD
    contact_messages table:
    ├─ id: 123
    ├─ name: "Marie Dupont"
    ├─ email: "marie@example.com"
    ├─ category: "demande_devis"        ← Badge couleur ORANGE
    ├─ priority: "haute"                ← Badge couleur ROUGE
    ├─ sentiment: "positif"             ← Badge couleur VERT
    └─ ai_summary: "Groupe cherche..."
    │
    ├─ AVANT CLASSIFICATION:
    │  [tous les champs vides]
    │
    └─ APRÈS CLASSIFICATION (5 sec):
       [tous les champs remplis]
```

---

## 5️⃣ Génération de réponse pour l'admin

```
INPUT POUR GEMINI:
└─ Catégorie: demande_devis
└─ Sentiment: positif
└─ Message client: "Groupe de 15 cherche plage..."
└─ Nom client: "Marie"

PROMPT À GEMINI:
"Tu es un agent agence voyages.
 Rédige réponse EMAIL pour ce client.
 Commence par 'Bonjour Marie,'
 Adapte ton au rôle (demande_devis = enthousiaste)
 Max 150 mots"

RÉPONSE DE GEMINI:
│
▼
┌─────────────────────────────────────────────────────┐
│ RÉPONSE SUGGÉRÉE POUR L'ADMIN                       │
├─────────────────────────────────────────────────────┤
│                                                      │
│ Bonjour Marie,                                       │
│                                                      │
│ Merci beaucoup de nous avoir contactés! Nous        │
│ sommes ravi d'apprendre que vous envisagez un       │
│ voyage de groupe en avril.                          │
│                                                      │
│ Nous avons plusieurs destinations de plage qui      │
│ seraient parfaites pour votre groupe de 15 pers:    │
│                                                      │
│ • Plages de Thessalonique (Grèce)                   │
│ • Côte Amalfitaine (Italie)                         │
│ • Îles Baléares (Espagne)                           │
│                                                      │
│ Je vais préparer un devis détaillé dans            │
│ les 24 heures.                                      │
│                                                      │
│ Cordialement,                                        │
│ L'équipe                                             │
│                                                      │
└─────────────────────────────────────────────────────┘
      │
      ▼
    ADMIN PEUT:
    ├─ ✅ Utiliser comme-ci
    ├─ ✏️ Modifier avant d'envoyer
    └─ ❌ Ecrire une nouvelle
```

---

## 6️⃣ Statuts et Priorités visuels

```
PRIORITÉ AFFICHÉE AUX ADMINS:

┌─────────────────────────┬──────────────┬──────────────────┐
│ Priorité                │ Couleur      │ Exemple          │
├─────────────────────────┼──────────────┼──────────────────┤
│ ⚠️ HAUTE                │ 🔴 ROUGE     │ Réclamation      │
│                         │              │ Urgence          │
│                         │              │ Groupe important │
├─────────────────────────┼──────────────┼──────────────────┤
│ → MOYENNE               │ 🟡 ORANGE    │ Question normale │
│                         │              │ Devis            │
├─────────────────────────┼──────────────┼──────────────────┤
│ ↓ BASSE                 │ 🟢 VERT      │ Info générale    │
│                         │              │ Newsletter       │
└─────────────────────────┴──────────────┴──────────────────┘

SENTIMENT AFFICHÉE AUX ADMINS:

┌─────────────────────┬──────────────┬──────────────────┐
│ Sentiment           │ Couleur      │ Approche         │
├─────────────────────┼──────────────┼──────────────────┤
│ 😊 POSITIF          │ 🟢 VERT      │ Enthousiaste    │
│                     │              │ Approfondir     │
├─────────────────────┼──────────────┼──────────────────┤
│ 😐 NEUTRE           │ 🟡 ORANGE    │ Factuel         │
│                     │              │ Informatif      │
├─────────────────────┼──────────────┼──────────────────┤
│ 😠 NÉGATIF          │ 🔴 ROUGE     │ Empathique      │
│                     │              │ Résoudre        │
└─────────────────────┴──────────────┴──────────────────┘

CATÉGORIES:

┌─────────────────┬──────────────────────────┐
│ Catégorie       │ Action de l'admin        │
├─────────────────┼──────────────────────────┤
│ 🚨 URGENT       │ Répondre IMMÉDIATEMENT  │
├─────────────────┼──────────────────────────┤
│ 💬 INFO         │ Informer le client      │
├─────────────────┼──────────────────────────┤
│ 😤 RÉCLAMATION  │ Excuses + solution      │
├─────────────────┼──────────────────────────┤
│ 📋 DEVIS        │ Proposer et vendre      │
└─────────────────┴──────────────────────────┘
```

---

## 7️⃣ Modèles de données simplifiés

```
DATABASE STRUCTURE:

users ──┬──────────────────────────┐
   │                               │
   ├─ conversations               │
   │  └─ messages (user/ai)       │
   │  └─ summaries               │
   │                               │
   └─ llm_logs                     │
   └─ interaction_logs            │

contact_messages (indépendant)
   ├─ message brut (name, email, message)
   ├─ classification IA (category, priority, sentiment)
   ├─ ai_summary (résumé Gemini)
   ├─ suggested_reply (réponse proposée)
   └─ auto_email_sent (confirmaton envoyée?)
```

---

## 8️⃣ Processus résumé en 1 image

```
CHAT IA                              CONTACT/ADMIN

User message                         Form submitted
    │                                    │
    ▼                                    ▼
Authenticate JWT                    Save message (FAST ✓)
    │                                    │
    ▼                                    ├─────────────────┐
Fetch conversation history           │                   │
    │                             Thread: Background work │
    ▼                        │ Classification      │ Email│
Analyze question             │ Gemini analyzes    │ Sent │
    │                        │ Registers reply    │      │
    ▼                        └────────┬────────────┘      │
Search destinations              Saves to DB              │
    │         ➜ Returns JSON      (3-5 sec later)       ▼
    ▼         with filters                         Client
Generate context                                    receives
    │                                              email
    ▼                        ➜ User doesn't wait   (5-10 sec)
Call Gemini API            ➜ Instant feedback
    │
    ▼
Get response (500-2000ms)
    │
    ▼
Save response + update timestamp
    │
    ▼
Return JSON to client
    │
    ▼
User sees reply in chat (Realtime)


RÉSUMÉ CLEF:
Chat = SYNCHRONE + temps réel
Contact = ASYNCHRONE + traitement en background
```

---

## 9️⃣ Intégrations External APIs

```
YOUR BACKEND
    │
    │
    ├─────────────────────────────────┬──────────────────────────┐
    │                                  │                          │
    ▼                                  ▼                          ▼
┌──────────────────────┐   ┌──────────────────────┐   ┌──────────────────────┐
│  GOOGLE GEMINI API   │   │  ALWAYSDATA SMTP     │   │   DATABASE (SQL)     │
├──────────────────────┤   ├──────────────────────┤   ├──────────────────────┤
│ Model:               │   │ Server:              │   │ PostgreSQL/MySQL     │
│ gemini-2.5-flash     │   │ smtp.alwaysdata.com  │   │ (Votre hébergeur)    │
│                      │   │ Port: 587            │   │                      │
│ Utilisé pour:        │   │ TLS: true            │   │ Stockage:            │
│ • Chat responses     │   │                      │   │ • Users              │
│ • Message analysis   │   │ Utilisé pour:        │   │ • Conversations      │
│ • Email generation   │   │ • Confirmation email │   │ • Messages           │
│ • Classification     │   │ • Admin replies      │   │ • Contact messages   │
│                      │   │                      │   │                      │
│ Coût: Pay-per-token │   │ Limité à X emails/j  │   │ Coût: Forfait        │
│ Gratuit jusqu'à un   │   │ (check limits)       │   │ (inclus)             │
│ certain quota        │   │                      │   │                      │
└──────────────────────┘   └──────────────────────┘   └──────────────────────┘
```

---

## 🔟 État d'un message Contact sur le timeline

```
0ms    └─ Form submitted
       └─ POST /api/contact

10ms   └─ ✅ Message saved to DB
       └─ ✅ Thread started
       └─ ✅ Response sent to client

100ms  └─ User sees "Message sent!" (confetti 🎉)

1000ms └─ Gemini starts classification

1500ms └─ Classification done
       └─ Gemini starts generating reply

2500ms └─ Reply generated
       └─ Gemini starts generating confirmation email

3500ms └─ Confirmation email generated
       └─ Saving to DB

4000ms └─ TODO: Send email to client

5000ms └─ 📧 Email sent to client!
       └─ "Thank you, we received your message"

5100ms └─ Admin dashboard updated
       └─ New message appears with colors:
          ├─ 🔴 Priority badge
          ├─ 🟢 Sentiment badge
          ├─ 📋 Category tag
          └─ ✨ Suggested reply visible
```

---

**Fin des schémas visuels** 🎨✨

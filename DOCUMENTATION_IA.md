# 📚 Documentation - Intégration IA du Projet PFAA

### 1️⃣ Routes principales (`routes/chat.py`)

#### **POST /api/chat/conversations**

Crée une nouvelle conversation de chat

```python
{
  "title": "Questions sur les destinations",
  "topic": "general"  # general, reservation, billing
}
```

**Réponse:**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "user_id": 123,
    "title": "Questions sur les destinations",
    "created_at": "2024-01-15 10:30"
  }
}
```

#### **GET /api/chat/conversations**

Liste toutes les conversations de l'utilisateur connecté

```python
# Résultat: Liste de toutes les conversations, triées par date décroissante
[
  { "id": 1, "title": "...", "updated_at": "2024-01-15" },
  { "id": 2, "title": "...", "updated_at": "2024-01-14" }
]
```

#### **GET /api/chat/conversations/<id>**

Récupère une conversation avec tous ses messages

```python
# Réponse:
{
  "success": true,
  "conversation": { ... },
  "messages": [
    { "id": 1, "sender_type": "user", "content": "Quelle est la meilleure destination en été?", "created_at": "10:30" },
    { "id": 2, "sender_type": "ai", "content": "Je vous recommande...", "created_at": "10:31" }
  ]
}
```

#### **POST /api/chat/message** ⭐ LA ROUTE PRINCIPALE

C'est la route clé qui gère l'envoi d'un message et obtient une réponse IA.

**Requête:**

```json
{
  "conversation_id": 1,
  "message": "Quelle est la meilleure destination pour un voyage romantique en été?"
}
```

**Étapes du traitement:**

```
1. Vérification de l'authentification JWT ✓
   └─> get_jwt_identity() pour récupérer user_id

2. Vérification que la conversation appartient à l'utilisateur ✓
   └─> Sécurité: empêcher l'accès aux conversations d'autres utilisateurs

3. Sauvegarde du message utilisateur en BD ✓
   └─> Message(sender_type='user', content=user_message)

4. Récupération de l'historique complet ✓
   └─> Tous les messages précédents de cette conversation

5. Formatage pour Gemini ✓
   └─> Conversion en format: [{'role': 'user', 'content': '...'}, ...]

6. Récupération du contexte utilisateur ⭐
   └─> Infos du client: nom, email

7. Appel à Gemini via get_ai_response() ⭐⭐⭐
   └─> Voir détails ci-dessous

8. Sauvegarde de la réponse IA en BD ✓
   └─> Message(sender_type='ai', content=ai_response)

9. Mise à jour de updated_at ✓
   └─> Pour tri par date plus tard
```

**Réponse:**

```json
{
  "success": true,
  "user_message": {
    "id": 10,
    "sender_type": "user",
    "content": "...",
    "created_at": "10:31"
  },
  "ai_message": {
    "id": 11,
    "sender_type": "ai",
    "content": "Voici mes recommandations pour un voyage romantique...",
    "created_at": "10:32"
  }
}
```

### 2️⃣ Fonction de réponse IA: `get_ai_response()`

C'est le **cœur du système de chat intelligent**.

**Code:**

```python
def get_ai_response(messages, user_context=""):
    # 1️⃣ Extraire le dernier message utilisateur
    user_message = messages[-1]["content"]

    # 2️⃣ ANALYSE INTELLIGENTE: Extraire filtres de la question
    filters = analyze_user_question(user_message)
    # Résultat: { "continent": "Europe", "type": "Beach", "season": "Summer", ... }

    # 3️⃣ RECHERCHE EN BD: Trouver destinations pertinentes
    destinations = search_destinations(filters)
    # Résultat: [Destination1, Destination2, ...]

    # 4️⃣ CONSTRUCTION DU CONTEXTE
    context = build_context(destinations)
    # Enrichissement avec profil utilisateur
    if user_context:
        context = f"Profil utilisateur: {user_context}\n\n{context}"

    # 5️⃣ HISTORIQUE DE CONVERSATION
    conversation = ""
    for msg in messages:
        role = "Client" if msg['role'] == 'user' else "Assistant"
        conversation += f"\n{role}: {msg['content']}"

    # 6️⃣ PROMPT Pour GEMINI (System + Contexte + Historique)
    system_prompt = f"""Tu es un assistant de support client professionnel...

    CONTEXTE IMPORTANT:
    {context}

    CONVERSATION ACTUELLE:{conversation}
    """

    # 7️⃣ APPEL À GEMINI
    response = model.generate_content(
        system_prompt,
        generation_config=GenerationConfig(
            temperature=0.7,    
        )
    )

    return response.text.strip()
```

### 3️⃣ Fonction d'analyse: `analyze_user_question()`

**Objectif:** Extraire des filtres intelligents de la question utilisateur

**Exemple:**

```
Input: "Je cherche une destination au Maroc pour une lune de miel en été, budget serré"

Output: {
  "continent": "Africa",
  "type": "Beach",
  "location": "Morocco",
  "season": "Summer",
  "budget": "cheap"
}
```

**Comment ça marche?**

1. Gemini analyse la question utilisateur
2. Retourne un JSON avec les filtres reconnus
3. Ces filtres sont utilisés dans `search_destinations()` pour faire une requête SQL intelligente

**Code du filtrage:**

```python
def search_destinations(filters):
    query = Destination.query

    if filters.get("continent"):
        query = query.filter(Destination.continent.ilike(...))

    if filters.get("type"):
        query = query.filter(Destination.type.ilike(...))

    if filters.get("season"):
        query = query.filter(Destination.bestSeason.ilike(...))

    if filters.get("budget"):
        if filters["budget"] == "cheap":
            query = query.filter(Destination.avgCostUSD < 100)
        elif filters["budget"] == "medium":
            query = query.filter(Destination.avgCostUSD.between(100, 300))

    return query.all()
```

### 4️⃣ Fonction de contexte: `build_context()`

Prend les destinations trouvées et les formate pour Gemini:

```python
context = """
    Destination: Paris
    Country: France
    Continent: Europe
    Type: Cultural
    Best Season: Spring
    Average Cost: $150
    Description: Capital de la France...

    Destination: Barcelona
    Country: Spain
    ...
"""
```

Ce contexte est inclus dans le prompt envoyé à Gemini pour qu'il formule des recommandations pertinentes.

### 5️⃣ Fonction de résumé: `summarize_conversation()`

#### **GET /api/chat/summary/<id>**

Génère un résumé intelligent d'une conversation complète.

**Étapes:**

1. Récupérer tous les messages de la conversation
2. Construire le texte complet
3. Envoyer à Gemini avec prompt de résumé
4. Parser la réponse (format spécifique: RÉSUMÉ: ... POINTS CLÉS: ...)
5. Sauvegarder dans ConversationSummary en BD


## 🔧 Système de Gestion des Contacts (Admin)

### Vue d'ensemble


1. ✅ Son message est **sauvegardé immédiatement**
2. 🔄 **En arrière-plan (async avec threading):**
   - Classifié par Gemini (catégorie, priorité, sentiment)
   - Résumé par Gemini
   - Une réponse suggérée est généré pour l'admin
   - Un email de confirmation est envoyé au client
3. 👨‍💼 **L'admin reçoit:**
   - La liste des messages avec badges couleur (priorité/sentiment)
   - Réponse suggérée prête à envoyer
   - Option pour envoyer une réponse personnalisée

### Architecture

```
┌────────────────────────────┐
│  FRONTEND (Angular)        │
│  Formulaire de contact     │
└────────────┬───────────────┘
             │
             ▼
   ┌──────────────────────┐
   │ POST /api/contact    │
   └──────────┬───────────┘
              │
      ┌───────▼─────────┐
      │ Sauvegarder BD  │
      │ (immédiat) ✓    │
      └───────┬─────────┘
              │
              ▼
   ┌──────────────────────┐
   │ Thread en parallèle  │ (ASYNC - n'attend pas)
   │ (ne bloque pas)      │
   └──────────┬───────────┘
              │
         ┌────┴─────────────────────────────────────┐
         │                                            │
         ▼                                            ▼
   ┌─────────────────┐                    ┌──────────────────┐
   │ Classification  │                    │ Génération Email │
   │ (Gemini)        │                    │ Confirmation     │
   │ - Catégorie     │                    │ (Gemini)         │
   │ - Priorité      │                    │                  │
   │ - Sentiment     │                    └──────────────────┘
   │ - Résumé        │                             │
   └─────────┬───────┘                            ▼
             │                         ┌──────────────────────┐
             │                         │ Envoi Email Client   │
             │                         │ (Flask-Mail)         │
             │                         └──────────────────────┘
             │
             ▼
   ┌────────────────────────┐
   │ Génération Réponse     │
   │ Suggérée pour Admin    │
   │ (Gemini)               │
   └────────────┬───────────┘
                │
                ▼
   ┌────────────────────────┐
   │ Sauvegarde en BD       │
   │ (CategoryMessage)      │
   └───────────────────────┘
```

### 1️⃣ Route de soumission: `POST /api/contact`

**Requête:**

```json
{
  "name": "Marie Dupont",
  "email": "marie@example.com",
  "phone": "+33 6 12 34 56 78",
  "subject": "Demande de devis groupe",
  "message": "Bonjour, nous sommes 15 personnes et cherchons une destination de plage pour avril..."
}
```

**Important:** La réponse est **immédiate** car le traitement IA se fait en **background thread** (ne bloque pas).

### 2️⃣ Traitement asynchrone: `process_with_ai()`

**Code simplifié:**

```python
def process_with_ai(app, message_id, name, email, subject, message):
    with app.app_context():
        # 1. Classification
        classification = classify_message(name, subject, message)
        category = classification["category"]        # urgent|info|reclamation|demande_devis
        priority = classification["priority"]        # haute|moyenne|basse
        sentiment = classification["sentiment"]      # positif|neutre|negatif
        ai_summary = classification["ai_summary"]    # résumé 1 phrase

        # 2. Réponse suggérée pour l'admin
        suggested_reply = generate_suggested_reply(...)

        # 3. Sauvegarde en BD
        update_ai_fields(message_id, category, priority, sentiment, ai_summary, suggested_reply)

        # 4. Email confirmation au client
        confirmation_body = generate_auto_confirmation(name, subject)
        send_confirmation_to_client(email, name, subject, confirmation_body)
```

**Cette fonction est appelée dans un thread:**

```python
thread = threading.Thread(
    target=process_with_ai,
    args=(app, message_id, name, email, subject, message)
)
thread.daemon = True
thread.start()  # Lance le thread sans attendre
```

### 3️⃣ Classification: `classify_message()`

**Objectif:** Analyser le message et le classer automatiquement

**Prompt envoyé à Gemini:**

**Catégories possibles:**

- **urgent**: Problème critique, réclamation sévère → Répondre ASAP
- **reclamation**: Client mécontent → Réponse empathique
- **demande_devis**: Demande commerciale → Réponse enthousiaste
- **info**: Question générale → Réponse informative

### 4️⃣ Génération de réponse suggérée: `generate_suggested_reply()`

**Objectif:** Générer une réponse professsionnelle pour l'admin

**Prompt:**

### 5️⃣ Génération de confirmation client: `generate_auto_confirmation()`

**Objectif:** Email de confirmation automatique envoyé au client ASAP

**Prompt:**

**Email reçu par le client (~5 sec après soumission):**

### 6️⃣ Routes d'administration

#### **GET /api/admin/notifications/count**

Nombre de messages non lus

```json
{ "unread_count": 3 }
```

#### **POST /api/admin/messages/<id>/reply**

Envoyer une réponse au client

### 🗄️ Modèles de données - Contact

**ContactMessage:**

```python
class ContactMessage(db.Model):
    __tablename__ = 'contact_messages'

    # Données du client
    id: int (Primary Key)
    name: str
    email: str
    phone: str
    subject: str
    message: str (le contenu)

    # Statut
    is_read: bool (défaut: False)

    # Champs IA (remplis par Gemini)
    category: str (urgent, info, reclamation, demande_devis)
    priority: str (haute, moyenne, basse)
    sentiment: str (positif, neutre, negatif)
    ai_summary: str (résumé 1 phrase)
    suggested_reply: str (réponse suggérée pour l'admin)

    # Email
    auto_email_sent: bool (confirmation envoyée au client?)

    created_at: datetime
```

---

## 🔌 Intégrations Technologiques

### 1️⃣ Google Gemini API

**Configuration:**

```python
import google.generativeai as genai

GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')  # À mettre dans .env
genai.configure(api_key=GEMINI_API_KEY)
model = genai.GenerativeModel('gemini-2.5-flash')
```

**Modèle utilisé: `gemini-2.5-flash`**

- ✅ Modèle léger et rapide
- ✅ Bon rapport coût/performance
- ✅ Supporte JSON structuré
- ✅ Parfait pour classification et génération

**Paramètres de génération:**

```python
GenerationConfig(
    temperature=0.7,        # Entre 0.2 (précis) et 0.7 (créatif)
    max_output_tokens=500   # Limiter la taille (optionnel)
)
```

### 2️⃣ Flask-Mail

**Configuration (dans `extensions.py`):**

```python
from flask_mail import Mail

mail = Mail(app)

```

**Fonction d'envoi:**

```python
def _send(to_email, to_name, subject, body):
    # Construit un email HTML pretty
    html_body = f"""
    <html>
    <body style="font-family:Arial;color:#333;">
        <div style="background:#1a3c5e;padding:20px;">
            <h2 style="color:white;">✈️ Votre Agence</h2>
        </div>
        <div style="background:#f9f9f9;padding:30px;">
            {body.replace(chr(10), '<br>')}
        </div>
    </body>
    </html>
    """

    msg = MailMessage(
        subject=subject,
        recipients=[to_email],
        html=html_body
    )
    mail.send(msg)
```

### 3️⃣ JWT Authentication

**Pour sécuriser les routes de chat:**


**Avantages:**

- ✅ Chaque utilisateur ne peut accéder qu'à ses conversations
- ✅ Protection contre l'accès non autorisé
- ✅ Enregistrement de qui fait quoi (audit trail)

---

## 📊 Flux de Données

### 1️⃣ Flux Chat (Temps réel)

```
+─────────────────────+
│  CLIENT (Frontend)  │
│  Saisit un message  │
+──────────┬──────────+
           │
           ▼
    ┌──────────────┐
    │ POST /message│
    │ + JWT token  │
    └──────────┬───┘
               │
        ┌──────▼────────────┐
        │ BACKEND FLASK     │
        │ Vérify JWT ✓      │
        └──────┬────────────┘
               │
    ┌──────────▼───────────┐
    │ Récupérer discussion │
    │ et tous les messages │
    └──────────┬───────────┘
               │
    ┌──────────▼──────────────────┐
    │ Appeler Gemini:             │
    │ - Analyser question         │
    │ - Chercher destinations     │
    │ - Générer réponse           │
    └──────────┬──────────────────┘
               │
               ▼
    ┌──────────────────────┐
    │ Sauvegarder messages │
    │ en BD (user + AI)    │
    └──────────┬───────────┘
               │
               ▼
    ┌──────────────────────┐
    │ Retourner au client  │
    │ JSON: user_message   │
    │       ai_message     │
    └──────────────────────┘
               │
               ▼
    ┌──────────────────────┐
    │ CLIENT (Frontend)    │
    │ Affiche le dialogue  │
    │ en temps réel ❤️     │
    └──────────────────────┘
```

**Temps total: ~500ms - 2s**

### 2️⃣ Flux Contact/Admin (Asynchrone)

```
+──────────────────────+
│  CLIENT (Frontend)   │
│  Remplir formulaire  │
+──────────┬───────────+
           │ POST /api/contact
           ▼
    ┌──────────────────┐
    │ BACKEND FLASK    │
    │ (synchrone)      │
    └──────────┬───────┘
               │
    ┌──────────▼──────────────┐
    │ Valider les données     │
    └──────────┬──────────────┘
               │
    ┌──────────▼──────────────┐
    │ Sauvegarder Message BD  │
    └──────────┬──────────────┘
               │
    ┌──────────▼──────────────────────────┐
    │ Lancer THREAD en parallèle (async)  │
    │ Ne pas attendre → réponse immédiate │
    └──────────┬──────────────────────────┘
               │
               ✅ Retour au client
               │  "Message envoyé!"
               │  (immédiat, ~100ms)
               │
    ┌──────────▼─────────────────────────────────────────┐
    │        THREAD WORKER (en arrière-plan)             │
    │         Faire le travail IA long                   │
    └──────────┬─────────────────────────────────────────┘
               │
    ┌──────────┴──────────────────────────────────┐
    │                                              │
    ▼                                              ▼
┌─────────────────────┐               ┌──────────────────────┐
│ Classification      │               │ Génération Email     │
│ (Gemini)            │               │ Confirmation         │
│ - Catégorie         │               │ (Gemini)             │
│ - Priorité          │               └──────────┬───────────┘
│ - Sentiment         │                          │
│ - Résumé            │               ┌──────────▼───────────┐
└──────────┬──────────┘               │ Envoi Email Client   │
           │                           │ (Flask-Mail)        │
           ▼                           │ ~5-10 secondes      │
┌─────────────────────────┐            └─────────────────────┘
│ Génération Réponse      │
│ Suggérée pour Admin     │
│ (Gemini)                │
│ ~2-3 secondes          │
└──────────┬──────────────┘
           │
           ▼
┌──────────────────────────┐
│ Sauvegarde BD            │
│ (ContactMessage)         │
│ - category              │
│ - priority              │
│ - sentiment             │
│ - ai_summary            │
│ - suggested_reply       │
└──────────┬───────────────┘
           │
           ✅ Traitement terminé
              (3-5 secondes total)

┌──────────────────────┐
│ ADMIN DASHBOARD      │
│                       │
│ Voit le message       │
│ - Badge priorité      │
│ - Badge sentiment     │
│ - Réponse suggérée    │
│ - Lui peut répondre   │
└──────────────────────┘
```

**Temps total: 3-5 secondes** (mais client ne voit que 100ms)

---

## 🎨 Intégration Frontend (Angular)

### Composant Chat

**Pseudo-code:**

```typescript
// chat.component.ts
export class ChatComponent {
  messages = [];

  // Charger une conversation
  loadConversation(id) {
    this.api.get(`/api/chat/conversations/${id}`).subscribe((response) => {
      this.messages = response.messages;
    });
  }

  // Envoyer un message
  sendMessage(text) {
    this.api
      .post(`/api/chat/message`, {
        conversation_id: this.currentConversationId,
        message: text,
      })
      .subscribe((response) => {
        // Ajouter le message utilisateur
        this.messages.push(response.user_message);
        // Ajouter la réponse IA
        this.messages.push(response.ai_message);
      });
  }
}
```

---

## 🔐 Sécurité

### Conversions

- ✅ Vérification JWT sur chaque requête
- ✅ Vérification que la conversation appartient à l'utilisateur
- ✅ Pas d'accès cross-user possible

### Contact

- ✅ Validation des emails (RFC 5322)
- ✅ Rate limiting recommandé (pour éviter spam)
- ✅ Champs obligatoires validés

##############################################################################################################
**Optimisations possibles:**

1. Cache l'historique côté client (websockets en temps réel)
2. Paginer les messages (charger 20 par 20)
3. Cache les destinations avec Redis
##############################################################################################################
## 🐛 Gestion des erreurs **Important:** Même si le traitement IA échoue, le message est toujours sauvegardé en BD.

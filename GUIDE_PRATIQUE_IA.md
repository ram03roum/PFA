# 🚀 Guide Pratique - Exemples Réels d'Utilisation

## Table des matières

1. [Exemple 1: Chat Client](#exemple-1--chat-avec-un-client)
2. [Exemple 2: Contact Form](#exemple-2--contact-form-soumis)
3. [Exemple 3: Admin Dashboard](#exemple-3--admin-dashboard)
4. [Cas spéciaux](#cas-spéciaux)
5. [Dépannage](#dépannage)

---

## Exemple 1 : Chat avec un client

### Scénario

**Utilisateur:** Alice (connectée, ID: 42)
**Action:** Ouvre un chat et pose une question

### Étape 1: Alice crée une nouvelle conversation

```
Frontend envoyé:
POST /api/chat/conversations
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
{
  "title": "Questions sur les destinations d'été",
  "topic": "general"
}

Backend répond:
200 OK
{
  "success": true,
  "data": {
    "id": 125,
    "user_id": 42,
    "title": "Questions sur les destinations d'été",
    "topic": "general",
    "status": "open",
    "created_at": "2024-01-15 14:30",
    "updated_at": "2024-01-15 14:30"
  }
}
```

**Quoi se passe en backend:**

```python
# routes/chat.py - create_conversation()
conv = Conversation(
    user_id=42,  # ← De JWT token
    title="Questions sur les destinations d'été",
    topic="general"
)
db.session.add(conv)
db.session.commit()
# conv.id = 125 (auto-increment)
```

**En BD:**

```sql
INSERT INTO conversations
(user_id, title, topic, status, created_at, updated_at)
VALUES
(42, 'Questions sur...', 'general', 'open', NOW(), NOW());
-- Retourne id=125
```

---

### Étape 2: Alice envoie un message

```
Frontend envoyé:
POST /api/chat/message
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
{
  "conversation_id": 125,
  "message": "Je suis d'origine marocaine et je cherche une destination de plage
             en été pour un voyage romantique avec mon copain. Notre budget est
             limité (moins de 500€ par personne). Quelles sont vos suggestions?"
}
```

**Quoi le backend fait:**

#### 1️⃣ Authentification + Sécurité

```python
@jwt_required()
def send_message():
    user_id = get_jwt_identity()  # ← 42

    # Vérifier que conversation appartient à cet utilisateur
    conv = Conversation.query.filter_by(
        id=125,
        user_id=42  # ← SÉCURITÉ! Pas d'accès cross-user
    ).first()

    if not conv:
        return {"error": "Conversation introuvable"}, 403
```

#### 2️⃣ Sauvegarder le message utilisateur

```python
user_msg = Message(
    conversation_id=125,
    sender_type='user',
    content="Je suis d'origine marocaine et je cherche..."
)
db.session.add(user_msg)
db.session.commit()
# user_msg.id = 1001 (auto-increment)

# En BD:
# INSERT INTO messages (conversation_id, sender_type, content, created_at)
# VALUES (125, 'user', '...', NOW());
```

#### 3️⃣ Récupérer l'historique (pour contexte)

```python
all_messages = Message.query.filter_by(conversation_id=125).all()
# Résultat: [user_msg] (seulement 1 pour l'instant)

# Construire format pour Gemini
messages_for_ai = [
    {
        'role': 'user',
        'content': 'Je suis d\'origine marocaine et je cherche...'
    }
]
```

#### 4️⃣ Récupérer contexte utilisateur

```python
user = User.query.get(42)
# user.name = "Alice Dupont"
# user.email = "alice@example.com"

context = f"Client: Alice Dupont, Email: alice@example.com"
```

#### 5️⃣ Appeler `get_ai_response()` ⭐ LE CŒUR

```python
ai_response = get_ai_response(messages_for_ai, context)
```

**Détail de `get_ai_response()`:**

```python
def get_ai_response(messages, user_context):
    # 1. Extraire dernier message
    user_message = "Je suis d'origine marocaine et je cherche..."

    # 2. ANALYSER LA QUESTION (ask Gemini)
    # Prompt envoyé à Gemini:
    # "Analyse cette demande de voyage et retourne JSON avec:
    #  - continent
    #  - type de voyage
    #  - saison
    #  - budget
    #  - location"

    filters = analyze_user_question(user_message)
    # Gemini répond:
    # {
    #   "continent": "Africa",
    #   "type": "Beach",
    #   "location": "Morocco",
    #   "season": "Summer",
    #   "budget": "cheap"  ← 500€ = cheap
    # }

    # 3. CHERCHER DESTINATIONS en BD
    destinations = search_destinations(filters)
    # SELECT * FROM destinations
    # WHERE continent LIKE '%Africa%'
    #   AND type LIKE '%Beach%'
    #   AND bestSeason LIKE '%Summer%'
    #   AND avgCostUSD < 100  ← budget cheap = < 100$
    #
    # Résultat: [
    #   Destination{
    #     id: 5,
    #     name: "Marrakech",
    #     country: "Morocco",
    #     continent: "Africa",
    #     type: "Beach",
    #     bestSeason: "Summer",
    #     avgCostUSD: 80,
    #     Description: "Beautiful beaches..."
    #   },
    #   Destination{id: 8, name: "Agadir", ...},
    #   Destination{id: 12, name: "Essaouira", ...}
    # ]

    # 4. CONSTRUIRE CONTEXTE
    context_str = """
    Destination: Marrakech
    Country: Morocco
    Continent: Africa
    Type: Beach
    Best Season: Summer
    Average Cost: 80 USD
    Description: Beautiful beaches...

    Destination: Agadir
    ...
    """

    # Enrichir avec profil
    if user_context:
        context_str = f"Profil utilisateur: {user_context}\n\n{context_str}"
    # →
    # "Profil utilisateur: Client: Alice Dupont, Email: alice@example.com
    #
    #  Destination: Marrakech..."

    # 5. CONSTRUIRE HISTORIQUE
    conversation_str = "Client: Je suis d'origine marocaine et je cherche..."

    # 6. ENVOYER À GEMINI
    system_prompt = f"""
    Tu es un assistant de support client professionnel pour une agence de voyages.
    Sois courtois, utile et respectueux.

    CONTEXTE IMPORTANT:
    {context_str}

    CONVERSATION ACTUELLE:
    {conversation_str}

    Maintenant, réponds à la dernière question de manière concise et utile.
    """

    response = model.generate_content(
        system_prompt,
        generation_config=GenerationConfig(
            temperature=0.7,
            # max_output_tokens=500
        )
    )

    # Gemini répond:
    # "Bonjour Alice!
    #
    #  Quelle excellente idée! Étant d'origine marocaine, vous apprécierez
    #  certainement revenir à la maison pour des vacances en amoureux.
    #
    #  Je vous recommande:
    #
    #  1. **Marrakech** - 80$ par jour
    #     Les belles plages méditerranéennes, la culture authentique...
    #
    #  2. **Agadir** - 75$ par jour
    #     Plus moderne, infrastructure touristique développée...
    #
    #  3. **Essaouira** - 85$ par jour
    #     Charme côtier, atmosphère bohème...
    #
    #  Toutes ces destinations sont parfaites pour l'été et votre budget!
    #  N'hésitez pas si vous avez des questions."

    return response.text.strip()
```

#### 6️⃣ Sauvegarder la réponse IA

```python
ai_msg = Message(
    conversation_id=125,
    sender_type='ai',
    content="Bonjour Alice! Quelle excellente idée..."
)
db.session.add(ai_msg)

# Mettre à jour timestamp de conversation
conv.updated_at = datetime.utcnow()
db.session.commit()

# En BD:
# INSERT INTO messages (conversation_id, sender_type, content, created_at)
# VALUES (125, 'ai', 'Bonjour Alice...', NOW());
#
# UPDATE conversations
# SET updated_at = NOW()
# WHERE id = 125;
```

#### 7️⃣ Retourner au client

```python
return jsonify({
    'success': True,
    'user_message': {
        'id': 1001,
        'sender_type': 'user',
        'content': 'Je suis d\'origine marocaine...',
        'created_at': '14:30'
    },
    'ai_message': {
        'id': 1002,
        'sender_type': 'ai',
        'content': 'Bonjour Alice! Quelle excellente idée...',
        'created_at': '14:31'
    }
}), 201
```

### Résultat final dans le chat

```
┌─────────────────────────────────────────────────────┐
│  Chat - Questions sur les destinations d'été       │
├─────────────────────────────────────────────────────┤
│                                                      │
│ [14:30] 👤 Alice:                                   │
│ Je suis d'origine marocaine et je cherche une       │
│ destination de plage en été pour un voyage          │
│ romantique avec mon copain. Notre budget est        │
│ limité (moins de 500€ par personne). Quelles       │
│ sont vos suggestions?                               │
│                                                      │
│ [14:31] 🤖 Assistant:                               │
│ Bonjour Alice!                                       │
│                                                      │
│ Quelle excellente idée! Étant d'origine marocaine,  │
│ vous apprécierez certainement revenir à la maison   │
│ pour des vacances en amoureux.                       │
│                                                      │
│ Je vous recommande:                                  │
│                                                      │
│ 1. Marrakech - 80$ par jour                          │
│    Les belles plages méditerranéennes, la          │
│    culture authentique...                            │
│                                                      │
│ 2. Agadir - 75$ par jour                             │
│    Plus moderne, infrastructure touristique...       │
│                                                      │
│ 3. Essaouira - 85$ par jour                          │
│    Charme côtier, atmosphère bohème...               │
│                                                      │
│ Toutes ces destinations sont parfaites pour         │
│ l'été et votre budget!                              │
│                                                      │
│ Je peux vous aider à réserver? 🏖️                    │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

## Exemple 2 : Contact Form soumis

### Scénario

**Utilisateur:** Mohamed (NOT connecté)
**Action:** Remplit formulaire de contact pour demander un devis groupe

### Étape 1: Mohamed remplit et soumet le formulaire

```
Frontend envoyé:
POST /api/contact
Content-Type: application/json
{
  "name": "Mohamed Samir",
  "email": "m.samir@company.com",
  "phone": "+33 6 87 65 43 21",
  "subject": "Devis groupe voyage Berlin",
  "message": "Bonjour,

Nous sommes 20 personnes d'une école professionnelle et
nous cherchons à organiser un voyage à Berlin en mai pour
une sortie classe.

Nous avons un budget limité d'environ 800€ par personne
(transport + hôtel + visites).

Pourriez-vous nous proposer un devis complet?

Merci d'avance!

Mohamed Samir
Organisateur sortie scolaire"
}
```

### Étape 2: Backend - Sauvegarde immédiate

```python
# routes/contact.py - submit_contact()

# 1. Valider
if not name or not email or not message:
    return {"error": "Champs requis"}, 400

# 2. Sauvegarder en BD (TRÈS RAPIDE)
def save_message(name, email, phone, subject, message):
    msg = ContactMessage(
        name="Mohamed Samir",
        email="m.samir@company.com",
        phone="+33 6 87 65 43 21",
        subject="Devis groupe voyage Berlin",
        message="Bonjour, Nous sommes 20 personnes..."
    )
    db.session.add(msg)
    db.session.commit()
    return msg.id  # ← 789

message_id = 789
```

**En BD immédiatement:**

```sql
INSERT INTO contact_messages
(name, email, phone, subject, message, created_at)
VALUES
('Mohamed Samir', 'm.samir@company.com', '+33...',
 'Devis groupe voyage Berlin', 'Bonjour...', NOW());
-- Retourne id=789
```

### Étape 3: Response immédiate au client

```
Frontend reçoit:
200 OK
{
  "success": true,
  "message": "Message envoyé avec succès"
}

Temps: ~50-100ms ✅
```

**Frontend affiche:** ✅ "Votre message a été reçu!"

### Étape 4: Thread en arrière-plan lance

```python
# Toujours dans submit_contact()
thread = threading.Thread(
    target=process_with_ai,
    args=(app, message_id=789, name="Mohamed Samir", email="m.samir@company.com", ...)
)
thread.daemon = True
thread.start()  # ← Lance sans attendre
# Contrôle retourne au client IMMÉDIATEMENT

# Le thread travaille en parallèle (3-5 secondes)
```

### Étape 5: Classification dans le thread

```python
# process_with_ai() - Lance après ~500ms

# 1️⃣ CLASSIFICATION
# Prompt Gemini:
# "Classifie ce message client:
#  Nom: Mohamed Samir
#  Sujet: Devis groupe voyage Berlin
#  Message: Nous sommes 20 personnes...
#
#  Retourne: {category, priority, sentiment, ai_summary}"

classification = classify_message(
    "Mohamed Samir",
    "Devis groupe voyage Berlin",
    "Bonjour, Nous sommes 20 personnes..."
)

# Gemini analyse et répond:
# {
#   "category": "demande_devis",     ← Commercial!
#   "priority": "haute",             ← 20 personnes = client important!
#   "sentiment": "positif",          ← Poli, enthousiaste
#   "ai_summary": "Groupe scolaire 20 pers demande devis Berlin mai 800€/pers"
# }

print("📊 Classification: demande_devis, haute priorité, sentiment positif")
```

### Étape 6: Génération réponse suggérée

```python
# 2️⃣ GENERATED SUGGESTED REPLY
# Prompt Gemini:
# "Tu es agent agence voyage professionnelle.
#  Rédige réponse email pour ce client.
#  Nom: Mohamed Samir
#  Sujet: Devis groupe Berlin
#  Message: 20 personnes...
#  Catégorie: demande_devis
#  Sentiment: positif
#
#  Règles:
#  - Commence par 'Bonjour Mohamed,'
#  - Enthousiaste (demande_devis)
#  - 150 mots max
#  - Pas de Markdown"

suggested_reply = generate_suggested_reply(
    "Mohamed Samir",
    "Devis groupe voyage Berlin",
    "Bonjour, Nous sommes 20 personnes...",
    "demande_devis",
    "positif"
)

# Gemini répond:
# "Bonjour Mohamed,
#
#  Quelle excellente initiative! Nous serions ravi
#  d'organiser votre sortie classe à Berlin.
#
#  Votre budget de 800€ par personne est très
#  réaliste pour Berlin. En mai, c'est la meilleure
#  saison pour visiter la ville.
#
#  Je vais préparer un devis détaillé incluant:
#  • Transport (train ou autocar)
#  • Hôtel 4 étoiles (75€/nuit)
#  • Visites guidées (Mur, Reichstag, musées)
#  • Assurance voyage
#
#  Vous recevrez le devis complet dans 24 heures.
#
#  Cordialement,
#  Votre agence de voyage"

print("✍️ Réponse suggérée générée")
```

### Étape 7: Email de confirmation client

```python
# 3️⃣ AUTO-CONFIRMATION EMAIL
# Prompt Gemini:
# "Rédige email de confirmation réception pour cm client.
#  Nom: Mohamed Samir
#  Sujet: Devis groupe voyage Berlin
#
#  Règles:
#  - Confirme réception
#  - Indique réponse sous 24h
#  - Chaleureux, professionnel
#  - 100 mots max"

confirmation_body = generate_auto_confirmation(
    "Mohamed Samir",
    "Devis groupe voyage Berlin"
)

# Gemini répond:
# "Bonjour Mohamed,
#
#  Nous avons bien reçu votre message ce matin.
#
#  Notre équipe étudiera votre demande de devis
#  pour 20 personnes à Berlin et vous répondra
#  dans les 24 heures avec une proposition
#  complète et adaptée.
#
#  À très bientôt!
#
#  L'équipe de votre agence de voyage"

# 4️⃣ ENVOI EMAIL
sent = send_confirmation_to_client(
    to_email="m.samir@company.com",
    to_name="Mohamed Samir",
    subject="Devis groupe voyage Berlin",
    body=confirmation_body
)

print("📧 Email confirmation envoyé à Mohamed")
```

### Étape 8: Sauvegarde en BD

```python
# 5️⃣ SAUVEGARDE EN BD
update_ai_fields(
    message_id=789,
    category="demande_devis",
    priority="haute",
    sentiment="positif",
    ai_summary="Groupe scolaire 20 pers Berlin mai 800€/pers",
    suggested_reply="Bonjour Mohamed, Quelle excellente..."
)

# UPDATE contact_messages
# SET category = 'demande_devis',
#     priority = 'haute',
#     sentiment = 'positif',
#     ai_summary = 'Groupe scolaire...',
#     suggested_reply = 'Bonjour Mohamed...'
# WHERE id = 789;

mark_email_sent(789)
# UPDATE contact_messages
# SET auto_email_sent = True
# WHERE id = 789;

print("✅ Message 789 entièrement traité!")
```

### Résumé Timeline

```
00ms  ├─ Mohamed soumet le formulaire
      └─ Frontend envoie POST /api/contact

15ms  ├─ Backend sauvegarde en BD
      └─ Thread lancé

50ms  └─ ✅ Response au client: "Message envoyé!"

500ms ├─ Thread Gemini: Classification

1000ms├─ Thread Gemini: Réponse suggérée

1500ms├─ Thread Gemini: Email confirmation

2000ms├─ Thread: Sauvegarde BD

2500ms├─ ✅ Thread terminé
      │
      └─ Admin vue détail: ⏳ En attente du mail...

3000ms└─ 📧 Email reçu par Mohamed!
      └─ "Bonjour Mohamed, nous avons reçu votre message..."

3100ms└─ 👨‍💼 Admin dashboard mise à jour:
         •  Nome: Mohamed Samir
         •  Priorité: 🔴 HAUTE
         •  Sentiment: 🟢 POSITIF
         •  Category: 📋 DEVIS
         •  Suggested reply visible
```

---

## Exemple 3 : Admin Dashboard

### Avant traitement (client vient de soumettre)

```
GET /api/admin/notifications/count
Response:
{
  "unread_count": 1
}

GET /api/admin/messages
Response:
{
  "messages": [
    {
      "id": 789,
      "name": "Mohamed Samir",
      "email": "m.samir@company.com",
      "subject": "Devis groupe voyage Berlin",
      "message": "Bonjour, Nous sommes 20 personnes...",
      "is_read": false,
      "category": null,           ← Pas encore classifié
      "priority": null,
      "sentiment": null,
      "ai_summary": null,
      "suggested_reply": null,
      "auto_email_sent": false,   ← Pas encore envoyé
      "created_at": "2024-01-15 15:30"
    }
  ]
}
```

### Après traitement (5 secondes plus tard)

```
GET /api/admin/messages
Response:
{
  "messages": [
    {
      "id": 789,
      "name": "Mohamed Samir",
      "email": "m.samir@company.com",
      "subject": "Devis groupe voyage Berlin",
      "message": "Bonjour, Nous sommes 20 personnes...",
      "is_read": false,
      "category": "demande_devis",           ← ✅ Classifié!
      "priority": "haute",                   ← 🔴 Badge rouge
      "sentiment": "positif",                ← 🟢 Badge vert
      "ai_summary": "Groupe scolaire 20 pers Berlin mai 800€/pers",
      "suggested_reply": "Bonjour Mohamed,\n\nQuelle excellente...",
      "auto_email_sent": true,               ← ✅ Email envoyé
      "created_at": "2024-01-15 15:30"
    }
  ]
}
```

### Affichage dans l'interface Admin

```
┌────────────────────────────────────────────────────────────────────┐
│  📬 Admin Messages Dashboard                                        │
├────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Unread: 1                                                          │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────┐   │
│  │ 🔴 HAUTE | 🟢 POSITIF | 📋 DEVIS                   📧 Envoyé │   │
│  │                                                               │   │
│  │ 👤 Mohamed Samir (m.samir@company.com)                       │   │
│  │ 📞 +33 6 87 65 43 21                                         │   │
│  │ 📅 15/01/2024 à 15:30                                        │   │
│  │                                                               │   │
│  │ Sujet: Devis groupe voyage Berlin                            │   │
│  │                                                               │   │
│  │ Message: Bonjour, Nous sommes 20 personnes d'une école      │   │
│  │          professionnelle et nous cherchons à organiser      │   │
│  │          un voyage à Berlin en mai...                        │   │
│  │                                                               │   │
│  │ 📊 IA Analysis:                                              │   │
│  │ • Category: Demande de devis                                │   │
│  │ • Priority: Haute (groupe de 20)                             │   │
│  │ • Sentiment: Positif                                         │   │
│  │ • Summary: Groupe scolaire 20 pers Berlin mai 800€/pers     │   │
│  │                                                               │   │
│  │ ┌──────────────────────────────────────────────┐            │   │
│  │ │ Suggested Reply (from AI)                     │            │   │
│  │ ├──────────────────────────────────────────────┤            │   │
│  │ │ Bonjour Mohamed,                              │            │   │
│  │ │                                                │            │   │
│  │ │ Quelle excellente initiative! Nous serions    │            │   │
│  │ │ ravi d'organiser votre sortie classe à Berlin.│            │   │
│  │ │                                                │            │   │
│  │ │ Votre budget de 800€ par personne est très    │            │   │
│  │ │ réaliste pour Berlin. En mai, c'est la        │            │   │
│  │ │ meilleure saison pour visiter la ville.       │            │   │
│  │ │                                                │            │   │
│  │ │ Je vais préparer un devis détaillé incluant:  │            │   │
│  │ │ • Transport (train ou autocar)                │            │   │
│  │ │ • Hôtel 4 étoiles (75€/nuit)                  │            │   │
│  │ │ • Visites guidées (Mur, Reichstag, musées)   │            │   │
│  │ │ • Assurance voyage                             │            │   │
│  │ │                                                │            │   │
│  │ │ Vous recevrez le devis complet dans 24h.      │            │   │
│  │ │                                                │            │   │
│  │ │ Cordialement,                                  │            │   │
│  │ │ Votre agence de voyage                         │            │   │
│  │ └──────────────────────────────────────────────┘            │   │
│  │                                                               │   │
│  │ [✏️ Edit] [✅ Send as-is] [🔄 Generate new]                 │   │
│  │                                                               │   │
│  └────────────────────────────────────────────────────────────┘   │
│                                                                      │
└────────────────────────────────────────────────────────────────────┘
```

### Admin envoie une réponse

```
Frontend envoyé:
POST /api/admin/messages/789/reply
Content-Type: application/json
{
  "reply": "Bonjour Mohamed,

Quelle excellente initiative! Nous serions ravi
d'organiser votre sortie classe à Berlin.

Votre budget de 800€ par personne est très
réaliste pour Berlin. En mai, c'est la meilleure
saison pour visiter la ville.

Je vais préparer un devis détaillé incluant:
• Vol aller-retour Paris ↔ Berlin
• Hôtel 4 étoiles (75€/nuit x 3 nuits = 225€)
• Visites guidées (Mur, Reichstag, musées)
• Transport local (métro, bus)
• Assurance voyage AXA

Budget estimé: 790€/personne

Vous recevrez le devis complet dedemain matin.

Cordialement,
Pierre Dupont
Directeur Agence"
}

Backend envoie:
┌─────────────────────────────────────────────┐
│ EMAIL ENVOYÉ À: m.samir@company.com         │
├─────────────────────────────────────────────┤
│ Subject: Re: Devis groupe voyage Berlin     │
│                                              │
│ Bonjour Mohamed,                            │
│                                              │
│ Quelle excellente initiative!...             │
│                                              │
│ Budget estimé: 790€/personne                │
│                                              │
│ Vous recevrez le devis complet demain matin.│
│                                              │
│ Cordialement,                               │
│ Pierre Dupont                                │
│ Directeur Agence                            │
└─────────────────────────────────────────────┘

Response:
200 OK
{
  "success": true,
  "message": "Réponse envoyée au client"
}
```

---

## Cas spéciaux

### Cas 1: Message urgent avec sentiment négatif

```
Entrée:
{
  "name": "Jean Luc",
  "subject": "URGENCE - Paiement refusé!",
  "message": "Bonjour, je tente payer ma réservation depuis 2h et
             le serveur dit 'paiement refusé'. Je dois partir demain!!!
             Très déçu de votre service!"
}

Classification Gemini:
{
  "category": "urgent",
  "priority": "haute",
  "sentiment": "negatif",
  "ai_summary": "Client cherche résoudre erreur paiement immédiatement"
}

Suggested reply (tone: EMPATHIQUE + RÉACTIF):
"Jean Luc,

Je suis sincèrement désolé pour cette situation frustrante!

J'ai immédiatement contacté notre équipe technique. Votre paiement
a effectivement échoué dus à un problème de serveur.

J'ai débloqué votre réservation manuellement. Vous pouvez partir
en confiance demain.

Je vous envoie une réduction de 10% sur votre prochain voyage
en excuse.

Merci de votre patience et désolé pour le désagrément.

Cordialement,"

Affichage Admin:
🔴 HAUTE | 🔴 NÉGATIF | 🚨 URGENT   [À traiter NOW!]
```

### Cas 2: Demande devis avec sentiment neutre

```
Entrée:
{
  "name": "ACME Corp",
  "subject": "Demande de devis séminaire entreprise",
  "message": "Nous cherchons prestataire pour séminaire 50 personnes.
             Chiffre interéssé par propositions et tarifs.
             Format: email."
}

Classification Gemini:
{
  "category": "demande_devis",
  "priority": "haute",
  "sentiment": "neutre",
  "ai_summary": "Entreprise 50 pers demande devis séminaire"
}

Suggested reply (tone: PROFESSIONNEL + INFORMATIF):
"Madame, Monsieur,

Merci de votre intérêt pour nos services.

Nous avons une expérience confirmée dans l'organisation
de séminaires entreprise.

Pour 50 personnes, nous proposons des forfaits clés en main incluant:
• Hébergement (hôtel 3-5 étoiles)
• Restauration (petit-déj, déj, dîner)
• Salle de conférence équipée
• Activités de team building
• Assurance

Je vous envoie notre catalogue de destinations et tarifs
dans l'après-midi.

À bientôt,"
```

### Cas 3: Email spam/nonsense

```
Entrée:
{
  "name": "Bob",
  "subject": "Buy viagra cheap!!!",
  "message": "Click here for cheap drugs!!!!"
}

Classification Gemini:
{
  "category": "info",
  "priority": "basse",
  "sentiment": "neutre",
  "ai_summary": "Spam message"
}

Suggested reply:
"Merci de votre message. Ce service n'est pas disponible.

Cordialement,"

Affichage Admin:
🟢 BASSE | 🟡 NEUTRE | 💬 INFO  [Spam - Supprimer]
```

---

## Dépannage

### Problème 1: Gemini ne répond pas au chat

**Symptômes:**

```
❌ "Désolé, une erreur est survenue"
```

**Diagnostic:**

```python
# 1. Vérifier clé API
echo $GEMINI_API_KEY
# Doit retourner quelque chose

# 2. Tester connexion Gemini
from services.ai_service import test_gemini_connection
test_gemini_connection()
# Doit retourner: ✅ Connexion Gemini OK

# 3. Vérifier logs Flask
# Les logs doivent être clairs sur l'erreur
```

**Solutions:**

```bash
# Ajouter clé API manquante
export GEMINI_API_KEY="your_key"

# Ou en .env:
GEMINI_API_KEY=your_gemini_key_here

# Redémarrer Flask:
python app.py
```

### Problème 2: Mails non reçus

**Symptômes:**

```
❌ "Email non envoyé à alice@example.com"
```

**Diagnostic:**

```python
# 1. Tester configuration mail
from extensions import mail
from flask_mail import Message

with app.app_context():
    msg = Message(
        subject="Test",
        recipients=["test@example.com"],
        body="Test"
    )
    mail.send(msg)
    # Doit retourner sans erreur
```

**Solutions:**

```bash
# Vérifier .env
MAIL_SERVER=smtp.alwaysdata.com
MAIL_PORT=587
MAIL_USE_TLS=True
MAIL_USERNAME=your_email@domain.com
MAIL_PASSWORD=your_password

# Vérifier dossier SPAM
# Mails peuvent aller en spam!

# Check AlwaysData limits
# Peut y avoir un quota d'emails/jour
```

### Problème 3: Classification mauvaise

**Exemple:**

```
Message: "Merci pour le voyage! Magnifique!"
Résultat: category=reclamation, sentiment=negative
❌ FAUX! C'est un compliment!
```

**Solution:**

```python
# Améliorer le prompt dans ai_service.py

def classify_message(name, subject, message):
    prompt = f"""Tu es expert en traitement customer service.
    Analyse TRÈS ATTENTIVEMENT ce message.

    Attention spéciale:
    - Un merci = positive/info, JAMAIS reclamation
    - Un compliment = positive
    - Une plainte = negative
    - Urgence = urgentcategory

    Nom: {name}
    Sujet: {subject}
    Message: {message}

    Retourne JSON...
    """
    # ...
```

### Problème 4: Performance lente

**Symptômes:**

```
Chat répond en 5+ secondes
Ou: Contact form attend 10+ secondes
```

**Diagnostic:**

```python
# Vérifier logs de timing
# Dans ai_service.py, ajouter timing
import time

def get_ai_response(...):
    start = time.time()

    # Gemini appel
    response = model.generate_content(...)

    elapsed = time.time() - start
    print(f"⏱️ Gemini response time: {elapsed:.2f}s")
    # Doit être < 2 sec
```

**Solutions:**

```python
# 1. Réduire taille prompt (moins de messages)
# 2. Réduire max_output_tokens
# 3. Utiliser cache Redis pour résultats similaires
# 4. Vérifier votre connexion internet
```

---

**Fin du guide pratique** 🎯✨

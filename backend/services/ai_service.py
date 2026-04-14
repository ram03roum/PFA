# import google.generativeai as genai #Permet de communiquer avec Google Gemini
# import os #Permet de récupérer les variables d’environnement (.env)
# from models import Destination
# import json

# # ✨ RESPONSABILITÉS:
# # 1. Gérer la connexion à Gemini (clé API, modèle)
# # 2. Formater les messages pour Gemini
# # 3. Appeler Gemini et traiter la réponse
# # 4. Résumer les conversations



# # Configuration Gemini
# #Récupération de la clé API
# GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')
# if not GEMINI_API_KEY:
#     raise ValueError("❌ GEMINI_API_KEY non trouvée dans .env")

# #Configuration du modèle
# genai.configure(api_key=GEMINI_API_KEY)

# GEMINI_MODEL='gemini-2.5-flash'
# try:
#     model = genai.GenerativeModel('gemini-2.5-flash')
# except Exception as e:
#     # Attempt to list available models to provide a clearer error message
#     available = None
#     try:
#         # try common listing method names; some SDK versions differ
#         if hasattr(genai, 'list_models'):
#             available = genai.list_models()
#         elif hasattr(genai, 'get_models'):
#             available = genai.get_models()
#     except Exception:
#         available = None

#     models_list = None
#     try:
#         # If listing returned an iterable of model objects or names, normalize it
#         if available:
#             if isinstance(available, dict) and 'models' in available:
#                 models_list = [m.get('name') or str(m) for m in available['models']]
#             else:
#                 models_list = [getattr(m, 'name', str(m)) for m in available]
#     except Exception:
#         models_list = None

#     more = f" Available models: {models_list}" if models_list else ""
#     raise RuntimeError(f"Failed to initialize Gemini model '{GEMINI_MODEL}': {e}.{more}") from e


# def get_ai_response(messages, user_context=""):
#     """
#     Génère une réponse IA avec Gemini en utilisant RAG.
    
#     Args:
#         messages: liste de dicts {'role': 'user'/'assistant', 'content': 'texte'}
#         user_context: infos supplémentaires sur le client
    
#     Returns:
#         str: réponse Gemini
#     """
    
#     # 1️⃣ Dernier message utilisateur (pour extraire filtres)
#     user_message = messages[-1]["content"]
#     # 2️⃣ Analyse de la question pour créer filtres
#     filters = analyze_user_question(user_message)

#     # 3️⃣ Récupération des destinations depuis la DB
#     destinations = search_destinations(filters)

#     # 4️⃣ Construction du contexte pour Gemini
#     context = build_context(destinations)

#     # Ajout du contexte utilisateur si fourni
#     if user_context:
#         context = f"Profil utilisateur: {user_context}\n\n{context}"

#     # 5️⃣ Historique complet de la conversation
#     conversation = ""
#     for msg in messages:
#         role = "Client" if msg['role'] == 'user' else "Assistant"
#         conversation += f"\n{role}: {msg['content']}"

#     # 6️⃣ Prompt pour Gemini
#     system_prompt = f"""Tu es un assistant de support client professionnel pour une agence de voyages.
# Sois courtois, utile et respectueux.
# Aide les clients avec les réservations, les destinations, les paiements et les questions générales.
#     Règles importantes :
#     - Réponds de façon claire.
#     # - N'utilise PAS de Markdown.
#     # - N'utilise PAS de ** ou *.
#     - Saute UNE LIGNE entre chaque destination.
#     - Chaque destination doit commencer sur une NOUVELLE LIGNE.
#     - Utilise ce format précis :
#     * 📍 **Nom de la destination** : Prix $
#     _Petit descriptif rapide_
#     - Utilise des listes à puces pour énumérer les destinations.
#     - Mets les noms des destinations et les prix en GRAS.
#     - Utilise des emojis pour rendre la réponse chaleureuse (ex: 🌴, ✈️, 💰).
#     - Sépare les destinations par des lignes horizontales si nécessaire.
#     - utilise des phrases courtes et simples pour être facilement compréhensible.
#     - utilise des tirés et retours à la ligne pour les listes et évite les longs paragraphes.
#     - Utilise des phrases simples.
#     - Utilise des listes à puces (asterisques).
#     - Mets les noms des destinations et les prix en GRAS (**texte**).
#     - Ajoute toujours le symbole $ après les prix.
#     - Ajoute un emoji pertinent au début de chaque ligne.
# CONTEXTE IMPORTANT:
# {context}

# CONVERSATION ACTUELLE:{conversation}

# Maintenant, réponds à la dernière question du client de manière concise et utile."""

#     try:
#         response = model.generate_content(
#             system_prompt,
#             generation_config=genai.types.GenerationConfig(
#                 temperature=0.7,
#                 # max_output_tokens=500
#             )
#         )
#         return response.text.strip() if response.text else "Désolé, je n'ai pas pu générer de réponse."
#     except Exception as e:
#         print(f"❌ ERREUR GEMINI: {e}")
#         return f"Désolé, une erreur est survenue: {e}"


# def summarize_conversation(messages):
#     """
#     Résume une conversation complète avec Gemini (GRATUIT)
    
#     Args:
#         messages: List de dicts {'role': 'user'/'assistant', 'content': 'texte'}
    
#     Returns:
#         dict: {'summary': str, 'key_points': list}
#     """
    
#     # Construire le texte de la conversation
#     # Résumer discussion admin ↔ client
#     # RÉSUMÉ:
#     # POINTS CLÉS:
#     conversation = ""
#     for msg in messages:
#         role = "Client" if msg['role'] == 'user' else "Assistant"
#         conversation += f"{role}: {msg['content']}\n"
    
#     prompt = f"""Résume la conversation suivante de manière concise (maximum 150 mots).
# Ensuite, identifie 3-5 points clés de la discussion.

# Format de réponse TRÈS IMPORTANT:
# RÉSUMÉ: [votre résumé ici - max 150 mots]
# POINTS CLÉS: [point1], [point2], [point3], [point4]

# CONVERSATION À RÉSUMER:
# {conversation}

# Réponds maintenant dans le format exact spécifié ci-dessus."""
    
#     try:
#         model_summary = genai.GenerativeModel('gemini-2.5-flash')
#         response = model_summary.generate_content(
#             prompt,
#             generation_config=genai.types.GenerationConfig(
#                 temperature=0.7,
#                 # max_output_tokens=300
#             )
#         )
        
#         if not response.text:
#             return {
#                 'summary': 'Impossible de générer le résumé',
#                 'key_points': []
#             }
        
#         result = response.text.strip()
        
#         # Parser la réponse
#         summary = ""
#         key_points = []
#         # Transformer texte IA → structure Python exploitable.
#         lines = result.split('\n')
#         for line in lines:
#             if line.startswith("RÉSUMÉ:"):
#                 summary = line.replace("RÉSUMÉ:", "").strip()
#             elif line.startswith("POINTS CLÉS:"):
#                 keys_text = line.replace("POINTS CLÉS:", "").strip()
#                 # Nettoyer et diviser les points
#                 key_points = [
#                     p.strip().strip('[]') 
#                     for p in keys_text.split(',') 
#                     if p.strip()
#                 ]
        
#         return {
#             'summary': summary if summary else 'Conversation résumée',
#             'key_points': key_points if key_points else ['Informations extraites de la conversation']
#         }
        
#     except Exception as e:
#         print(f"❌ ERREUR RÉSUMÉ GEMINI: {str(e)}")
#         return {
#             'summary': f'Erreur: {str(e)}',
#             'key_points': []
#         }


# def test_gemini_connection():
#     """
#     Teste la connexion à Gemini
#     Utile pour vérifier la configuration
#     """
#     try:
#         test_prompt = "Bonjour, peux-tu me dire que tu fonctionnes correctement?"
#         response = model.generate_content(test_prompt)
        
#         if response.text:
#             print("✅ Connexion Gemini OK")
#             print(f"Réponse test: {response.text[:100]}...")
#             return True
#         else:
#             print("❌ Pas de réponse de Gemini")
#             return False
            
#     except Exception as e:
#         print(f"❌ Erreur connexion Gemini: {str(e)}")
#         return False
    
# def analyze_user_question(message):
#     """
#     Analyse la question du client et extrait des filtres pour la recherche
#     Exemple: continent, type, saison, budget,location (ville)

#     """
#     prompt = f"""
#     Analyse cette demande de voyage et retourne uniquement les informations importantes en JSON:
#     - continent
#     - type de voyage
#     - saison
#     - budget (cheap, medium, expensive)
#     - location (ville)

#     Exemple de réponse JSON:
#     {{
#         "continent": "Europe",
#         "type": "Beach",
#         "location": "Paris",
#         "season": "Summer",
#         "budget": "cheap"
#     }}

#     Question utilisateur: {message}
#     """

#     response = model.generate_content(prompt)
    
#     try:
#         # json.loads() pour transformer la réponse textuelle de Gemini en un dict Python exploitable
#         # response.text contient la réponse textuelle de Gemini, qui devrait être un JSON formaté 
#         filters = json.loads(response.text)
#     except Exception:
#         filters = {}
#     return filters


# def search_destinations(filters):
#     query = Destination.query

#     if filters.get("continent"):
#         query = query.filter(Destination.continent.ilike(f"%{filters['continent']}%"))
    
#     if filters.get("type"):
#         query = query.filter(Destination.type.ilike(f"%{filters['type']}%"))
    
#     if filters.get("season"):
#         query = query.filter(Destination.bestSeason.ilike(f"%{filters['season']}%"))
    
#     if filters.get("budget"):
#         if filters["budget"] == "cheap":
#             query = query.filter(Destination.avgCostUSD < 150)
#         elif filters["budget"] == "medium":
#             query = query.filter(Destination.avgCostUSD.between(150, 300))
#         elif filters["budget"] == "expensive":
#             query = query.filter(Destination.avgCostUSD > 300)

#     return query.all()

# def build_context(destinations):
#     context = ""
#     for d in destinations:
#         context += f"""
#         Destination: {d.name}
#         Country: {d.country}
#         Continent: {d.continent}
#         Type: {d.type}
#         Best Season: {d.bestSeason}
#         Average Cost: {d.avgCostUSD}
#         Description: {d.Description}
#         """
#     return context


# # ─── FONCTIONS CONTACT ────────────────────────────────────────────────────────
# # analyse le message client et retourner un JSON avec catégorie, priorité, sentiment et résumé IA
# def classify_message(name, subject, message):
#     """Classifie un message client - VERSION LOCALE (sans Gemini pour économiser quota)"""
    
#     # Analyse simple du texte pour classifier
#     msg_lower = (subject + " " + message).lower()
    
#     # Détecte category
#     if any(word in msg_lower for word in ['urgent', 'urgent', 'probleme', 'erreur', 'bug']):
#         category = "urgent"
#         priority = "haute"
#     elif any(word in msg_lower for word in ['devis', 'prix', 'tarif', 'reduction', 'discount', 'offre']):
#         category = "demande_devis"
#         priority = "moyenne"
#     elif any(word in msg_lower for word in ['reclamation', 'plainte', 'probleme', 'insatisfait', 'mauvais']):
#         category = "reclamation"
#         priority = "haute"
#     else:
#         category = "info"
#         priority = "moyenne"
    
#     # Détecte sentiment
#     if any(word in msg_lower for word in ['merci', 'super', 'parfait', 'excellent', 'bon']):
#         sentiment = "positif"
#     elif any(word in msg_lower for word in ['probleme', 'mauvais', 'insatisfait', 'plainte', 'erreur']):
#         sentiment = "negatif"
#     else:
#         sentiment = "neutre"
    
#     # Résumé court
#     if len(message) > 50:
#         ai_summary = message[:60] + "..."
#     else:
#         ai_summary = message
    
#     result = {
#         "category": category,
#         "priority": priority,
#         "sentiment": sentiment,
#         "ai_summary": ai_summary
#     }
    
#     print(f"✅ Classification locale: {result}")
#     return result

# # rediger un mail de réponse suggérée pour l'admin en fonction de la catégorie et du sentiment
# def generate_suggested_reply(name, subject, message, category, sentiment):
#     """Génère une réponse suggérée - VERSION SIMPLE (sans Gemini)"""
    
#     # Template selon la catégorie
#     templates = {
#         'urgent': f"""Bonjour {name},

# Merci pour votre message urgent. Nous avons bien noté l'importance de votre demande et nous vous répondrons avec priorité.

# Notre équipe est en train de traiter votre requête et vous recontacterons dans les prochaines heures.

# Cordialement,
# L'équipe de votre agence de voyage""",
        
#         'reclamation': f"""Bonjour {name},

# Nous sommes très désolés d'apprendre que vous n'êtes pas satisfait de nos services. Votre retour est très important pour nous.

# Nous prenons votre réclamation au sérieux et allons investiguer immédiatement. Un membre de notre équipe vous contactera rapidement pour trouver une solution.

# Merci de votre patience et de votre confiance.

# Cordialement,
# L'équipe de votre agence de voyage""",
        
#         'demande_devis': f"""Bonjour {name},

# Merci de votre intérêt pour une réduction ou un devis personnalisé. Nous aimerions bien vous aider!

# Notre équipe vous contactera rapidement avec les meilleures offres adaptées à votre demande.

# À bientôt!

# Cordialement,
# L'équipe de votre agence de voyage""",
        
#         'info': f"""Bonjour {name},

# Merci pour votre question. Nous avons bien pris note de votre demande d'information.

# Notre équipe vous répondra rapidement avec tous les détails dont vous avez besoin.

# Cordialement,
# L'équipe de votre agence de voyage"""
#     }
    
#     reply = templates.get(category, templates['info'])
#     print(f"✅ Réponse suggérée générée pour {category}")
#     return reply


# def generate_auto_confirmation(name, subject):
#     """Génère l'email de confirmation - VERSION SIMPLE (sans Gemini)"""
    
#     return f"""Bonjour {name},

# Merci pour votre message! Nous l'avons bien reçu et nous apprécions votre intérêt pour nos services de voyages.

# Notre équipe vous répondra dans les 24 heures avec une réponse adaptée à votre demande.

# En attendant, n'hésitez pas à explorer nos destinations disponibles sur la plateforme.

# Merci de votre confiance.

# L'équipe de votre agence de voyage"""


# def generate_relance_email(name, destinations=[]):
#     """Génère un email de relance - VERSION SIMPLE (pas d'appels Gemini)"""
    
#     dest_text = ""
#     if destinations and len(destinations) > 0:
#         dest_list = "\n".join([f"- {d.name} ({d.country})" for d in destinations[:3]])
#         dest_text = f"\n\nDESTINATIONS QUI POURRAIENT VOUS INTÉRESSER:\n{dest_list}"
    
#     return f"""Bonjour {name},

# Vous nous manquez! Nous avons de magnifiques destinations à vous proposer pour votre prochain voyage.{dest_text}

# Pourquoi ne pas explorer ces opportunités et vivre une nouvelle aventure?

# À très bientôt!

# L'équipe de votre agence de voyage"""


# # import google.generativeai as genai
# # import os
# # import json
# # import hashlib
# # from models import Destination

# # # =========================
# # # CONFIG GEMINI
# # # =========================

# # GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')
# # if not GEMINI_API_KEY:
# #     raise ValueError("❌ GEMINI_API_KEY non trouvée dans .env")

# # genai.configure(api_key=GEMINI_API_KEY)

# # MODEL_NAME = "gemini-2.5-flash"
# # model = genai.GenerativeModel(MODEL_NAME)

# # # =========================
# # # CACHE SIMPLE (RAM)
# # # =========================

# # CACHE = {}

# # def make_cache_key(prefix, text):
# #     return prefix + "_" + hashlib.md5(text.encode()).hexdigest()

# # def get_cache(key):
# #     return CACHE.get(key)

# # def set_cache(key, value):
# #     CACHE[key] = value


# # # =========================
# # # SAFE GEMINI CALL (ANTI-CRASH)
# # # =========================

# # def gemini_call(prompt, cache_prefix="default", fallback="Service indisponible"):
    
# #     key = make_cache_key(cache_prefix, prompt)

# #     # ✅ 1. CACHE
# #     cached = get_cache(key)
# #     if cached:
# #         print(f"✅ Cache hit: {cache_prefix}")
# #         return cached

# #     # ✅ 2. API CALL
# #     try:
# #         print(f"🚀 Appel Gemini [{cache_prefix}]...")
# #         response = model.generate_content(
# #             prompt,
# #             generation_config=genai.types.GenerationConfig(
# #                 temperature=0.7,
# #                 max_output_tokens=500
# #             ),
# #             timeout=30
# #         )

# #         result = response.text.strip() if response.text else fallback
        
# #         print(f"✅ Gemini réponse reçue: {len(result)} caractères")

# #         # save cache
# #         set_cache(key, result)

# #         return result

# #     except Exception as e:
# #         print(f"❌ Gemini error ({cache_prefix}): {type(e).__name__}: {str(e)}")
# #         import traceback
# #         traceback.print_exc()
# #         return fallback


# # # =========================
# # # ANALYSE QUESTION
# # # =========================

# # def analyze_user_question(message):

# #     prompt = f"""
# # Retourne UNIQUEMENT JSON:

# # {{
# #  "continent": "",
# #  "type": "",
# #  "season": "",
# #  "budget": "",
# #  "location": ""
# # }}

# # Message: {message}
# # """

# #     result = gemini_call(prompt, "analysis", "{}")

# #     try:
# #         return json.loads(result)
# #     except:
# #         return {}


# # # =========================
# # # DESTINATIONS
# # # =========================

# # def search_destinations(filters):
# #     query = Destination.query

# #     if filters.get("continent"):
# #         query = query.filter(Destination.continent.ilike(f"%{filters['continent']}%"))

# #     if filters.get("type"):
# #         query = query.filter(Destination.type.ilike(f"%{filters['type']}%"))

# #     if filters.get("season"):
# #         query = query.filter(Destination.bestSeason.ilike(f"%{filters['season']}%"))

# #     if filters.get("budget"):
# #         if filters["budget"] == "cheap":
# #             query = query.filter(Destination.avgCostUSD < 150)
# #         elif filters["budget"] == "medium":
# #             query = query.filter(Destination.avgCostUSD.between(150, 300))
# #         elif filters["budget"] == "expensive":
# #             query = query.filter(Destination.avgCostUSD > 300)

# #     return query.all()


# # def build_context(destinations):
# #     return "\n".join([
# #         f"{d.name} | {d.country} | {d.type} | {d.avgCostUSD}$"
# #         for d in destinations
# #     ])


# # # =========================
# # # CHAT IA
# # # =========================

# # def get_ai_response(messages, user_context=""):

# #     user_message = messages[-1]["content"]

# #     filters = analyze_user_question(user_message)
# #     destinations = search_destinations(filters)
# #     context = build_context(destinations)

# #     conversation = "\n".join([
# #         f"{'Client' if m['role']=='user' else 'Assistant'}: {m['content']}"
# #         for m in messages
# #     ])

# #     prompt = f"""
# # Tu es un assistant voyage.

# # CONTEXTE:
# # {context}

# # CONVERSATION:
# # {conversation}

# # Réponds clairement et simplement.
# # """

# #     return gemini_call(prompt, "chat", "Désolé, service temporairement indisponible.")


# # # =========================
# # # RÉSUMÉ CONVERSATION
# # # =========================

# # def summarize_conversation(messages):

# #     conversation = "\n".join([
# #         f"{'Client' if m['role']=='user' else 'Assistant'}: {m['content']}"
# #         for m in messages
# #     ])

# #     prompt = f"""
# # Résume en 100 mots max + 3 points clés.

# # {conversation}
# # """

# #     result = gemini_call(prompt, "summary", "")

# #     return {
# #         "summary": result,
# #         "key_points": []
# #     }


# # # =========================
# # # CLASSIFICATION MESSAGE
# # # =========================

# # def classify_message(name, subject, message):

# #     prompt = f"""Tu es un agent support pour une agence de voyage. 
# # Classifie ce message client en JSON:

# # CLIENT: {name}
# # SUJET: {subject}
# # MESSAGE: {message}

# # Retourne EXACTEMENT ce JSON (rien d'autre, pas de texte avant/après):

# # {{
# #   "category": "urgent|info|reclamation|demande_devis",
# #   "priority": "haute|moyenne|basse",
# #   "sentiment": "positif|neutre|negatif",
# #   "ai_summary": "résumé en 1-2 phrases"
# # }}"""

# #     result = gemini_call(prompt, "classify", '{"category": "info", "priority": "moyenne", "sentiment": "neutre", "ai_summary": "Message client reçu"}')

# #     try:
# #         # Nettoyer les backticks markdown si présent
# #         result = result.replace('```json', '').replace('```', '').strip()
# #         return json.loads(result)
# #     except:
# #         print(f"❌ JSON parse error: {result}")
# #         return {
# #             "category": "info",
# #             "priority": "moyenne",
# #             "sentiment": "neutre",
# #             "ai_summary": "Message reçu"
# #         }


# # # =========================
# # # RÉPONSE ADMIN
# # # =========================

# # def generate_suggested_reply(name, subject, message, category, sentiment):

# #     prompt = f"""Tu es un agent d'une agence de voyage. Rédige une réponse EMAIL pour ce client:

# # CLIENT: {name}
# # SUJET: {subject}
# # MESSAGE: {message}
# # CATÉGORIE: {category}
# # SENTIMENT: {sentiment}

# # RÈGLES:
# # - Commence par "Bonjour {name},"
# # - 100-150 mots maximum
# # - Réponse DIRECTE au message
# # - Tone adapté: urgent=rapide, reclamation=empathique, info=court, devis=enthousiaste
# # - Termine par "Cordialement" ou "Bien à vous"
# # - PAS de markdown, texte simple
# # - PAS d'emojis
# # - Phrases claires et courtes"""

# #     return gemini_call(
# #         prompt,
# #         "reply",
# #         f"Bonjour {name},\n\nMerci pour votre message. Notre équipe travaille à y répondre de manière personnalisée.\n\nCordialement,\nL'équipe de votre agence de voyage"
# #     )


# # # =========================
# # # CONFIRMATION EMAIL
# # # =========================

# # def generate_auto_confirmation(name, subject):

# #     prompt = f"""Tu es un agent d'une agence de voyage. 
# # Rédige un COURT email de confirmation de réception pour ce client:

# # Client: {name}
# # Sujet du message: {subject}

# # RÈGLES:
# # - Commence par "Bonjour {name},"
# # - 80-100 mots maximum
# # - Remercie pour le message
# # - Confirme qu'on l'a bien reçu
# # - Indique un délai de réponse (24-48h)
# # - Ton professionnel et chaleureux
# # - Termine par "L'équipe de votre agence de voyage"
# # - PAS de markdown, PAS d'emojis"""

# #     return gemini_call(
# #         prompt,
# #         "confirm",
# #         f"Bonjour {name},\n\nMerci pour votre message! Nous l'avons bien reçu et notre équipe vous répondra dans les 24 heures.\n\nCordialement,\nL'équipe de votre agence de voyage"
# #     )


# # # =========================
# # # RELANCE EMAIL (OPTIMISÉ)
# # # =========================

# # def generate_relance_email(name, destinations=None):

# #     dest_list = ""
# #     if destinations:
# #         dest_list = "\n".join([f"- {d.name} ({d.country})" for d in destinations[:3]])
# #         dest_section = f"\n\nDESTINATIONS SUGGÉRÉES:\n{dest_list}"
# #     else:
# #         dest_section = ""

# #     prompt = f"""Tu es un agent d'agence de voyage. Rédige un EMAIL DE RELANCE court:

# # CLIENT: {name}
# # {dest_section}

# # RÈGLES:
# # - Commence par "Bonjour {name},"
# # - 60-80 mots MAXIMUM
# # - Aéré avec sauts de ligne
# # - Relance sympathique et douce
# # - Si destinations: liste-les simplement (pas de formatage)
# # - Termine par "L'équipe de votre agence de voyage"
# # - PAS de markdown, PAS d'emojis
# # - Ton: amical et incitatif"""

# #     return gemini_call(
# #         prompt,
# #         "relance",
# #         f"Bonjour {name},\n\nVous nous manquez! Découvrez nos nouvelles offres exclusives.\n\nL'équipe de votre agence de voyage"
# #     )


# # # =========================
# # # TEST
# # # =========================

# # def test_gemini_connection():
# #     try:
# #         res = model.generate_content("Bonjour")
# #         print("✅ Gemini OK")
# #         return True
# #     except Exception as e:
# #         print("❌ Erreur:", e)
# #         return False
    
# # if __name__ == "__main__":
# #     print("🔍 Test connexion Gemini...")
# #     test_gemini_connection()

# #     print("\n🔍 Test appel simple...")
# #     print(gemini_call("Dis bonjour en une phrase."))

# #     print("\n🔍 Test cache (doit être rapide)...")
# #     print(gemini_call("Dis bonjour en une phrase."))

import google.generativeai as genai
import os
import json
import hashlib
import time
from models import Destination

# ============================================================
# CONFIGURATION GEMINI
# ============================================================

GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')
if not GEMINI_API_KEY:
    raise ValueError("❌ GEMINI_API_KEY non trouvée dans .env")

genai.configure(api_key=GEMINI_API_KEY)

GEMINI_MODEL = 'gemini-2.5-flash'
try:
    model = genai.GenerativeModel(GEMINI_MODEL)
except Exception as e:
    raise RuntimeError(f"Failed to initialize Gemini model '{GEMINI_MODEL}': {e}") from e


# ============================================================
# CACHE EN MÉMOIRE
# ============================================================

_CACHE = {}

def _cache_key(prefix: str, text: str) -> str:
    return f"{prefix}_{hashlib.md5(text.encode()).hexdigest()}"

def _cache_get(key: str):
    return _CACHE.get(key)

def _cache_set(key: str, value):
    _CACHE[key] = value


# ============================================================
# APPEL GEMINI SÉCURISÉ
# ============================================================

def _gemini_call(prompt: str, cache_prefix: str = "default", fallback: str = "", max_retries: int = 2, delay: float = 0.3) -> str:
    key = _cache_key(cache_prefix, prompt)

    cached = _cache_get(key)
    if cached is not None:
        print(f"✅ Cache hit: [{cache_prefix}]")
        return cached

    for attempt in range(max_retries + 1):
        try:
            print(f"🚀 Appel Gemini [{cache_prefix}] - tentative {attempt + 1}...")
            response = model.generate_content(
                prompt,
                generation_config=genai.types.GenerationConfig(
                    temperature=0.7,
                    # max_output_tokens=600,
                )
            )
            result = response.text.strip() if response.text else fallback
            _cache_set(key, result)
            print(f"✅ Gemini OK [{cache_prefix}] — {len(result)} caractères")
            return result

        except Exception as e:
            error_str = str(e)
            if "429" in error_str or "quota" in error_str.lower() or "rate" in error_str.lower():
                if attempt < max_retries:
                    wait_time = (attempt + 1) * 10
                    print(f"⚠️  Quota Gemini (429) — attente {wait_time}s avant retry...")
                    time.sleep(wait_time)
                    continue
                else:
                    print(f"❌ Quota épuisé après {max_retries} retries.")
                    return fallback
            else:
                print(f"❌ Erreur Gemini [{cache_prefix}]: {e}")
                return fallback

    return fallback


# ============================================================
# DICTIONNAIRE INTELLIGENT : Termes → Pays/Continent
# ============================================================

# Associe des mots-clés (typos, spécialités, monuments) à des pays/continents
ASSOCIATED_TERMS = {
    # ITALIE
    'pizza': 'Italia',
    'piza': 'Italia',
    'pâtes': 'Italia',
    'pasta': 'Italia',
    'rome': 'Italia',
    'roma': 'Italia',
    'venise': 'Italia',
    'venezia': 'Italia',
    'florence': 'Italia',
    'firenze': 'Italia',
    'toscane': 'Italia',
    'amalfi': 'Italia',
    'colosseum': 'Italia',
    'colisee': 'Italia',
    
    # FRANCE
    'paris': 'France',
    'eiffel': 'France',
    'tour eiffel': 'France',
    'lavande': 'France',
    'provence': 'France',
    'lyon': 'France',
    'marseille': 'France',
    'riviera': 'France',
    'bretagne': 'France',
    'chateau': 'France',
    'bordeaux': 'France',
    'alsace': 'France',
    
    # ESPAGNE
    'madrid': 'Spain',
    'espagne': 'Spain',
    'spain': 'Spain',
    'barcelona': 'Spain',
    'barcelone': 'Spain',
    'sagrada familia': 'Spain',
    'flamenco': 'Spain',
    'paella': 'Spain',
    'madrid': 'Spain',
    'seville': 'Spain',
    'seville': 'Spain',
    'ibiza': 'Spain',
    'malaga': 'Spain',
    
    # GRÈCE
    'grece': 'Greece',
    'greece': 'Greece',
    'athenes': 'Greece',
    'athens': 'Greece',
    'santorini': 'Greece',
    'mykonos': 'Greece',
    'crete': 'Greece',
    'acropole': 'Greece',
    'parthenon': 'Greece',
    'cyclades': 'Greece',
    'iles grecques': 'Greece',
    
    # PORTUGAL
    'lisbonne': 'Portugal',
    'lisbon': 'Portugal',
    'porto': 'Portugal',
    'portugal': 'Portugal',
    'douro': 'Portugal',
    'algarve': 'Portugal',
    'matcha': 'Portugal',
    'azulejos': 'Portugal',
    
    # ALLEMAGNE
    'allemagne': 'Germany',
    'germany': 'Germany',
    'berlin': 'Germany',
    'munich': 'Germany',
    'munchen': 'Germany',
    'baviere': 'Germany',
    'bavaria': 'Germany',
    'neuschwanstein': 'Germany',
    'rhin': 'Germany',
    'nuremberg': 'Germany',
    
    # SUISSE
    'suisse': 'Switzerland',
    'switzerland': 'Switzerland',
    'zurich': 'Switzerland',
    'lucerne': 'Switzerland',
    'montagne': 'Switzerland',
    'alpes': 'Switzerland',
    'glacier': 'Switzerland',
    'interlaken': 'Switzerland',
    'jungfrau': 'Switzerland',
    
    # UK
    'Londres': 'England',
    'london': 'England',
    'big ben': 'England',
    'royaume uni': 'England',
    'uk': 'England',
    'angleterre': 'England',
    'ecosse': 'Scotland',
    'scotland': 'Scotland',
    'edimbourg': 'Scotland',
    'edinburgh': 'Scotland',
    
    # TURQUIE
    'turquie': 'Turkey',
    'turkey': 'Turkey',
    'istanbul': 'Turkey',
    'cappadoce': 'Turkey',
    'cappadocia': 'Turkey',
    'ephesus': 'Turkey',
    'bodrum': 'Turkey',
    'gallipoli': 'Turkey',
    
    # ÉGYPTE
    'egypte': 'Egypt',
    'egypt': 'Egypt',
    'cairo': 'Egypt',
    'le caire': 'Egypt',
    'pyramide': 'Egypt',
    'giza': 'Egypt',
    'sphinx': 'Egypt',
    'nile': 'Egypt',
    'nil': 'Egypt',
    'croisiere': 'Egypt',
    
    # MAROC
    'maroc': 'Morocco',
    'morocco': 'Morocco',
    'marrakech': 'Morocco',
    'fes': 'Morocco',
    'agadir': 'Morocco',
    'casbah': 'Morocco',
    'medina': 'Morocco',
    'sahara': 'Morocco',
    'casa': 'Morocco',
    
    # ASIE
    'asie': 'Asia',
    'asia': 'Asia',
    'inde': 'India',
    'india': 'India',
    'taj mahal': 'India',
    'thailand': 'Thailand',
    'thaïlande': 'Thailand',
    'bangkok': 'Thailand',
    'bali': 'Indonesia',
    'indonesie': 'Indonesia',
    'java': 'Indonesia',
    'vietnam': 'Vietnam',
    'ho chi minh': 'Vietnam',
    'hanoi': 'Vietnam',
    'cambodge': 'Cambodia',
    'angkor': 'Cambodia',
    'japon': 'Japan',
    'japan': 'Japan',
    'tokyo': 'Japan',
    'kyoto': 'Japan',
    'fuji': 'Japan',
    'chine': 'China',
    'china': 'China',
    'grande muraille': 'China',
    'beijing': 'China',
    'shanghai': 'China',
    'hong kong': 'Hong Kong',
    
    # AMÉRIQUE
    'usa': 'United States',
    'etats unis': 'United States',
    'united states': 'United States',
    'new york': 'United States',
    'los angeles': 'United States',
    'vegas': 'United States',
    'florida': 'United States',
    'hawaii': 'United States',
    'canada': 'Canada',
    'vancouver': 'Canada',
    'toronto': 'Canada',
    'mexique': 'Mexico',
    'mexico': 'Mexico',
    'cancun': 'Mexico',
    'mexico city': 'Mexico',
    'punta cana': 'Dominican Republic',
    'republique dominicaine': 'Dominican Republic',
    'costa rica': 'Costa Rica',
    'perou': 'Peru',
    'peru': 'Peru',
    'machu picchu': 'Peru',
    'lima': 'Peru',
    'argentina': 'Argentina',
    'argentine': 'Argentina',
    'buenos aires': 'Argentina',
    'bresil': 'Brazil',
    'brazil': 'Brazil',
    'rio': 'Brazil',
    'amazon': 'Brazil',
    
    # AFRIQUE
    'afrique': 'Africa',
    'kenya': 'Kenya',
    'safari': 'Kenya',
    'serengeti': 'Tanzania',
    'tanzanie': 'Tanzania',
    'tanzanya': 'Tanzania',
    'zanzibar': 'Tanzania',
    'afrique du sud': 'South Africa',
    'south africa': 'South Africa',
    'cap': 'South Africa',
    'table mountain': 'South Africa',
    'senegal': 'Senegal',
    'dakar': 'Senegal',
    
    # OCEANIE
    'australie': 'Australia',
    'australia': 'Australia',
    'sydney': 'Australia',
    'melbourne': 'Australia',
    'grande barriere': 'Australia',
    'great barrier': 'Australia',
    'nouvelle zelande': 'New Zealand',
    'new zealand': 'New Zealand',
    'auckland': 'New Zealand',
    'fiji': 'Fiji',
    'fidji': 'Fiji',
}

def _resolve_location_alias(location_str: str) -> str:
    """
    Convertit un terme écrit librement (ex: "pizza", "taj mahal") en pays/continent
    Retourne le pays normalisé ou la chaîne originale si pas de match
    """
    if not location_str:
        return ""
    
    location_lower = location_str.lower().strip()
    
    # Chercher un match direct dans le dictionnaire
    if location_lower in ASSOCIATED_TERMS:
        return ASSOCIATED_TERMS[location_lower]
    
    # Chercher un match partiel (pour les phrases comme "taj mahal" ou "grande barriere")
    for term, country in ASSOCIATED_TERMS.items():
        if term in location_lower or location_lower in term:
            return country
    
    # Si pas de match, retourner l'original
    return location_str


# ============================================================
# FALLBACKS LOCAUX INTELLIGENTS (sans Gemini)
# ============================================================

def _detect_category_local(message: str) -> str:
    """
    Détecte la catégorie avec des règles précises.
    Ordre : urgent > reclamation > demande_devis > info
    """
    msg_lower = message.lower()

    # Réclamation : paiement sans confirmation, attente longue, injuste
    reclamation_keywords = [
        'pas normale', 'pas normal', 'pas de confirmation', 'toujours pas',
        'depuis des jours', 'depuis plusieurs', 'aucune confirmation',
        'problème', 'probleme', 'plainte', 'insatisfait', 'mauvais',
        'erreur', 'inacceptable', 'scandaleux', 'déçu', 'decu',
        'paiement', 'payé', 'paye', 'débité', 'debite'
    ]
    urgent_keywords = [
        'urgent', 'urgence', 'rapidement', 'vite', 'asap',
        'immédiatement', 'immediatement', 'au plus tôt', 'au plus tot',
        'top', 'dès que possible', 'des que possible'
    ]
    devis_keywords = [
        'devis', 'prix', 'tarif', 'réduction', 'reduction', 'offre', 'coût', 'cout'
    ]

    # Réclamation détectée en premier (paiement sans retour = grave)
    if any(w in msg_lower for w in reclamation_keywords):
        # Si en plus c'est urgent, on garde réclamation (plus grave qu'urgent seul)
        return 'reclamation'

    if any(w in msg_lower for w in urgent_keywords):
        return 'urgent'

    if any(w in msg_lower for w in devis_keywords):
        return 'demande_devis'

    return 'info'


def _detect_priority_local(message: str, category: str) -> str:
    """Déduit la priorité depuis la catégorie et le contenu."""
    msg_lower = message.lower()

    if category == 'urgent':
        return 'haute'
    if category == 'reclamation':
        # Réclamation avec paiement = priorité haute
        if any(w in msg_lower for w in ['paiement', 'payé', 'paye', 'débité', 'argent']):
            return 'haute'
        return 'haute'
    if category == 'demande_devis':
        return 'moyenne'
    return 'basse'


def _detect_sentiment_local(message: str) -> str:
    msg_lower = message.lower()
    if any(w in msg_lower for w in [
        'pas normale', 'pas normal', 'inacceptable', 'déçu', 'decu',
        'scandaleux', 'insatisfait', 'mauvais', 'problème', 'probleme'
    ]):
        return 'negatif'
    if any(w in msg_lower for w in ['merci', 'super', 'parfait', 'excellent', 'bien', 'content']):
        return 'positif'
    return 'neutre'


def _generate_local_summary(name: str, subject: str, message: str) -> str:
    """
    Génère un résumé lisible sans Gemini.
    Analyse l'intention réelle du message.
    """
    import re
    msg_lower = message.lower()

    # Détecter l'intention principale (ordre : du plus spécifique au plus général)
    if any(w in msg_lower for w in ['paiement', 'payé', 'paye', 'débité']) and \
       any(w in msg_lower for w in ['confirmation', 'confirmer', 'confirm']):
        action = "signale un paiement effectué sans confirmation de réservation reçue"

    elif any(w in msg_lower for w in ['depuis des jours', 'depuis plusieurs', 'toujours pas', 'aucune']):
        action = "relance après une longue attente sans réponse"

    elif any(w in msg_lower for w in ['confirmation', 'confirmer', 'confirm']):
        action = "demande une confirmation de réservation"

    elif any(w in msg_lower for w in ['annuler', 'annulation', 'cancel']):
        action = "souhaite annuler sa réservation"

    elif any(w in msg_lower for w in ['modifier', 'changer', 'change', 'déplacer']):
        action = "souhaite modifier sa réservation"

    elif any(w in msg_lower for w in ['rembours', 'remboursement', 'refund']):
        action = "demande un remboursement"

    elif any(w in msg_lower for w in ['problème', 'probleme', 'erreur', 'bug', 'souci']):
        action = "signale un problème"

    elif any(w in msg_lower for w in ['prix', 'tarif', 'coût', 'cout', 'devis']):
        action = "demande des informations tarifaires"

    elif any(w in msg_lower for w in ['urgent', 'rapidement', 'vite', 'top']):
        action = "formule une demande urgente"

    else:
        action = "envoie une demande"

    # Extraire le contexte (numéro de réservation ou destination)
    context = ""
    match = re.search(r'#(\d+)', subject)
    if match:
        context = f" pour la réservation #{match.group(1)}"
    elif subject and subject not in ("Question générale", ""):
        # Extraire juste le nom de destination si présent dans le sujet
        dest_match = re.search(r'—\s*(.+?)(?:\s*\d{4}|$)', subject)
        if dest_match:
            context = f" ({dest_match.group(1).strip()})"

    return f"Le client {action}{context}."


def _default_reply(name: str, category: str) -> str:
    templates = {
        'urgent': f"""Bonjour {name},

Nous avons bien reçu votre message urgent et le traitons en priorité absolue.

Un membre de notre équipe vous contactera dans les prochaines heures pour vous apporter une réponse rapide.

Merci de votre patience.

Cordialement,
L'équipe de votre agence de voyage""",

        'reclamation': f"""Bonjour {name},

Nous sommes sincèrement désolés pour la situation que vous rencontrez et comprenons votre frustration.

Votre réclamation est prise très au sérieux. Notre équipe va examiner votre dossier immédiatement et vous recontactera sous 24h avec une solution concrète.

Merci de votre patience et de votre confiance.

Cordialement,
L'équipe de votre agence de voyage""",

        'demande_devis': f"""Bonjour {name},

Merci pour votre intérêt ! Nous serions ravis de vous préparer un devis personnalisé.

Notre équipe vous contactera très prochainement avec les meilleures offres adaptées à votre projet.

Cordialement,
L'équipe de votre agence de voyage""",

        'info': f"""Bonjour {name},

Merci pour votre message. Nous avons bien noté votre demande.

Notre équipe vous répondra dans les 24 heures avec tous les détails nécessaires.

Cordialement,
L'équipe de votre agence de voyage"""
    }
    return templates.get(category, templates['info'])


def _default_confirmation(name: str) -> str:
    return f"""Bonjour {name},

Nous avons bien reçu votre message et nous vous en remercions.

Notre équipe vous répondra dans les 24 à 48 heures avec une réponse personnalisée.

En attendant, n'hésitez pas à explorer nos destinations sur la plateforme.

L'équipe de votre agence de voyage"""


# ============================================================
# TRAITEMENT EMAIL — 1 SEUL APPEL GEMINI
# ============================================================

def process_email_with_ai(name: str, subject: str, message: str,
                           segment: str = None, churn_risk: str = None) -> dict:
    """
    1 seul appel Gemini pour : classifier + résumer + générer les emails.
    Si Gemini est indisponible (429), fallback local complet et intelligent.
    """

    # Préparer le fallback en avance (utilisé si Gemini échoue)
    local_category    = _detect_category_local(message)
    local_priority    = _detect_priority_local(message, local_category)
    local_sentiment   = _detect_sentiment_local(message)
    local_summary     = _generate_local_summary(name, subject, message)
    local_reply       = _default_reply(name, local_category)
    local_confirm     = _default_confirmation(name)

    fallback_json = {
        "category":          local_category,
        "priority":          local_priority,
        "sentiment":         local_sentiment,
        "ai_summary":        local_summary,
        "suggested_reply":   local_reply,
        "auto_confirmation": local_confirm
    }

    # Contexte segment pour personnaliser
    segment_context = ""
    if segment:
        segment_context = f"Segment client : {segment}"
        if segment == "VIP":
            segment_context += " — Client très important, traitement prioritaire et ton très professionnel"
        elif segment == "Régulier":
            segment_context += " — Client fidèle, ton chaleureux et attentionné"
        elif segment == "Nouveau":
            segment_context += " — Nouveau client, ton accueillant et rassurant"
        elif segment == "Inactif":
            segment_context += " — Client inactif, profiter pour le fidéliser"

    if churn_risk and churn_risk in ["Critique", "Élevé"]:
        segment_context += f"\n⚠️ Risque de départ : {churn_risk} — réponse encore plus soignée"

#     prompt = f"""Tu es un agent support professionnel d'une agence de voyages.
#     Analyse ce message client et génère une réponse PERSONNALISÉE et CONCISE.

# CLIENT : {name}
# SUJET : {subject}
# MESSAGE : {message}
# {segment_context}

# RÈGLES IMPORTANTES :
# - suggested_reply : 80-100 mots MAX, direct et utile, commence par "Bonjour {name},"
# - Ne pas répéter le message du client dans la réponse
# - Adapter le ton selon la catégorie :
#   * urgent → très réactif, propose un rappel téléphonique
#   * reclamation → empathique, s'excuser, proposer une solution concrète
#   * demande_devis → enthousiaste, mentionner qu'on peut négocier
#   * info → informatif et rassurant
# - auto_confirmation : 50-60 mots MAX, simple et rassurant pour le client
# - ai_summary : 1 phrase, reformuler l'intention (pas copier le message)


# Retourne UNIQUEMENT ce JSON valide, sans texte avant ni après, sans backticks markdown :

# {{
#     "category": "urgent|info|reclamation|demande_devis",
#     "priority": "haute|moyenne|basse",
#     "sentiment": "positif|neutre|negatif",
#     "ai_summary": "Reformule l'intention du client en 1 phrase professionnelle. INTERDIT de copier le message original. Exemple : Le client signale un paiement effectué sans confirmation de réservation reçue pour la réservation #17.",
#     "suggested_reply": "Email de réponse pour l'admin, 80-100 mots, commence par Bonjour {name}, professionnel et empathique selon la catégorie, termine par Cordialement suivi du nom de l'agence",
#     "auto_confirmation": "Email de confirmation pour le client, 60-80 mots, professionnel et rassurant, termine par L'équipe de votre agence de voyage"
# }}"""

    prompt = f""" Tu es un agent de support client EXPERT dans une agence de voyage haut de gamme.

Ton objectif est de répondre comme un humain professionnel, pas comme une IA.

CLIENT : {name}
SUJET : {subject}
MESSAGE : {message}
{segment_context}

🎯 OBJECTIFS :
- Comprendre l’intention réelle du client
- Rassurer et apporter une réponse claire
- Donner une impression premium (service client de qualité)

📌 RÈGLES :

1. suggested_reply :
- 80 à 120 mots
- Ton humain, naturel, professionnel (éviter style robot)
- Commencer par : Bonjour {name},
- Adapter selon situation :
    • urgent → proposer action immédiate (appel, traitement rapide)
    • reclamation → s’excuser + solution concrète
    • demande → être utile et engageant
- Ajouter UNE phrase personnalisée (important ⭐)
- Terminer par :
  Cordialement,
  L’équipe de votre agence de voyage

2. auto_confirmation :
- 60 à 80 mots
- Simple, rassurant
- Confirmer réception + délai de réponse
- Ton chaleureux

3. ai_summary :
- 1 phrase claire
- Reformuler l’intention
- Interdiction de copier le message

4. Classification :
- category : urgent | info | reclamation | demande_devis
- priority : haute | moyenne | basse
- sentiment : positif | neutre | negatif

⚠️ IMPORTANT :
- Réponses naturelles (pas IA)
- Pas de répétition du message
- Français professionnel

Retourne UNIQUEMENT ce JSON valide :

{{
    "category": "",
    "priority": "",
    "sentiment": "",
    "ai_summary": "",
    "suggested_reply": "",
    "auto_confirmation": ""
}}
"""
    result = _gemini_call(
        prompt,
        f"email_{hashlib.md5((name + message).encode()).hexdigest()[:8]}",
        "",
        # max_retries=1
        2,    # max_retries
        0.3   # delay between retries
    )

    if not result:
        print(f"⚠️  Gemini indisponible — fallback local: cat={local_category} | résumé: {local_summary}")
        return fallback_json

    try:
        clean  = result.replace("```json", "").replace("```", "").strip()
        parsed = json.loads(clean)

        # Vérification anti-copie du résumé
        ai_summary = parsed.get("ai_summary", "").strip()
        if not ai_summary or ai_summary == message.strip():
            print("⚠️  Résumé IA = copie du message → fallback local")
            parsed["ai_summary"] = local_summary

        # Compléter les champs manquants ou vides
        for key, val in fallback_json.items():
            if key not in parsed or not parsed[key]:
                parsed[key] = val

        print(f"✅ Email traité par Gemini — cat: {parsed.get('category')} | "
              f"résumé: {parsed.get('ai_summary')[:70]}...")
        return parsed

    except Exception as e:
        print(f"⚠️  Erreur parsing JSON Gemini: {e} → fallback utilisé")
        return fallback_json


# ============================================================
# CHAT IA (RAG)
# ============================================================

# def analyze_user_question(message: str) -> dict:
#     """
#     Analyse la demande de voyage et extrait les filtres intelligemment.
#     Essaie d'abord localement avec les termes associés, puis demande à Gemini.
#     """
#     msg_lower = message.lower()
    
#     # ✅ ÉTAPE 1 : Détection locale des termes associés (rapide + fiable)
#     detected_location = ""
#     for term, country in ASSOCIATED_TERMS.items():
#         if term in msg_lower:
#             detected_location = country
#             print(f"✅ Localisation détectée (local) : {term} → {country}")
#             break
    
#     # ✅ ÉTAPE 2 : Appel Gemini pour une analyse complète
#     prompt = f"""Analyse cette demande de voyage. Retourne UNIQUEMENT du JSON valide.

# {{
#     "continent": "",
#     "type": "",
#     "location": "",
#     "season": "",
#     "budget": ""
# }}

# Budget : "cheap" (< 150$), "medium" (150-300$), "expensive" (> 300$). Laisse vide si non mentionné.

# Question : {message}"""

#     result = _gemini_call(prompt, "analysis", "{}")
#     try:
#         clean = result.replace("```json", "").replace("```", "").strip()
#         filters = json.loads(clean)
        
#         # ✅ ÉTAPE 3 : Enrichir les filtres avec la détection locale
#         if detected_location and not filters.get("location"):
#             filters["location"] = detected_location
#             print(f"✅ Localisation enrichie : {detected_location}")
        
#         return filters
#     except Exception as e:
#         print(f"⚠️  Erreur parse JSON: {e}")
#         # Fallback : retourner la localisation détectée au moins
#         return {"location": detected_location} if detected_location else {}

def analyze_user_question(message: str) -> dict:
    """
    Gemini analyse la question en priorité.
    Fallback local si Gemini indisponible.
    """

    # ── Essai avec Gemini ──────────────────────────────────────
    prompt = f"""Analyse cette demande de voyage et extrais les filtres en JSON.

Message : {message}

Retourne UNIQUEMENT ce JSON, sans texte avant ni après :
{{
    "continent": "europe|asie|afrique|amerique|oceanie|null",
    "country":   "nom du pays en anglais ou null",
    "city":      "nom de la ville ou null",
    "type":      "Beach|Mountain|City|Adventure|Relaxation|Cultural|null",
    "season":    "Summer|Winter|Spring|Autumn|null",
    "budget":    "cheap|medium|expensive|null"
}}

Exemples :
- "je veux la mer cet été" → type=Beach, season=Summer
- "voyage pas cher en Asie" → continent=asie, budget=cheap  
- "week-end culturel à Rome" → city=Rome, country=Italy, type=Cultural
- "montagne pour skier" → type=Mountain, season=Winter"""

    result = _gemini_call(
        prompt,
        f"filters_{hashlib.md5(message.encode()).hexdigest()[:8]}",
        "",
        max_retries=1
    )

    if result:
        try:
            clean   = result.replace("```json", "").replace("```", "").strip()
            filters = json.loads(clean)

            # Nettoyer les valeurs "null" string
            filters = {k: v for k, v in filters.items()
                      if v and v != "null" and v != "None"}

            # Mapper city/country vers continent si manquant
            if filters.get("city") and not filters.get("continent"):
                filters = _enrich_with_local_geo(filters)

            print(f"✅ Filtres extraits par Gemini: {filters}")
            return filters

        except Exception as e:
            print(f"⚠️ Parsing filtres Gemini échoué: {e} → fallback local")

    # ── Fallback local si Gemini échoue ───────────────────────
    print("⚠️ Fallback local pour les filtres")
    return _extract_filters_local(message)


def _enrich_with_local_geo(filters: dict) -> dict:
    """
    Enrichit les filtres avec le continent si on a une ville/pays.
    Utilisé après parsing Gemini pour compléter les données manquantes.
    """
    country_to_continent = {
        'france': 'europe', 'italy': 'europe', 'spain': 'europe',
        'germany': 'europe', 'portugal': 'europe', 'greece': 'europe',
        'morocco': 'afrique', 'tunisia': 'afrique', 'egypt': 'afrique',
        'japan': 'asie', 'thailand': 'asie', 'indonesia': 'asie',
        'uae': 'asie', 'india': 'asie', 'china': 'asie',
        'usa': 'amerique', 'canada': 'amerique', 'mexico': 'amerique',
        'brazil': 'amerique', 'australia': 'oceanie',
    }
    country = filters.get("country", "").lower()
    if country in country_to_continent:
        filters["continent"] = country_to_continent[country]
    return filters


def _normalize(text: str) -> str:
    """
    Normalise le texte : minuscules, accents supprimés, etc.
    """
    import unicodedata
    text = text.lower()
    text = ''.join(
        c for c in unicodedata.normalize('NFD', text)
        if unicodedata.category(c) != 'Mn'
    )
    return text


def _extract_filters_local(message: str) -> dict:
    """
    Fallback local minimal — utilisé uniquement si Gemini est down.
    Garde les mêmes listes que ton code actuel.
    """
    msg_lower = message.lower()
    msg_norm  = _normalize(message)
    filters   = {}

    # Continents directs
    if any(w in msg_lower for w in ['europe', 'france', 'italie', 'espagne']):
        filters['continent'] = 'europe'
    elif any(w in msg_lower for w in ['asie', 'japon', 'thailande', 'bali']):
        filters['continent'] = 'asie'
    elif any(w in msg_lower for w in ['afrique', 'maroc', 'tunisie', 'egypte']):
        filters['continent'] = 'afrique'
    elif any(w in msg_lower for w in ['amerique', 'usa', 'canada', 'mexique']):
        filters['continent'] = 'amerique'

    # Types
    if any(w in msg_norm for w in ['plage', 'mer', 'beach', 'sable']):
        filters['type'] = 'Beach'
    elif any(w in msg_norm for w in ['montagne', 'ski', 'randonnee']):
        filters['type'] = 'Mountain'
    elif any(w in msg_norm for w in ['ville', 'city', 'musee', 'shopping']):
        filters['type'] = 'City'

    # Budget
    if any(w in msg_norm for w in ['pas cher', 'economique', 'budget']):
        filters['budget'] = 'cheap'
    elif any(w in msg_norm for w in ['luxe', 'luxury', 'premium']):
        filters['budget'] = 'expensive'

    # Saison
    if any(w in msg_norm for w in ['ete', 'summer', 'juillet', 'aout']):
        filters['season'] = 'Summer'
    elif any(w in msg_norm for w in ['hiver', 'winter', 'ski', 'neige']):
        filters['season'] = 'Winter'

    print(f"⚠️ Filtres fallback local: {filters}")
    return filters


def search_destinations(filters: dict):
    """
    Recherche intelligente avec fallback :
    1. Cherche par location exact, continent, type, saison, budget
    2. Si aucun résultat : cherche juste par pays/continent
    3. Si toujours rien : retourne toutes les destinations populaires
    """
    query = Destination.query
    
    # ✅ ÉTAPE 1 : Résoudre les alias (ex: "pizza" → "Italia")
    if filters.get("location"):
        resolved = _resolve_location_alias(filters["location"])
        filters["location"] = resolved
        print(f"🔄 Alias résolu : {filters.get('location')} → {resolved}")
    
    # ✅ ÉTAPE 2 : Appliquer les filtres principaux
    original_query = query
    
    if filters.get("location"):
        # Chercher par ville ou pays
        query = query.filter(
            (Destination.name.ilike(f"%{filters['location']}%")) |
            (Destination.country.ilike(f"%{filters['location']}%")) |
            (Destination.continent.ilike(f"%{filters['location']}%"))
        )
    
    if filters.get("continent"):
        query = query.filter(Destination.continent.ilike(f"%{filters['continent']}%"))
    
    if filters.get("type"):
        query = query.filter(Destination.type.ilike(f"%{filters['type']}%"))
    
    if filters.get("season"):
        query = query.filter(Destination.bestSeason.ilike(f"%{filters['season']}%"))
    
    if filters.get("budget"):
        if filters["budget"] == "cheap":
            query = query.filter(Destination.avgCostUSD < 150)
        elif filters["budget"] == "medium":
            query = query.filter(Destination.avgCostUSD.between(150, 300))
        elif filters["budget"] == "expensive":
            query = query.filter(Destination.avgCostUSD > 300)
    
    results = query.all()
    
    # ✅ ÉTAPE 3 : FALLBACK #1 - Si aucun résultat, chercher juste par pays/continent
    if not results and filters.get("location"):
        print(f"⚠️  Aucune destination trouvée pour '{filters['location']}' — recherche par pays/continent...")
        fallback_query = original_query.filter(
            (Destination.country.ilike(f"%{filters['location']}%")) |
            (Destination.continent.ilike(f"%{filters['location']}%"))
        )
        results = fallback_query.all()
        
        if results:
            print(f"✅ {len(results)} destination(s) trouvée(s) par fallback pays/continent")
    
    # ✅ ÉTAPE 4 : FALLBACK #2 - Si toujours rien, retourner les plus populaires
    if not results:
        print(f"⚠️  Aucune destination trouvée — retour des destinations populaires...")
        results = original_query.limit(5).all()
        if results:
            print(f"✅ Affichage de {len(results)} destinations populaires")
    
    return results


def build_context(destinations) -> str:
    if not destinations:
        return "Aucune destination spécifique trouvée dans la base de données."
    return "\n".join([
        f"- {d.name} ({d.country}, {d.continent}) | {d.type} | {d.bestSeason} | {d.avgCostUSD}$ | {d.Description}"
        for d in destinations
    ])


def get_ai_response(messages: list, user_context: str = "") -> str:
    user_message = messages[-1]["content"]

    # Cache unique par message (évite les collisions entre utilisateurs)
    chat_cache_key = f"chat_{hashlib.md5(user_message.encode()).hexdigest()[:12]}"

    # Fallback intelligent basé sur la question (sans Gemini)
    fallback = _generate_chat_fallback(user_message)

    filters      = analyze_user_question(user_message)
    destinations = search_destinations(filters)
    context      = build_context(destinations)

    if user_context:
        context = f"Profil client : {user_context}\n\n{context}"

    conversation = "\n".join([
        f"{'Client' if m['role'] == 'user' else 'Assistant'}: {m['content']}"
        for m in messages
    ])

    prompt = f"""Tu es un assistant de support client pour une agence de voyages. Sois chaleureux, clair et utile.

RÈGLES :
- Listes à puces (*)
- Noms et prix en GRAS (**texte**)
- Emojis pertinents (🌴 ✈️ 💰 📍)
- Format destination : 📍 **Nom** : **Prix $** — description courte
- Une ligne vide entre chaque destination

DESTINATIONS DISPONIBLES :
{context}

CONVERSATION :
{conversation}

Réponds de façon concise et utile."""

    return _gemini_call(prompt, chat_cache_key, fallback)


def _generate_chat_fallback(user_message: str) -> str:
    """
    Réponse de secours intelligente quand Gemini est indisponible.
    Basée sur les mots-clés du message pour ne jamais laisser le chat vide.
    """
    msg_lower = user_message.lower()

    if any(w in msg_lower for w in ['bonjour', 'salut', 'hello', 'bonsoir']):
        return ("Bonjour ! 👋 Je suis votre assistant voyage. "
                "Je rencontre une légère difficulté technique en ce moment, "
                "mais je suis là pour vous aider. "
                "Posez-moi votre question et je ferai de mon mieux ! ✈️")

    if any(w in msg_lower for w in ['destination', 'voyage', 'partir', 'visiter', 'où aller']):
        return ("🌴 Nous avons de magnifiques destinations disponibles ! "
                "Je rencontre une difficulté technique momentanée pour accéder à notre catalogue complet. "
                "Pourriez-vous préciser votre budget et la période souhaitée ? "
                "Notre équipe peut aussi vous contacter directement via le formulaire de contact. 📍")

    if any(w in msg_lower for w in ['prix', 'tarif', 'coût', 'budget', 'combien']):
        return ("💰 Nos destinations proposent des tarifs variés selon la saison et le type de voyage. "
                "Je rencontre une difficulté technique momentanée. "
                "Pour un devis personnalisé, n'hésitez pas à utiliser notre formulaire de contact "
                "et notre équipe vous répondra sous 24h ! ✈️")

    if any(w in msg_lower for w in ['réservation', 'reservation', 'réserver', 'reserver', 'book']):
        return ("📋 Pour toute question concernant une réservation, "
                "notre équipe est disponible via le formulaire de contact. "
                "Je rencontre une difficulté technique momentanée qui m'empêche de vous répondre complètement. "
                "Réessayez dans quelques instants ou contactez-nous directement ! 🙏")

    if any(w in msg_lower for w in ['annul', 'rembours', 'problème', 'probleme', 'erreur']):
        return ("⚠️ Je comprends votre préoccupation. "
                "Je rencontre une difficulté technique momentanée, "
                "mais notre équipe prend ce type de situation très au sérieux. "
                "Veuillez utiliser le formulaire de contact pour qu'un conseiller "
                "traite votre demande en priorité. Nous vous répondrons sous 24h. 🙏")

    # Réponse générique
    return ("Je rencontre une difficulté technique momentanée et ne peux pas vous répondre complètement. "
            "Veuillez réessayer dans quelques instants, "
            "ou contactez-nous via le formulaire de contact pour une réponse garantie. "
            "Nous sommes là pour vous aider ! 🙏✈️")


def summarize_conversation(messages: list) -> dict:
    conversation = "\n".join([
        f"{'Client' if m['role'] == 'user' else 'Assistant'}: {m['content']}"
        for m in messages
    ])

    prompt = f"""Résume cette conversation en 100 mots maximum, puis donne 3 à 5 points clés.

Format OBLIGATOIRE :
RÉSUMÉ: [résumé ici]
POINTS CLÉS: [point1], [point2], [point3]

CONVERSATION :
{conversation}"""

    result     = _gemini_call(prompt, "summary", "")
    summary    = "Résumé non disponible"
    key_points = []

    if result:
        for line in result.split('\n'):
            line = line.strip()
            if line.startswith("RÉSUMÉ:"):
                summary = line.replace("RÉSUMÉ:", "").strip()
            elif line.startswith("POINTS CLÉS:"):
                keys_text  = line.replace("POINTS CLÉS:", "").strip()
                key_points = [p.strip().strip('[]') for p in keys_text.split(',') if p.strip()]

    return {
        'summary':    summary,
        'key_points': key_points if key_points else ["Conversation analysée"]
    }


# ============================================================
# WRAPPERS DE COMPATIBILITÉ (ne pas casser le code existant)
# ============================================================

def classify_message(name: str, subject: str, message: str) -> dict:
    result = process_email_with_ai(name, subject, message)
    return {
        "category":   result["category"],
        "priority":   result["priority"],
        "sentiment":  result["sentiment"],
        "ai_summary": result["ai_summary"]
    }


def generate_suggested_reply(name: str, subject: str, message: str, category: str, sentiment: str) -> str:
    return _default_reply(name, category)


def generate_auto_confirmation(name: str, subject: str) -> str:
    return _default_confirmation(name)


def generate_relance_email(name: str, destinations=None) -> str:
    dest_text = ""
    if destinations:
        dest_list = "\n".join([f"- {d.name} ({d.country})" for d in destinations[:3]])
        dest_text = f"\n\nDestinations qui pourraient vous intéresser :\n{dest_list}"
    return f"""Bonjour {name},

Vous nous manquez ! Nous avons de magnifiques destinations à vous proposer.{dest_text}

N'hésitez pas à nous contacter pour un devis personnalisé.

L'équipe de votre agence de voyage"""


# ============================================================
# TEST
# ============================================================

def test_gemini_connection() -> bool:
    try:
        response = model.generate_content("Réponds juste : OK")
        if response.text:
            print("✅ Connexion Gemini OK")
            return True
        return False
    except Exception as e:
        print(f"❌ Erreur connexion Gemini: {e}")
        return False
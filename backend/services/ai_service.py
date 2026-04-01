import google.generativeai as genai #Permet de communiquer avec Google Gemini
import os #Permet de récupérer les variables d’environnement (.env)
from models import Destination
import json

# ✨ RESPONSABILITÉS:
# 1. Gérer la connexion à Gemini (clé API, modèle)
# 2. Formater les messages pour Gemini
# 3. Appeler Gemini et traiter la réponse
# 4. Résumer les conversations



# Configuration Gemini
#Récupération de la clé API
GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')
if not GEMINI_API_KEY:
    raise ValueError("❌ GEMINI_API_KEY non trouvée dans .env")

#Configuration du modèle
genai.configure(api_key=GEMINI_API_KEY)

GEMINI_MODEL='gemini-2.5-flash'
try:
    model = genai.GenerativeModel('gemini-2.5-flash')
except Exception as e:
    # Attempt to list available models to provide a clearer error message
    available = None
    try:
        # try common listing method names; some SDK versions differ
        if hasattr(genai, 'list_models'):
            available = genai.list_models()
        elif hasattr(genai, 'get_models'):
            available = genai.get_models()
    except Exception:
        available = None

    models_list = None
    try:
        # If listing returned an iterable of model objects or names, normalize it
        if available:
            if isinstance(available, dict) and 'models' in available:
                models_list = [m.get('name') or str(m) for m in available['models']]
            else:
                models_list = [getattr(m, 'name', str(m)) for m in available]
    except Exception:
        models_list = None

    more = f" Available models: {models_list}" if models_list else ""
    raise RuntimeError(f"Failed to initialize Gemini model '{GEMINI_MODEL}': {e}.{more}") from e
# Générer une réponse intelligente au client.
# def get_ai_response(messages, context=""):
    
#     # Construire la conversation
#     # Client: ...
#     # Assistant: ...
#     # Dernier message utilisateur
#     user_message = messages[-1]["content"]
#     # Analyse de la question
#     filters = analyze_user_question(user_message)
#     # Recherche dans la base Destination
#     destinations = search_destinations(filters)
#     # Créer le contexte pour Gemini
#     context = build_context(destinations)
    
#     conversation = ""
#     for msg in messages:
#         role = "Client" if msg['role'] == 'user' else "Assistant"
#         conversation += f"\n{role}: {msg['content']}"
    
#     system_prompt = f"""Tu es un assistant de support client professionnelle pour une agence de voyages.
# Sois courtois, utile et respectueux.
# Aide les clients avec les réservations, les destinations, les paiements, et les questions générales.

# CONTEXTE IMPORTANT:
# {context}

# CONVERSATION ACTUELLE:{conversation}

# Maintenant, réponds à la dernière question du client de manière concise et utile."""
    
#     # temperature :
#     # 0.2 → réponse très stricte
#     # 0.7 → plus naturelle
#     # max_output_tokens → limite la taille

#     try:
#         response = model.generate_content(
#             system_prompt,
#             generation_config=genai.types.GenerationConfig(
#                 temperature=0.7,
#                 max_output_tokens=500
#             )
#         )
        
#         if response.text:
#             return response.text.strip()
#         else:
#             return "Désolé, je n'ai pas pu générer une réponse. Veuillez réessayer."
            
#     except Exception as e:
#         print(f"❌ ERREUR GEMINI: {str(e)}")
#         return f"Désolé, une erreur est survenue: {str(e)}"

def get_ai_response(messages, user_context=""):
    """
    Génère une réponse IA avec Gemini en utilisant RAG.
    
    Args:
        messages: liste de dicts {'role': 'user'/'assistant', 'content': 'texte'}
        user_context: infos supplémentaires sur le client
    
    Returns:
        str: réponse Gemini
    """
    # 1️⃣ Dernier message utilisateur (pour extraire filtres)
    user_message = messages[-1]["content"]
    # 2️⃣ Analyse de la question pour créer filtres
    filters = analyze_user_question(user_message)

    # 3️⃣ Récupération des destinations depuis la DB
    destinations = search_destinations(filters)

    # 4️⃣ Construction du contexte pour Gemini
    context = build_context(destinations)

    # Ajout du contexte utilisateur si fourni
    if user_context:
        context = f"Profil utilisateur: {user_context}\n\n{context}"

    # 5️⃣ Historique complet de la conversation
    conversation = ""
    for msg in messages:
        role = "Client" if msg['role'] == 'user' else "Assistant"
        conversation += f"\n{role}: {msg['content']}"

    # 6️⃣ Prompt pour Gemini
    system_prompt = f"""Tu es un assistant de support client professionnel pour une agence de voyages.
Sois courtois, utile et respectueux.
Aide les clients avec les réservations, les destinations, les paiements et les questions générales.
    Règles importantes :
    - Réponds de façon claire.
    - N'utilise PAS de Markdown.
    - N'utilise PAS de ** ou *.
    - Utilise des phrases simples.
CONTEXTE IMPORTANT:
{context}

CONVERSATION ACTUELLE:{conversation}

Maintenant, réponds à la dernière question du client de manière concise et utile."""

    try:
        response = model.generate_content(
            system_prompt,
            generation_config=genai.types.GenerationConfig(
                temperature=0.7,
                # max_output_tokens=500
            )
        )
        return response.text.strip() if response.text else "Désolé, je n'ai pas pu générer de réponse."
    except Exception as e:
        print(f"❌ ERREUR GEMINI: {e}")
        return f"Désolé, une erreur est survenue: {e}"


def summarize_conversation(messages):
    """
    Résume une conversation complète avec Gemini (GRATUIT)
    
    Args:
        messages: List de dicts {'role': 'user'/'assistant', 'content': 'texte'}
    
    Returns:
        dict: {'summary': str, 'key_points': list}
    """
    
    # Construire le texte de la conversation
    # Résumer discussion admin ↔ client
    # RÉSUMÉ:
    # POINTS CLÉS:
    conversation = ""
    for msg in messages:
        role = "Client" if msg['role'] == 'user' else "Assistant"
        conversation += f"{role}: {msg['content']}\n"
    
    prompt = f"""Résume la conversation suivante de manière concise (maximum 150 mots).
Ensuite, identifie 3-5 points clés de la discussion.

Format de réponse TRÈS IMPORTANT:
RÉSUMÉ: [votre résumé ici - max 150 mots]
POINTS CLÉS: [point1], [point2], [point3], [point4]

CONVERSATION À RÉSUMER:
{conversation}

Réponds maintenant dans le format exact spécifié ci-dessus."""
    
    try:
        model_summary = genai.GenerativeModel('gemini-2.5-flash')
        response = model_summary.generate_content(
            prompt,
            generation_config=genai.types.GenerationConfig(
                temperature=0.7,
                max_output_tokens=300
            )
        )
        
        if not response.text:
            return {
                'summary': 'Impossible de générer le résumé',
                'key_points': []
            }
        
        result = response.text.strip()
        
        # Parser la réponse
        summary = ""
        key_points = []
        # Transformer texte IA → structure Python exploitable.
        lines = result.split('\n')
        for line in lines:
            if line.startswith("RÉSUMÉ:"):
                summary = line.replace("RÉSUMÉ:", "").strip()
            elif line.startswith("POINTS CLÉS:"):
                keys_text = line.replace("POINTS CLÉS:", "").strip()
                # Nettoyer et diviser les points
                key_points = [
                    p.strip().strip('[]') 
                    for p in keys_text.split(',') 
                    if p.strip()
                ]
        
        return {
            'summary': summary if summary else 'Conversation résumée',
            'key_points': key_points if key_points else ['Informations extraites de la conversation']
        }
        
    except Exception as e:
        print(f"❌ ERREUR RÉSUMÉ GEMINI: {str(e)}")
        return {
            'summary': f'Erreur: {str(e)}',
            'key_points': []
        }


def test_gemini_connection():
    """
    Teste la connexion à Gemini
    Utile pour vérifier la configuration
    """
    try:
        test_prompt = "Bonjour, peux-tu me dire que tu fonctionnes correctement?"
        response = model.generate_content(test_prompt)
        
        if response.text:
            print("✅ Connexion Gemini OK")
            print(f"Réponse test: {response.text[:100]}...")
            return True
        else:
            print("❌ Pas de réponse de Gemini")
            return False
            
    except Exception as e:
        print(f"❌ Erreur connexion Gemini: {str(e)}")
        return False
    
def analyze_user_question(message):
    """
    Analyse la question du client et extrait des filtres pour la recherche
    Exemple: continent, type, saison, budget,location (ville)

    """
    prompt = f"""
    Analyse cette demande de voyage et retourne uniquement les informations importantes en JSON:
    - continent
    - type de voyage
    - saison
    - budget (cheap, medium, expensive)
    - location (ville)

    Exemple de réponse JSON:
    {{
        "continent": "Europe",
        "type": "Beach",
        "location": "Paris",
        "season": "Summer",
        "budget": "cheap"
    }}

    Question utilisateur: {message}
    """

    response = model.generate_content(prompt)
    
    try:
        # json.loads() pour transformer la réponse textuelle de Gemini en un dict Python exploitable
        # response.text contient la réponse textuelle de Gemini, qui devrait être un JSON formaté 
        filters = json.loads(response.text)
    except Exception:
        filters = {}
    return filters


def search_destinations(filters):
    query = Destination.query

    if filters.get("continent"):
        query = query.filter(Destination.continent.ilike(f"%{filters['continent']}%"))
    
    if filters.get("type"):
        query = query.filter(Destination.type.ilike(f"%{filters['type']}%"))
    
    if filters.get("season"):
        query = query.filter(Destination.bestSeason.ilike(f"%{filters['season']}%"))
    
    if filters.get("budget"):
        if filters["budget"] == "cheap":
            query = query.filter(Destination.avgCostUSD < 100)
        elif filters["budget"] == "medium":
            query = query.filter(Destination.avgCostUSD.between(100, 300))
        elif filters["budget"] == "expensive":
            query = query.filter(Destination.avgCostUSD > 300)

    return query.all()

def build_context(destinations):
    context = ""
    for d in destinations:
        context += f"""
        Destination: {d.name}
        Country: {d.country}
        Continent: {d.continent}
        Type: {d.type}
        Best Season: {d.bestSeason}
        Average Cost: {d.avgCostUSD}
        Description: {d.Description}
        """
    return context

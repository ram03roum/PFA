import os
import openai
from dotenv import load_dotenv

load_dotenv(os.path.join(os.path.dirname(__file__), '../.env'))

OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')
OPENAI_API_URL = os.getenv('OPENAI_API_URL', 'https://api.openai.com/v1')

if OPENAI_API_KEY:
    openai.api_key = OPENAI_API_KEY
if OPENAI_API_URL:
    openai.api_base = OPENAI_API_URL

MODEL_NAME = os.getenv('OPENAI_MODEL', 'gpt-3.5-turbo')


def _call_openai(prompt: str) -> str:
    if not OPENAI_API_KEY:
        raise RuntimeError('OPENAI_API_KEY non configurée dans le fichier .env')

    response = openai.ChatCompletion.create(
        model=MODEL_NAME,
        messages=[{'role': 'user', 'content': prompt}],
        temperature=0.6,
        max_tokens=350,
        n=1
    )

    message = response.choices[0].message.get('content', '').strip()
    return message


def generate_email(customer_name: str, subject: str, context: str) -> str:
    prompt = f"""
Tu es un assistant commercial intelligent pour une plateforme de voyage.
Rédige un email professionnel à [{customer_name}] sur le sujet suivant : {subject}.
Contexte : {context}
- Objet court (1 phrase)
- Corps : 4 phrases maximum
- Ton : chaleureux, clair, orienté conversion.
"""
    return _call_openai(prompt)


def summarize_conversation(conversation_text: str) -> str:
    prompt = f"""
Tu es un assistant qui résume les échanges clients.
Résumé court en bullet points (3-4 éléments).
Conversation :
{conversation_text}
"""
    return _call_openai(prompt)


def suggest_followup(conversation_text: str) -> str:
    prompt = f"""
Tu es un assistant CRM intelligent.
Sur la base de cette conversation, propose 2 actions de relance et un message court à envoyer au client.
Conversation :
{conversation_text}
"""
    return _call_openai(prompt)

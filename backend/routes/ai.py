from flask import Blueprint, jsonify, request
from openai_service import generate_email, summarize_conversation, suggest_followup

ai_bp = Blueprint('ai', __name__, url_prefix='/ai')

@ai_bp.route('/email', methods=['POST'])
def ai_generate_email():
    payload = request.json or {}
    customer_name = payload.get('customer_name', 'client')
    subject = payload.get('subject', 'Votre offre voyage')
    context = payload.get('context', '')

    try:
        result = generate_email(customer_name, subject, context)
        return jsonify({'email': result})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@ai_bp.route('/summary', methods=['POST'])
def ai_summarize():
    payload = request.json or {}
    conversation = payload.get('conversation', '')

    if not conversation:
        return jsonify({'error': 'conversation requise'}), 400

    try:
        result = summarize_conversation(conversation)
        return jsonify({'summary': result})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@ai_bp.route('/followup', methods=['POST'])
def ai_followup():
    payload = request.json or {}
    conversation = payload.get('conversation', '')

    if not conversation:
        return jsonify({'error': 'conversation requise'}), 400

    try:
        result = suggest_followup(conversation)
        return jsonify({'followup': result})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

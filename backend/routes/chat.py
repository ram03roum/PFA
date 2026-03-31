from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from extensions import db
from models import User, Conversation, Message, ConversationSummary
from services.ai_service import get_ai_response, summarize_conversation
from datetime import datetime

chat_bp = Blueprint('chat', __name__, url_prefix='/api')

# ─────────────────────────────────────────────────
# GET /api/chat/conversations
# Liste toutes les conversations de l'utilisateur
# ─────────────────────────────────────────────────
@chat_bp.route('/chat/conversations', methods=['GET'])
@jwt_required()
def get_conversations():
    try:
        user_id = get_jwt_identity()
        # Les trie par date (plus récente en premier)
        conversations = Conversation.query.filter_by(user_id=user_id).order_by(
            Conversation.updated_at.desc()
        ).all()
        
        return jsonify({
            'success': True,
            'data': [c.to_dict() for c in conversations]
        }), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500


# ─────────────────────────────────────────────────
# POST /api/chat/conversations
# Créer une nouvelle conversation
# ─────────────────────────────────────────────────
@chat_bp.route('/chat/conversations', methods=['POST'])
@jwt_required()
def create_conversation():
    try:
        user_id = get_jwt_identity()
        data = request.json
        
        conv = Conversation(
            user_id=user_id,
            title=data.get('title', 'Nouvelle conversation'),
            topic=data.get('topic', 'general')
        )
        
        db.session.add(conv)
        db.session.commit()
        
        return jsonify({
            'success': True,
            'data': conv.to_dict()
        }), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500


# ─────────────────────────────────────────────────
# GET /api/chat/conversations/<id>
# Récupère une conversation avec tous ses messages
# ─────────────────────────────────────────────────
@chat_bp.route('/chat/conversations/<int:conv_id>', methods=['GET'])
@jwt_required()
def get_conversation(conv_id):
    try:
        user_id = get_jwt_identity()
        conv = Conversation.query.filter_by(id=conv_id, user_id=user_id).first()
        
        if not conv:
            return jsonify({'error': 'Conversation non trouvée'}), 404
        
        messages = Message.query.filter_by(conversation_id=conv_id).all()
        
        return jsonify({
            'success': True,
            'conversation': conv.to_dict(),
            'messages': [m.to_dict() for m in messages]
        }), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500


# ─────────────────────────────────────────────────
# POST /api/chat/message
# Envoyer un message et obtenir une réponse IA
# ─────────────────────────────────────────────────
@chat_bp.route('/chat/message', methods=['POST'])
@jwt_required()
def send_message():
    try:
        user_id = get_jwt_identity()
        data = request.json
        
        conversation_id = data.get('conversation_id')
        user_message = data.get('message')
        
        # Vérifier que la conversation appartient à l'utilisateur
        conv = Conversation.query.filter_by(id=conversation_id, user_id=user_id).first()
        if not conv:
            return jsonify({'error': 'Conversation invalide'}), 403
        
        # 1. Sauvegarder le message de l'utilisateur
        user_msg = Message(
            conversation_id=conversation_id,
            sender_type='user',
            content=user_message
        )
        db.session.add(user_msg)
        db.session.commit()
        
        # 2. Récupérer l'historique pour le contexte
        all_messages = Message.query.filter_by(conversation_id=conversation_id).all()
        
        # 3. Construire le format pour Gemini
        messages_for_ai = [
            {'role': 'user' if m.sender_type == 'user' else 'assistant', 'content': m.content}
            for m in all_messages
        ]
        
        # 4. Récupérer le contexte utilisateur
        user = User.query.get(user_id)
        context = f"Client: {user.name}, Email: {user.email}"
        
        # 5. Générer réponse IA avec Gemini
        ai_response = get_ai_response(messages_for_ai, context)
        
        # 6. Sauvegarder la réponse IA
        ai_msg = Message(
            conversation_id=conversation_id,
            sender_type='ai',
            content=ai_response
        )
        db.session.add(ai_msg)
        
        # 7. Mettre à jour la date de la conversation
        conv.updated_at = datetime.utcnow()
        db.session.commit()
        
        return jsonify({
            'success': True,
            'user_message': user_msg.to_dict(),
            'ai_message': ai_msg.to_dict()
        }), 201
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500


# ─────────────────────────────────────────────────
# GET /api/chat/summary/<id>
# Générer un résumé de la conversation
# ─────────────────────────────────────────────────
@chat_bp.route('/chat/summary/<int:conversation_id>', methods=['GET'])
@jwt_required()
def get_summary(conversation_id):
    try:
        user_id = get_jwt_identity()
        conv = Conversation.query.filter_by(id=conversation_id, user_id=user_id).first()
        
        if not conv:
            return jsonify({'error': 'Conversation non trouvée'}), 404
        
        # Vérifier si un résumé existe déjà
        existing_summary = ConversationSummary.query.filter_by(conversation_id=conversation_id).first()
        if existing_summary:
            return jsonify({
                'success': True,
                'data': existing_summary.to_dict()
            }), 200
        
        # Générer le résumé
        messages = Message.query.filter_by(conversation_id=conversation_id).all()
        messages_for_ai = [
            {'role': 'user' if m.sender_type == 'user' else 'assistant', 'content': m.content}
            for m in messages
        ]
        
        summary_data = summarize_conversation(messages_for_ai)
        
        # Sauvegarder le résumé
        summary = ConversationSummary(
            conversation_id=conversation_id,
            summary=summary_data['summary'],
            key_points=summary_data['key_points']
        )
        db.session.add(summary)
        db.session.commit()
        
        return jsonify({
            'success': True,
            'data': summary.to_dict()
        }), 201
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500


# ─────────────────────────────────────────────────
# DELETE /api/chat/conversations/<id>
# Fermer une conversation
# ─────────────────────────────────────────────────
@chat_bp.route('/chat/conversations/<int:conv_id>', methods=['DELETE'])
@jwt_required()
def close_conversation(conv_id):
    try:
        user_id = get_jwt_identity()
        conv = Conversation.query.filter_by(id=conv_id, user_id=user_id).first()
        
        if not conv:
            return jsonify({'error': 'Conversation non trouvée'}), 404
        
        conv.status = 'closed'
        db.session.commit()
        
        return jsonify({
            'success': True,
            'message': 'Conversation fermée'
        }), 200
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from extensions import db
from models import User, Conversation, Message, ConversationSummary, ContactMessage
from services.ai_service import get_ai_response, summarize_conversation
from datetime import datetime
import threading


chat_bp = Blueprint('chat', __name__, url_prefix='/api')

 
# ─── Helpers ─────────────────────────────────────────────────────────────────
 
def _auto_summarize_if_needed(app, conversation_id: int):
    """
    Génère automatiquement un résumé en arrière-plan si :
    - La conversation a ≥ 5 messages
    - Aucun résumé n'existe encore (ou le dernier date de plus de 10 messages)
    Appelé après chaque envoi de message — sans bloquer la réponse.
    """
    with app.app_context():
        try:
            messages = Message.query.filter_by(conversation_id=conversation_id).all()
            count = len(messages)
 
            # Seuil : 5 messages, puis tous les 10 messages suivants
            existing = ConversationSummary.query.filter_by(
                conversation_id=conversation_id
            ).first()
 
            should_summarize = (
                count >= 5 and not existing
            ) or (
                existing and count % 10 == 0 and count >= 10
            )
 
            if not should_summarize:
                return
 
            print(f"🤖 Auto-résumé conversation {conversation_id} ({count} messages)...")
 
            messages_for_ai = [
                {'role': 'user' if m.sender_type == 'user' else 'assistant',
                 'content': m.content}
                for m in messages
            ]
 
            summary_data = summarize_conversation(messages_for_ai)
 
            if existing:
                # Mettre à jour le résumé existant
                existing.summary    = summary_data['summary']
                existing.key_points = summary_data['key_points']
                existing.updated_at = datetime.utcnow()
            else:
                # Créer un nouveau résumé
                summary = ConversationSummary(
                    conversation_id=conversation_id,
                    summary=summary_data['summary'],
                    key_points=summary_data['key_points']
                )
                db.session.add(summary)
 
            db.session.commit()
            print(f"✅ Résumé auto sauvegardé — conv {conversation_id}")
 
        except Exception as e:
            print(f"❌ Erreur auto-résumé: {e}")
 

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
        
        # ✅ Résumé automatique en arrière-plan (sans bloquer la réponse)
        app    = current_app._get_current_object()
        thread = threading.Thread(
            target=_auto_summarize_if_needed,
            args=(app, conversation_id)
        )
        thread.daemon = True
        thread.start()
        return jsonify({
            'success': True,
            'user_message': user_msg.to_dict(),
            'ai_message': ai_msg.to_dict()
        }), 201
        
    except Exception as e:
        db.session.rollback()
        import traceback
        print(f"❌ ERREUR CHAT: {e}")
        print(traceback.format_exc())  # ← ajoute cette ligne
        return jsonify({'error': str(e)}), 500
 
 
def _force_summarize(app, conversation_id: int):
    """Force la génération du résumé à la fermeture de conversation."""
    with app.app_context():
        try:
            messages = Message.query.filter_by(conversation_id=conversation_id).all()
            if not messages:
                return
 
            messages_for_ai = [
                {'role': 'user' if m.sender_type == 'user' else 'assistant',
                 'content': m.content}
                for m in messages
            ]
            summary_data = summarize_conversation(messages_for_ai)
 
            existing = ConversationSummary.query.filter_by(
                conversation_id=conversation_id
            ).first()
 
            if existing:
                existing.summary    = summary_data['summary']
                existing.key_points = summary_data['key_points']
                existing.updated_at = datetime.utcnow()
            else:
                summary = ConversationSummary(
                    conversation_id=conversation_id,
                    summary=summary_data['summary'],
                    key_points=summary_data['key_points']
                )
                db.session.add(summary)
 
            db.session.commit()
            print(f"✅ Résumé final généré — conv {conversation_id}")
        except Exception as e:
            print(f"❌ Erreur résumé final: {e}")



# ─── Route admin : profil complet d'un client ────────────────────────────────
 
@chat_bp.route('/admin/client-profile/<string:email>', methods=['GET'])
@jwt_required()
def get_client_profile(email):
    """
    ✅ SMART CRM — Profil complet d'un client pour l'admin.
    Retourne :
    - Infos client
    - Résumés de toutes ses conversations chat
    - Ses messages de contact
    - Son segment et risque churn
    """
    try:
        user = User.query.filter_by(email=email).first()
 
        if not user:
            # Client non inscrit — juste ses messages de contact
            contact_messages = ContactMessage.query.filter_by(email=email).order_by(
                ContactMessage.created_at.desc()
            ).limit(5).all()
            return jsonify({
                'success':         True,
                'registered':      False,
                'contact_history': [m.to_dict() for m in contact_messages],
                'conversations':   [],
                'chat_summaries':  [],
                'total_messages':  len(contact_messages)
            }), 200
 
        # Conversations et résumés
        conversations = Conversation.query.filter_by(user_id=user.id).order_by(
            Conversation.updated_at.desc()
        ).all()
 
        chat_summaries = []
        for conv in conversations:
            summary = ConversationSummary.query.filter_by(
                conversation_id=conv.id
            ).first()
            msg_count = Message.query.filter_by(conversation_id=conv.id).count()
 
            chat_summaries.append({
                'conversation_id':    conv.id,
                'conversation_title': conv.title,
                'topic':              conv.topic,
                'status':             conv.status,
                'message_count':      msg_count,
                'created_at':         conv.created_at.strftime('%d/%m/%Y %H:%M') if conv.created_at else '',
                'updated_at':         conv.updated_at.strftime('%d/%m/%Y %H:%M') if conv.updated_at else '',
                'summary':            summary.summary    if summary else None,
                'key_points':         summary.key_points if summary else [],
                'has_summary':        summary is not None
            })
 
        # Messages de contact
        contact_messages = ContactMessage.query.filter_by(email=email).order_by(
            ContactMessage.created_at.desc()
        ).limit(10).all()
 
        # Calcul segment rapide
        from models import Reservation, Favorite, InteractionLog
        reservations = Reservation.query.filter_by(user_id=user.id).count()
        favorites    = Favorite.query.filter_by(user_id=user.id).count()
        score        = reservations * 3 + favorites * 2
 
        if score >= 5:   segment = 'VIP'
        elif score >= 3: segment = 'Régulier'
        elif score > 0:  segment = 'Nouveau'
        else:            segment = 'Inactif'
 
        # Dernière activité
        last_msg = Message.query.join(Conversation).filter(
            Conversation.user_id == user.id
        ).order_by(Message.created_at.desc()).first()
 
        last_activity = None
        if last_msg and last_msg.created_at:
            days = (datetime.utcnow() - last_msg.created_at).days
            last_activity = f"il y a {days} jour(s)"
 
        return jsonify({
            'success':         True,
            'registered':      True,
            'client': {
                'id':           user.id,
                'name':         user.name,
                'email':        user.email,
                'segment':      segment,
                'reservations': reservations,
                'favorites':    favorites,
                'last_activity': last_activity
            },
            'chat_summaries':  chat_summaries,
            'contact_history': [m.to_dict() for m in contact_messages],
            'total_conversations': len(conversations),
            'total_contact_msgs':  len(contact_messages)
        }), 200
 
    except Exception as e:
        print(f"❌ Erreur profil client: {e}")
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
# @chat_bp.route('/chat/conversations/<int:conv_id>', methods=['DELETE'])
# @jwt_required()
# def close_conversation(conv_id):
#     try:
#         user_id = get_jwt_identity()
#         conv = Conversation.query.filter_by(id=conv_id, user_id=user_id).first()
        
#         if not conv:
#             return jsonify({'error': 'Conversation non trouvée'}), 404
        
#         conv.status = 'closed'
#         db.session.commit()
        
#         return jsonify({
#             'success': True,
#             'message': 'Conversation fermée'
#         }), 200
#     except Exception as e:
#         db.session.rollback()
#         return jsonify({'error': str(e)}), 500

###########################################################################
#eni zedetha 

# ── CORRECTION UNIQUE dans chat.py ────────────────────────────────────────────
# Remplace SEULEMENT la fonction close_conversation existante par celle-ci :
# (le reste de ton chat.py reste identique)
 
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
 
        # ✅ AJOUT : générer le résumé final à la fermeture
        messages = Message.query.filter_by(conversation_id=conv_id).all()
        if len(messages) >= 2:
            app    = current_app._get_current_object()
            thread = threading.Thread(
                target=_force_summarize,
                args=(app, conv_id)
            )
            thread.daemon = True
            thread.start()
 
        return jsonify({'success': True, 'message': 'Conversation fermée'}), 200
 
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500
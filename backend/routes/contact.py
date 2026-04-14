# from email import message
# from flask import Blueprint, request, jsonify,current_app
# from flask_jwt_extended import jwt_required, get_jwt_identity  
# from flask_cors import cross_origin
# from extensions import db
# from services.ai_service import classify_message, generate_suggested_reply, generate_auto_confirmation
# from services.mail_service import send_confirmation_to_client, send_reply_to_client
# import threading
# from models import ContactMessage, User, InteractionLog, Reservation, Favorite
# from datetime import datetime

# contact_bp = Blueprint('contact', __name__)


# # ─── Helpers DB (directement ici, pas de fichier séparé) ─────────────────────

# # def save_message(name, email, phone, subject, message):
# #     msg = ContactMessage(
# #         name=name, email=email,
# #         phone=phone, subject=subject, message=message
# #     )
# #     db.session.add(msg)
# #     db.session.commit()
# #     print(f"💾 Message sauvegardé en DB — ID: {msg.id} | De: {name} ({email})")
# #     return msg.id

# def save_message(name, email, phone, subject, message,
#                  contact_type=None, destination_id=None,
#                  destination_name=None, reservation_id=None):
#     msg = ContactMessage(
#         name=name, email=email,
#         phone=phone, subject=subject, message=message,
#         contact_type=contact_type,
#         destination_id=destination_id,
#         destination_name=destination_name,
#         reservation_id=reservation_id
#     )
#     db.session.add(msg)
#     db.session.commit()
#     print(f"💾 Message sauvegardé — ID: {msg.id} | Type: {contact_type} | "
#           f"Destination: {destination_name or 'N/A'} | Réservation: {reservation_id or 'N/A'}")
#     return msg.id

# def update_ai_fields(message_id, category, priority, sentiment, ai_summary, suggested_reply):
#     msg = ContactMessage.query.get(message_id)
#     if msg:
#         msg.category        = category
#         msg.priority        = priority
#         msg.sentiment       = sentiment
#         msg.ai_summary      = ai_summary
#         msg.suggested_reply = suggested_reply
#         db.session.commit()
#         print(f"💾 Champs IA mis à jour — ID: {message_id} | Cat: {category} | Prio: {priority} | Sentiment: {sentiment}")


# def mark_email_sent(message_id):
#     msg = ContactMessage.query.get(message_id)
#     if msg:
#         msg.auto_email_sent = True
#         db.session.commit()
#         print(f"📧 Email marqué comme envoyé — ID: {message_id}")

# # ─── Traitement IA en arrière-plan ───────────────────────────────────────────

# # def process_with_ai(message_id, name, email, subject, message):
# #     try:
# #         # 1. Classification
# #         classification  = classify_message(name, subject, message)
# #         category        = classification.get("category", "info")
# #         priority        = classification.get("priority", "moyenne")
# #         sentiment       = classification.get("sentiment", "neutre")
# #         ai_summary      = classification.get("ai_summary", "")

# #         # 2. Réponse suggérée pour l'admin
# #         suggested_reply = generate_suggested_reply(name, subject, message, category, sentiment)

# #         # 3. Sauvegarde en DB
# #         update_ai_fields(message_id, category, priority, sentiment, ai_summary, suggested_reply)

# #         # 4. Email de confirmation au client
# #         confirmation_body = generate_auto_confirmation(name, subject)
# #         sent = send_confirmation_to_client(email, name, subject, confirmation_body)
# #         if sent:
# #             mark_email_sent(message_id)

# #     except Exception as e:
# #         print(f"Erreur traitement IA: {e}")


# # ─── Calcul score de priorité global ────────────────────────────────────────

# def compute_priority_score(msg):
#     """
#     Calcule le score de priorité global d'un message
#     en combinant : catégorie IA + segment client + churn risk + ancienneté
#     """
#     score = 0
#     details = {}

#     # ── Critère 1 — Catégorie Gemini ──────────────────────────────────────
#     category_points = {
#         'urgent':        40,
#         'reclamation':   30,
#         'demande_devis': 20,
#         'info':          10
#     }
#     cat_pts = category_points.get(msg.category, 10)
#     score  += cat_pts
#     details['categorie_pts'] = cat_pts

#     # ── Critère 2 — Segment client ─────────────────────────────────────────
#     user           = User.query.filter_by(email=msg.email).first()
#     segment        = 'Inactif'
#     churn_risk     = 'Faible'
#     segment_pts    = 5
#     churn_pts      = 5

#     if user:
#         # Calcul rapide du segment
#         reservations = Reservation.query.filter_by(user_id=user.id).count()
#         favorites    = Favorite.query.filter_by(user_id=user.id).count()
#         views        = InteractionLog.query.filter_by(
#                            user_id=user.id, action='view'
#                        ).count()

#         recent_score = reservations * 3 + favorites * 2 + views * 1

#         if recent_score >= 5:    segment = 'VIP'
#         elif recent_score >= 3:  segment = 'Régulier'
#         elif reservations > 0:   segment = 'Nouveau'
#         else:                    segment = 'Inactif'

#         # Calcul rapide du churn risk
#         last_interaction = InteractionLog.query.filter_by(
#             user_id=user.id
#         ).order_by(InteractionLog.created_at.desc()).first()

#         if last_interaction:
#             days_inactive = (datetime.utcnow() - last_interaction.created_at).days
#             if days_inactive > 7:    churn_risk = 'Critique'
#             elif days_inactive > 5:  churn_risk = 'Élevé'
#             elif days_inactive > 3:  churn_risk = 'Moyen'
#             else:                    churn_risk = 'Faible'
#         else:
#             churn_risk = 'Critique'

#         segment_points = {
#             'VIP':      30,
#             'Régulier': 20,
#             'Nouveau':  10,
#             'Inactif':   5
#         }
#         churn_points = {
#             'Critique': 20,
#             'Élevé':    15,
#             'Moyen':    10,
#             'Faible':    5
#         }
#         segment_pts = segment_points.get(segment, 5)
#         churn_pts   = churn_points.get(churn_risk, 5)

#     score += segment_pts
#     score += churn_pts
#     details['segment_pts']  = segment_pts
#     details['churn_pts']    = churn_pts

#     # ── Critère 4 — Ancienneté du message ──────────────────────────────────
#     anciennete_pts = 0
#     if msg.created_at:
#         hours = (datetime.utcnow() - msg.created_at).total_seconds() / 3600
#         if hours > 24:   anciennete_pts = 10
#         elif hours > 12: anciennete_pts = 7
#         elif hours > 6:  anciennete_pts = 4

#     score += anciennete_pts
#     details['anciennete_pts'] = anciennete_pts

#     print(f"🎯 Score priorité — ID: {msg.id} | {msg.name} | "
#           f"Cat:{cat_pts}pts + Seg:{segment_pts}pts + "
#           f"Churn:{churn_pts}pts + Ancienneté:{anciennete_pts}pts = {score}pts | "
#           f"Segment:{segment} | Churn:{churn_risk}")

#     return {
#         'priority_score':  score,
#         'client_segment':  segment,
#         'churn_risk':      churn_risk,
#         'score_details':   details
#     }


# # ─── Thread IA ───────────────────────────────────────────────────────────────
# def process_with_ai(app, message_id, name, email, subject, message, destination_name=None, reservation_id=None, contact_type=None):
#     with app.app_context():
#         try:
#             print(f"🤖 Démarrage IA pour message {message_id}...")
            
#             from services.ai_service import classify_message, generate_suggested_reply, generate_auto_confirmation
#             from services.mail_service import send_confirmation_to_client

#             # 1. Classification
#             print("📊 Classification en cours...")
#             classification = classify_message(name, subject, message)
#             print(f"✅ Classification: {classification}")
            
#             category   = classification.get("category", "info")
#             priority   = classification.get("priority", "moyenne")
#             sentiment  = classification.get("sentiment", "neutre")
#             ai_summary = classification.get("ai_summary", "")

#             # 2. Réponse suggérée
#             print("✍️ Génération réponse suggérée...")
#             try:
#                 suggested_reply = generate_suggested_reply(name, subject, message, category, sentiment)
#                 print(f"✅ Réponse suggérée générée: {suggested_reply[:50]}...")
#             except Exception as e:
#                 print(f"❌ ERREUR generate_suggested_reply: {e}")
#                 suggested_reply = f"Bonjour {name}, merci pour votre message. Nous reviendrons vers vous rapidement."

#             # 3. Sauvegarde en DB
#             print("💾 Sauvegarde en DB...")
#             update_ai_fields(message_id, category, priority, sentiment, ai_summary, suggested_reply)
#             print(f"✅ DB mise à jour")

#             # 4. Email confirmation
#             print("📧 Envoi email confirmation...")
#             try:
#                 confirmation_body = generate_auto_confirmation(name, subject)
#                 print(f"✅ Email confirmation généré: {confirmation_body[:50]}...")
#             except Exception as e:
#                 print(f"❌ ERREUR generate_auto_confirmation: {e}")
#                 confirmation_body = f"Bonjour {name}, votre message a été reçu. Réponse sous 24h."
            
#             sent = send_confirmation_to_client(email, name, subject, confirmation_body)
#             if sent:
#                 mark_email_sent(message_id)
#                 print(f"✅ Email envoyé à {email}")
#             else:
#                 print(f"❌ Email non envoyé à {email}")

#         except Exception as e:
#             import traceback
#             print(f"❌ ERREUR THREAD IA: {e}")
#             print(traceback.format_exc())


# # ─── Routes ──────────────────────────────────────────────────────────────────

# # @contact_bp.route('/api/contact', methods=['POST'])
# # @cross_origin()
# # def submit_contact():
# #     data    = request.get_json()
# #     name    = data.get('name', '').strip()
# #     email   = data.get('email', '').strip()
# #     phone   = data.get('phone', '').strip()
# #     subject = data.get('subject', '').strip()
# #     message = data.get('message', '').strip()

# #     print(f"\n📩 Nouveau message reçu — De: {name} ({email}) | Sujet: {subject}")

# #     if not name or not email or not message:
# #         print("❌ Champs requis manquants")
# #         return jsonify({'error': 'Champs requis manquants'}), 400

# #     # Sauvegarde immédiate
# #     message_id = save_message(name, email, phone, subject, message)

# # # on utilise le thread pour ne pas bloquer la réponse au client pendant le traitement IA, qui peut être long (classification + email)
# # # cad il renvoie true dès que le message est sauvegardé, et le traitement IA se fait ensuite en arrière-plan sans impacter l'expérience utilisateur.
# # # sans thread le client attendrait la fin du traitement IA (classification + email) avant de recevoir la confirmation que son message a été reçu, 
# # # ce qui peut être long et frustrant. Avec le thread, le client reçoit une réponse immédiate confirmant que son message a été reçu, pendant que le 
# # # traitement IA se fait en parallèle.

# #     # IA en arrière-plan (ne bloque pas la réponse)
# #     app = current_app._get_current_object()
# #     thread = threading.Thread(
# #         target=process_with_ai,
# #         args=(app, message_id, name, email, subject, message)
# #     )
# #     thread.daemon = True
# #     thread.start()
# #     print(f"🔄 Thread IA lancé pour message {message_id}")

# #     return jsonify({'success': True, 'message': 'Message envoyé avec succès'}), 201



# @contact_bp.route('/api/contact', methods=['POST'])
# @cross_origin()
# def submit_contact():
#     data             = request.get_json()
#     name             = data.get('name', '').strip()
#     email            = data.get('email', '').strip()
#     phone            = data.get('phone', '').strip()
#     subject          = data.get('subject', '').strip()
#     message          = data.get('message', '').strip()
#     contact_type     = data.get('contact_type', 'general')
#     destination_id   = data.get('destination_id')
#     destination_name = data.get('destination_name', '').strip()
#     reservation_id   = data.get('reservation_id')

#     if not name or not email or not message:
#         return jsonify({'error': 'Champs requis manquants'}), 400

#     # Enrichir le sujet automatiquement selon le type
#     if not subject:
#         if contact_type == 'reservation' and reservation_id:
#             subject = f"Concernant ma réservation #{reservation_id}"
#         elif contact_type == 'destination' and destination_name:
#             subject = f"Question sur {destination_name}"
#         else:
#             subject = "Question générale"

#     message_id = save_message(
#         name, email, phone, subject, message,
#         contact_type, destination_id, destination_name, reservation_id
#     )

#     app    = current_app._get_current_object()
#     thread = threading.Thread(
#         target=process_with_ai,
#         args=(app, message_id, name, email, subject,
#               message, destination_name, reservation_id, contact_type)
#     )
#     thread.daemon = True
#     thread.start()

#     return jsonify({'success': True, 'message': 'Message envoyé avec succès'}), 201

# @contact_bp.route('/api/admin/notifications/count', methods=['GET'])
# @cross_origin()
# def unread_count():
#     count = ContactMessage.query.filter_by(is_read=False).count()
#     print(f"🔔 Notifications non lues: {count}")
#     return jsonify({'unread_count': count})


# @contact_bp.route('/api/admin/messages', methods=['GET'])
# @cross_origin()
# def list_messages():
#     msgs = ContactMessage.query.order_by(ContactMessage.created_at.desc()).all()
#     print(f"📋 Liste messages demandée — {len(msgs)} message(s)")
#     return jsonify({'messages': [m.to_dict() for m in msgs]})

# @contact_bp.route('/api/admin/messages/prioritized', methods=['GET'])
# @cross_origin()
# def get_prioritized_messages():
#     """Retourne les messages non lus triés par score de priorité global"""
#     print("\n🚨 Calcul des priorités des messages...")

#     msgs   = ContactMessage.query.filter_by(is_read=False).all()
#     print(f"📊 {len(msgs)} message(s) non lu(s) à analyser")

#     result = []
#     for msg in msgs:
#         data     = msg.to_dict()
#         priority = compute_priority_score(msg)
#         data.update(priority)
#         result.append(data)

#     # Tri par score décroissant
#     result.sort(key=lambda x: x['priority_score'], reverse=True)

#     print(f"\n📊 Classement final des priorités :")
#     for i, m in enumerate(result):
#         print(f"  #{i+1} | {m['name']:20} | Score: {m['priority_score']:3}pts "
#               f"| {m['client_segment']:10} | Churn: {m['churn_risk']}")

#     return jsonify({'messages': result}), 200

# @contact_bp.route('/api/admin/messages/<int:message_id>', methods=['GET'])
# @cross_origin()
# def get_message(message_id):
#     msg = ContactMessage.query.get_or_404(message_id)
#     msg.is_read = True
#     db.session.commit()
#     print(f"👁️ Message {message_id} ouvert et marqué comme lu")
#     return jsonify({'message': msg.to_dict()})


# @contact_bp.route('/api/admin/messages/<int:message_id>/reply', methods=['POST'])
# @cross_origin()
# def send_reply(message_id):
#     msg        = ContactMessage.query.get_or_404(message_id)
#     data       = request.get_json()
#     reply_body = data.get('reply', '').strip()

#     print(f"📩 Tentative d'envoi d'une réponse à {msg.email}")

#     sent = send_reply_to_client(msg.email, msg.name, msg.subject, reply_body)
#     if sent:
#         print(f"✅ Réponse envoyée avec succès à {msg.email}")
#         return jsonify({'success': True, 'message': 'Réponse envoyée au client'})
#     print(f"❌ Échec envoi réponse à {msg.email}")
#     return jsonify({'error': 'Échec envoi email'}), 500


# @contact_bp.route('/api/admin/messages/<int:message_id>/read', methods=['PUT'])
# @cross_origin()
# def mark_read(message_id):
#     msg = ContactMessage.query.get_or_404(message_id)
#     msg.is_read = True
#     db.session.commit()
#     print(f"✅ Message {message_id} marqué comme lu")
#     return jsonify({'success': True})


# @contact_bp.route('/api/admin/messages/read-all', methods=['PUT'])
# @cross_origin()
# def read_all():
#     ContactMessage.query.update({'is_read': True})
#     db.session.commit()
#     print("✅ Tous les messages marqués comme lus")
#     return jsonify({'success': True})

# @contact_bp.route('/api/contact/my-reservations', methods=['GET'])
# @cross_origin()
# def get_client_reservations():
#     """
#     Retourne les réservations du client connecté
#     pour le formulaire de contact
#     """
#     from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity
#     from models import Reservation, Destination

#     try:
#         verify_jwt_in_request(optional=True)
#         user_id = get_jwt_identity()
#     except Exception:
#         user_id = None

#     if not user_id:
#         return jsonify({'reservations': []}), 200

#     reservations = Reservation.query.filter_by(
#         user_id=user_id
#     ).order_by(Reservation.created_at.desc()).all()

#     result = []
#     for r in reservations:
#         result.append({
#             'id':          r.id,
#             'destination': r.destination.name if r.destination else 'N/A',
#             'country':     r.destination.country if r.destination else '',
#             'check_in':    r.check_in.strftime('%d/%m/%Y') if r.check_in else '',
#             'check_out':   r.check_out.strftime('%d/%m/%Y') if r.check_out else '',
#             'status':      r.status,
#             'amount':      r.total_amount,
#             'label':       f"Réservation #{r.id} — {r.destination.name if r.destination else 'N/A'} "
#                            f"({r.check_in.strftime('%d/%m/%Y') if r.check_in else ''} → "
#                            f"{r.check_out.strftime('%d/%m/%Y') if r.check_out else ''}) — {r.status}"
#         })

#     print(f"📋 {len(result)} réservation(s) retournées pour user {user_id}")
#     return jsonify({'reservations': result}), 200

from email import message
from flask import Blueprint, request, jsonify, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_cors import cross_origin
from extensions import db

# ✅ MODIFIÉ : on importe process_email_with_ai au lieu des 3 fonctions séparées
from services.ai_service import process_email_with_ai

from services.mail_service import send_confirmation_to_client, send_reply_to_client
import threading
from models import ContactMessage, User, InteractionLog, Reservation, Favorite
from datetime import datetime

contact_bp = Blueprint('contact', __name__)


# ─── Helpers DB ──────────────────────────────────────────────────────────────

def save_message(name, email, phone, subject, message,
                 contact_type=None, destination_id=None,
                 destination_name=None, reservation_id=None):
    msg = ContactMessage(
        name=name, email=email,
        phone=phone, subject=subject, message=message,
        contact_type=contact_type,
        destination_id=destination_id,
        destination_name=destination_name,
        reservation_id=reservation_id
    )
    db.session.add(msg)
    db.session.commit()
    print(f"💾 Message sauvegardé — ID: {msg.id} | Type: {contact_type} | "
          f"Destination: {destination_name or 'N/A'} | Réservation: {reservation_id or 'N/A'}")
    return msg.id


def update_ai_fields(message_id, category, priority, sentiment, ai_summary, suggested_reply):
    msg = ContactMessage.query.get(message_id)
    if msg:
        msg.category        = category
        msg.priority        = priority
        msg.sentiment       = sentiment
        msg.ai_summary      = ai_summary
        msg.suggested_reply = suggested_reply
        db.session.commit()
        print(f"💾 Champs IA mis à jour — ID: {message_id} | Cat: {category} | "
              f"Prio: {priority} | Sentiment: {sentiment}")


def mark_email_sent(message_id):
    msg = ContactMessage.query.get(message_id)
    if msg:
        msg.auto_email_sent = True
        db.session.commit()
        print(f"📧 Email marqué comme envoyé — ID: {message_id}")


# ─── Calcul score de priorité global ─────────────────────────────────────────

def compute_priority_score(msg):
    score   = 0
    details = {}

    category_points = {
        'urgent':        40,
        'reclamation':   30,
        'demande_devis': 20,
        'info':          10
    }
    cat_pts = category_points.get(msg.category, 10)
    score  += cat_pts
    details['categorie_pts'] = cat_pts

    user        = User.query.filter_by(email=msg.email).first()
    segment     = 'Inactif'
    churn_risk  = 'Faible'
    segment_pts = 5
    churn_pts   = 5

    if user:
        reservations = Reservation.query.filter_by(user_id=user.id).count()
        favorites    = Favorite.query.filter_by(user_id=user.id).count()
        views        = InteractionLog.query.filter_by(user_id=user.id, action='view').count()
        recent_score = reservations * 3 + favorites * 2 + views * 1

        if recent_score >= 5:   segment = 'VIP'
        elif recent_score >= 3: segment = 'Régulier'
        elif reservations > 0:  segment = 'Nouveau'
        else:                   segment = 'Inactif'

        last_interaction = InteractionLog.query.filter_by(
            user_id=user.id
        ).order_by(InteractionLog.created_at.desc()).first()

        if last_interaction:
            days_inactive = (datetime.utcnow() - last_interaction.created_at).days
            if days_inactive > 7:   churn_risk = 'Critique'
            elif days_inactive > 5: churn_risk = 'Élevé'
            elif days_inactive > 3: churn_risk = 'Moyen'
            else:                   churn_risk = 'Faible'
        else:
            churn_risk = 'Critique'

        segment_pts = {'VIP': 30, 'Régulier': 20, 'Nouveau': 10, 'Inactif': 5}.get(segment, 5)
        churn_pts   = {'Critique': 20, 'Élevé': 15, 'Moyen': 10, 'Faible': 5}.get(churn_risk, 5)

    score += segment_pts + churn_pts
    details['segment_pts'] = segment_pts
    details['churn_pts']   = churn_pts

    anciennete_pts = 0
    if msg.created_at:
        hours = (datetime.utcnow() - msg.created_at).total_seconds() / 3600
        if hours > 24:   anciennete_pts = 10
        elif hours > 12: anciennete_pts = 7
        elif hours > 6:  anciennete_pts = 4

    score += anciennete_pts
    details['anciennete_pts'] = anciennete_pts

    print(f"🎯 Score — ID:{msg.id} | Cat:{cat_pts} + Seg:{segment_pts} + "
          f"Churn:{churn_pts} + Anc:{anciennete_pts} = {score}pts | "
          f"Segment:{segment} | Churn:{churn_risk}")

    return {
        'priority_score': score,
        'client_segment': segment,
        'churn_risk':     churn_risk,
        'score_details':  details
    }


# ─── Thread IA — ✅ MODIFIÉ ──────────────────────────────────────────────────

def process_with_ai(app, message_id, name, email, subject, message,
                    destination_name=None, reservation_id=None, contact_type=None):
    with app.app_context():
        try:
            print(f"🤖 Démarrage IA pour message {message_id}...")

            # ✅ UN SEUL appel Gemini au lieu de 3 (évite l'erreur 429)
            from services.ai_service import process_email_with_ai
            from services.mail_service import send_confirmation_to_client
            message_enrichi = message
            if destination_name:
                message_enrichi = f"[Destination : {destination_name}]\n{message}"
            if reservation_id:
                message_enrichi = f"[Réservation #{reservation_id}]\n{message_enrichi}"

            # ── 1 seul appel Gemini pour tout ────────────────────
            print("🚀 Appel Gemini unique...")

            result = process_email_with_ai(name, subject, message_enrichi, contact_type)

            # Extraire les champs du résultat
            category          = result.get("category", "info")
            priority          = result.get("priority", "moyenne")
            sentiment         = result.get("sentiment", "neutre")
            ai_summary        = result.get("ai_summary", "")
            suggested_reply   = result.get("suggested_reply", "")
            confirmation_body = result.get("auto_confirmation", "")
            print(f"✅ IA terminée — cat:{category} | prio:{priority} | sentiment:{sentiment}")
            print(f"📝 Résumé : {ai_summary}")
            print(f"✍️  Réponse suggérée ({len(suggested_reply)} chars) : {suggested_reply[:80]}...")
            # Sauvegarde en DB
            update_ai_fields(message_id, category, priority, sentiment,
                             ai_summary, suggested_reply)

            # Envoi email de confirmation au client
            # sent = send_confirmation_to_client(email, name, subject, confirmation_body)
            # if sent:
            #     mark_email_sent(message_id)
            #     print(f"✅ Email envoyé à {email}")

            if confirmation_body:
                sent = send_confirmation_to_client(
                    email, name, subject, confirmation_body
                )
                if sent:
                    mark_email_sent(message_id)
                    print(f"✅ Email confirmation envoyé à {email}")
                else:
                    print(f"❌ Email non envoyé à {email}")

        except Exception as e:
            import traceback
            print(f"❌ ERREUR THREAD IA: {e}")
            print(traceback.format_exc())


# ─── Routes ──────────────────────────────────────────────────────────────────

@contact_bp.route('/api/contact', methods=['POST'])
@cross_origin()
def submit_contact():
    data             = request.get_json()
    name             = data.get('name', '').strip()
    email            = data.get('email', '').strip()
    phone            = data.get('phone', '').strip()
    subject          = data.get('subject', '').strip()
    message          = data.get('message', '').strip()
    contact_type     = data.get('contact_type', 'general')
    destination_id   = data.get('destination_id')
    destination_name = data.get('destination_name', '').strip()
    reservation_id   = data.get('reservation_id')

    if not name or not email or not message:
        return jsonify({'error': 'Champs requis manquants'}), 400
     # ── Récupérer le segment du client ───────────────────────
    segment    = 'Inconnu'
    churn_risk = None
    user = User.query.filter_by(email=email).first()
    if user:
        reservations = Reservation.query.filter_by(user_id=user.id).count()
        favorites    = Favorite.query.filter_by(user_id=user.id).count()
        views        = InteractionLog.query.filter_by(
                           user_id=user.id, action='view').count()
        score = reservations * 3 + favorites * 2 + views * 1
        if score >= 5:    segment = 'VIP'
        elif score >= 3:  segment = 'Régulier'
        elif reservations > 0: segment = 'Nouveau'
        else:             segment = 'Inactif'

        last = InteractionLog.query.filter_by(
            user_id=user.id
        ).order_by(InteractionLog.created_at.desc()).first()
        if last:
            days = (datetime.utcnow() - last.created_at).days
            if days > 7:   churn_risk = 'Critique'
            elif days > 5: churn_risk = 'Élevé'
            elif days > 3: churn_risk = 'Moyen'
            else:          churn_risk = 'Faible'

    print(f"👤 Client: {name} | Segment: {segment} | Churn: {churn_risk}")
    if not subject:
        if contact_type == 'reservation' and reservation_id:
            subject = f"Concernant ma réservation #{reservation_id}"
        elif contact_type == 'destination' and destination_name:
            subject = f"Question sur {destination_name}"
        else:
            subject = "Question générale"

    message_id = save_message(
        name, email, phone, subject, message,
        contact_type, destination_id, destination_name, reservation_id
    )

    app    = current_app._get_current_object()
    thread = threading.Thread(
        target=process_with_ai,
        args=(app, message_id, name, email, subject,
              message, destination_name, reservation_id, contact_type)
    )
    thread.daemon = True
    thread.start()

    return jsonify({'success': True, 'message': 'Message envoyé avec succès'}), 201


@contact_bp.route('/api/admin/notifications/count', methods=['GET'])
@cross_origin()
def unread_count():
    count = ContactMessage.query.filter_by(is_read=False).count()
    return jsonify({'unread_count': count})


@contact_bp.route('/api/admin/messages', methods=['GET'])
@cross_origin()
def list_messages():
    msgs = ContactMessage.query.order_by(ContactMessage.created_at.desc()).all()
    return jsonify({'messages': [m.to_dict() for m in msgs]})


@contact_bp.route('/api/admin/messages/prioritized', methods=['GET'])
@cross_origin()
def get_prioritized_messages():
    msgs   = ContactMessage.query.filter_by(is_read=False).all()
    result = []
    for msg in msgs:
        data     = msg.to_dict()
        priority = compute_priority_score(msg)
        data.update(priority)
        result.append(data)

    result.sort(key=lambda x: x['priority_score'], reverse=True)
    return jsonify({'messages': result}), 200


@contact_bp.route('/api/admin/messages/<int:message_id>', methods=['GET'])
@cross_origin()
def get_message(message_id):
    msg         = ContactMessage.query.get_or_404(message_id)
    msg.is_read = True
    db.session.commit()
    return jsonify({'message': msg.to_dict()})


@contact_bp.route('/api/admin/messages/<int:message_id>/reply', methods=['POST'])
@cross_origin()
def send_reply(message_id):
    msg        = ContactMessage.query.get_or_404(message_id)
    data       = request.get_json()
    reply_body = data.get('reply', '').strip()

    sent = send_reply_to_client(msg.email, msg.name, msg.subject, reply_body)
    if sent:
        return jsonify({'success': True, 'message': 'Réponse envoyée au client'})
    return jsonify({'error': 'Échec envoi email'}), 500


@contact_bp.route('/api/admin/messages/<int:message_id>/read', methods=['PUT'])
@cross_origin()
def mark_read(message_id):
    msg         = ContactMessage.query.get_or_404(message_id)
    msg.is_read = True
    db.session.commit()
    return jsonify({'success': True})


@contact_bp.route('/api/admin/messages/read-all', methods=['PUT'])
@cross_origin()
def read_all():
    ContactMessage.query.update({'is_read': True})
    db.session.commit()
    return jsonify({'success': True})


@contact_bp.route('/api/contact/my-reservations', methods=['GET'])
@cross_origin()
def get_client_reservations():
    from flask_jwt_extended import verify_jwt_in_request, get_jwt_identity
    from models import Reservation, Destination

    try:
        verify_jwt_in_request(optional=True)
        user_id = get_jwt_identity()
    except Exception:
        user_id = None

    if not user_id:
        return jsonify({'reservations': []}), 200

    reservations = Reservation.query.filter_by(
        user_id=user_id
    ).order_by(Reservation.created_at.desc()).all()

    result = []
    for r in reservations:
        result.append({
            'id':          r.id,
            'destination': r.destination.name if r.destination else 'N/A',
            'country':     r.destination.country if r.destination else '',
            'check_in':    r.check_in.strftime('%d/%m/%Y') if r.check_in else '',
            'check_out':   r.check_out.strftime('%d/%m/%Y') if r.check_out else '',
            'status':      r.status,
            'amount':      r.total_amount,
            'label':       f"Réservation #{r.id} — {r.destination.name if r.destination else 'N/A'} "
                           f"({r.check_in.strftime('%d/%m/%Y') if r.check_in else ''} → "
                           f"{r.check_out.strftime('%d/%m/%Y') if r.check_out else ''}) — {r.status}"
        })

    return jsonify({'reservations': result}), 200


from flask_mail import Message
from flask import render_template, current_app
from extensions import mail

def send_automated_email(recipient, subject, template, **kwargs):
    """
    Fonction universelle pour envoyer un mail.
    kwargs permet de passer des objets comme 'user' ou 'reservation' au template HTML.
    """
    print(f"DEBUG MAIL: Tentative avec {current_app.config['MAIL_SERVER']}:{current_app.config['MAIL_PORT']}")
    
    msg = Message(
        subject,
        recipients=[recipient],
        sender=current_app.config.get('MAIL_DEFAULT_SENDER', current_app.config.get('MAIL_USERNAME'))
    )
    msg.html = render_template(f"{template}.html", **kwargs)
    mail.send(msg)
    print(f"DEBUG MAIL: ✅ Mail envoyé à {recipient}")

from flask_mail import Message as MailMessage

AGENCY_NAME = "Votre Agence de Voyage"
AGENCY_EMAIL = "pfa8144@gmail.com"


def _send(to_email, to_name, subject, body):
    """Fonction interne d'envoi"""
    try:
        html_body = f"""
        <html>
        <body style="font-family:Arial,sans-serif;color:#333;max-width:600px;margin:auto;padding:20px;">
            <div style="background:#1a3c5e;padding:20px;border-radius:8px 8px 0 0;">
                <h2 style="color:white;margin:0;">✈️ {AGENCY_NAME}</h2>
            </div>
            <div style="background:#f9f9f9;padding:30px;border-radius:0 0 8px 8px;line-height:1.6;">
                {body.replace(chr(10), '<br>')}
            </div>
            <p style="color:#999;font-size:12px;text-align:center;margin-top:20px;">
                Cet email a été envoyé automatiquement depuis {AGENCY_NAME}.
            </p>
        </body>
        </html>
        """

        msg = MailMessage(
            subject=subject,
            recipients=[to_email],
            body=body,
            html=html_body
        )
        mail.send(msg)
        return True

    except Exception as e:
        print(f"❌ Erreur envoi email à {to_email}: {e}")
        return False


def send_confirmation_to_client(client_email, client_name, subject, body):
    """Email de confirmation automatique au client après soumission du formulaire"""
    return _send(
        to_email=client_email,
        to_name=client_name,
        subject=f"✅ Confirmation de réception – {subject or 'Votre demande'}",
        body=body
    )


def send_reply_to_client(client_email, client_name, subject, body):
    """Email de réponse de l'admin au client"""
    return _send(
        to_email=client_email,
        to_name=client_name,
        subject=f"Re: {subject or 'Votre demande'}",
        body=body
    )
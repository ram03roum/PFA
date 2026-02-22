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
        sender=current_app.config['MAIL_USERNAME']
    )
    msg.html = render_template(f"{template}.html", **kwargs)
    mail.send(msg)
    print(f"DEBUG MAIL: ✅ Mail envoyé à {recipient}")
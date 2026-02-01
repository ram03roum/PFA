package com.bacoge.constructionmaterial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bacoge.constructionmaterial.config.properties.MailProperties;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private MailProperties mailProperties;
    
    public void sendWelcomeEmail(String toEmail, String firstName, String lastName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Bienvenue chez B&Acoge !");
            message.setText(buildWelcomeMessage(firstName, lastName));
            message.setFrom(mailProperties.getFrom());
            
            mailSender.send(message);
            logger.info("Email de bienvenue envoyé à: {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de bienvenue à {}: {}", toEmail, e.getMessage());
        }
    }
    
    public void sendProfileUpdateNotification(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Profil mis à jour - B&Acoge");
            message.setText(buildProfileUpdateMessage(firstName));
            message.setFrom(mailProperties.getFrom());
            
            mailSender.send(message);
            logger.info("Email de mise à jour de profil envoyé à: {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de mise à jour de profil à {}: {}", toEmail, e.getMessage());
        }
    }
    
    public void sendPasswordChangeNotification(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mot de passe modifié - B&Acoge");
            message.setText(buildPasswordChangeMessage(firstName));
            message.setFrom(mailProperties.getFrom());
            
            mailSender.send(message);
            logger.info("Email de changement de mot de passe envoyé à: {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de changement de mot de passe à {}: {}", toEmail, e.getMessage());
        }
    }
    
    public void sendAccountDeactivationNotification(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Compte désactivé - B&Acoge");
            message.setText(buildAccountDeactivationMessage(firstName));
            message.setFrom(mailProperties.getFrom());
            
            mailSender.send(message);
            logger.info("Email de désactivation de compte envoyé à: {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de désactivation à {}: {}", toEmail, e.getMessage());
        }
    }
    
    public void sendAccountDeletionConfirmation(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Compte supprimé - B&Acoge");
            message.setText(buildAccountDeletionMessage(firstName));
            message.setFrom(mailProperties.getFrom());
            
            mailSender.send(message);
            logger.info("Email de confirmation de suppression envoyé à: {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de confirmation de suppression à {}: {}", toEmail, e.getMessage());
        }
    }
    
    public void send2FAActivationNotification(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Authentification à deux facteurs activée - B&Acoge");
            message.setText(build2FAActivationMessage(firstName));
            message.setFrom(mailProperties.getFrom());
            
            mailSender.send(message);
            logger.info("Email de notification 2FA envoyé à: {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de notification 2FA à {}: {}", toEmail, e.getMessage());
        }
    }
    
    private String buildWelcomeMessage(String firstName, String lastName) {
        return String.format(
            "Bonjour %s %s,\n\n" +
            "Bienvenue chez B&Acoge !\n\n" +
            "Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter et découvrir notre large gamme de matériaux de construction.\n\n" +
            "Nous sommes ravis de vous compter parmi nos clients et nous nous engageons à vous offrir les meilleurs produits et services.\n\n" +
            "N'hésitez pas à nous contacter si vous avez des questions.\n\n" +
            "Cordialement,\n" +
            "L'équipe B&Acoge\n\n" +
            "---\n" +
            "Ceci est un message automatique, merci de ne pas y répondre.",
            firstName, lastName
        );
    }
    
    private String buildProfileUpdateMessage(String firstName) {
        return String.format(
            "Bonjour %s,\n\n" +
            "Votre profil B&Acoge a été mis à jour avec succès.\n\n" +
            "Si vous n'êtes pas à l'origine de cette modification, veuillez nous contacter immédiatement.\n\n" +
            "Cordialement,\n" +
            "L'équipe B&Acoge\n\n" +
            "---\n" +
            "Ceci est un message automatique, merci de ne pas y répondre.",
            firstName
        );
    }
    
    private String buildPasswordChangeMessage(String firstName) {
        return String.format(
            "Bonjour %s,\n\n" +
            "Votre mot de passe B&Acoge a été modifié avec succès.\n\n" +
            "Si vous n'êtes pas à l'origine de cette modification, veuillez nous contacter immédiatement pour sécuriser votre compte.\n\n" +
            "Cordialement,\n" +
            "L'équipe B&Acoge\n\n" +
            "---\n" +
            "Ceci est un message automatique, merci de ne pas y répondre.",
            firstName
        );
    }
    
    private String buildAccountDeactivationMessage(String firstName) {
        return String.format(
            "Bonjour %s,\n\n" +
            "Votre compte B&Acoge a été désactivé comme demandé.\n\n" +
            "Vous pouvez réactiver votre compte à tout moment en nous contactant.\n\n" +
            "Nous espérons vous revoir bientôt !\n\n" +
            "Cordialement,\n" +
            "L'équipe B&Acoge\n\n" +
            "---\n" +
            "Ceci est un message automatique, merci de ne pas y répondre.",
            firstName
        );
    }
    
    private String buildAccountDeletionMessage(String firstName) {
        return String.format(
            "Bonjour %s,\n\n" +
            "Votre compte B&Acoge a été supprimé définitivement comme demandé.\n\n" +
            "Toutes vos données ont été effacées de nos systèmes.\n\n" +
            "Nous vous remercions d'avoir fait confiance à B&Acoge.\n\n" +
            "Cordialement,\n" +
            "L'équipe B&Acoge\n\n" +
            "---\n" +
            "Ceci est un message automatique, merci de ne pas y répondre.",
            firstName
        );
    }
    
    private String build2FAActivationMessage(String firstName) {
        return String.format(
            "Bonjour %s,\n\n" +
            "L'authentification à deux facteurs a été activée sur votre compte B&Acoge.\n\n" +
            "Cette mesure de sécurité supplémentaire protège votre compte contre les accès non autorisés.\n\n" +
            "Si vous n'êtes pas à l'origine de cette activation, veuillez nous contacter immédiatement.\n\n" +
            "Cordialement,\n" +
            "L'équipe B&Acoge\n\n" +
            "---\n" +
            "Ceci est un message automatique, merci de ne pas y répondre.",
            firstName
        );
    }
    
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(mailProperties.getFrom());
            
            mailSender.send(message);
            logger.info("Email envoyé à: {} avec le sujet: {}", toEmail, subject);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email à {}: {}", toEmail, e.getMessage());
        }
    }
}
package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.ContactMessageDto;
import com.bacoge.constructionmaterial.service.ContactService;
import com.bacoge.constructionmaterial.service.AdminNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour la gestion des formulaires de contact
 */
@Controller
public class ContactController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    
    @Autowired
    private ContactService contactService;
    
    @Autowired
    private AdminNotificationService adminNotificationService;
    
    /**
     * Traite la soumission du formulaire de contact
     */
    @PostMapping("/contact/submit")
    public String submitContactForm(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "privacy", required = false) String privacy,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validation de base
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom est obligatoire");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("L'email est obligatoire");
            }
            if (subject == null || subject.trim().isEmpty()) {
                throw new IllegalArgumentException("Le sujet est obligatoire");
            }
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Le message est obligatoire");
            }
            if (privacy == null) {
                throw new IllegalArgumentException("Vous devez accepter la politique de confidentialité");
            }
            
            // Créer le DTO du message de contact
            ContactMessageDto contactMessage = ContactMessageDto.builder()
                    .name(name.trim())
                    .email(email.trim())
                    .phone(phone != null ? phone.trim() : null)
                    .subject(subject.trim())
                    .message(message.trim())
                    .build();
            
            // Ajouter les informations d'authentification si l'utilisateur est connecté
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                contactMessage.setAuthenticated(true);
                contactMessage.setUserEmail(authentication.getName());
                logger.info("Message de contact soumis par un utilisateur authentifié: {}", authentication.getName());
            } else {
                contactMessage.setAuthenticated(false);
                logger.info("Message de contact soumis par un utilisateur invité");
            }
            
            // Soumettre le message via le service
            Map<String, Object> savedMessageData = contactService.submitContactMessage(convertToMap(contactMessage));
            ContactMessageDto savedMessage = convertFromMap(savedMessageData);
            
            // Notifier les administrateurs en temps réel (WebSocket)
            try {
                adminNotificationService.createContactMessageNotification(
                        savedMessage.getName(),
                        savedMessage.getEmail(),
                        savedMessage.getSubject(),
                        savedMessage.getMessage(),
                        savedMessage.getId()
                );
            } catch (Exception notifyEx) {
                logger.warn("Impossible d'envoyer la notification admin pour le message de contact {}: {}",
                        savedMessage.getId(), notifyEx.getMessage());
            }
            
            // Ajouter un message de succès
            redirectAttributes.addFlashAttribute("messageSent", true);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Votre message a été envoyé avec succès ! Nous vous répondrons dans les plus brefs délais.");
            
            logger.info("Message de contact traité avec succès - ID: {}", savedMessage.getId());
            
            return "redirect:/contact";
            
        } catch (IllegalArgumentException e) {
            // Erreurs de validation
            logger.warn("Erreur de validation du formulaire de contact: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("formData", createFormData(name, email, phone, subject, message));
            return "redirect:/contact";
            
        } catch (Exception e) {
            // Erreurs système
            logger.error("Erreur lors du traitement du formulaire de contact: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Une erreur est survenue lors de l'envoi de votre message. Veuillez réessayer.");
            redirectAttributes.addFlashAttribute("formData", createFormData(name, email, phone, subject, message));
            return "redirect:/contact";
        }
    }
    
    /**
     * Crée un objet contenant les données du formulaire pour les conserver en cas d'erreur
     */
    private FormData createFormData(String name, String email, String phone, String subject, String message) {
        return new FormData(name, email, phone, subject, message);
    }
    
    /**
     * Classe interne pour conserver les données du formulaire
     */
    public static class FormData {
        private final String name;
        private final String email;
        private final String phone;
        private final String subject;
        private final String message;
        
        public FormData(String name, String email, String phone, String subject, String message) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.subject = subject;
            this.message = message;
        }
        
        // Getters
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
    }
    
    // Helper methods for DTO/Map conversion
    private Map<String, Object> convertToMap(ContactMessageDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", dto.getId());
        map.put("name", dto.getName());
        map.put("email", dto.getEmail());
        map.put("subject", dto.getSubject());
        map.put("message", dto.getMessage());
        map.put("status", dto.getStatus());
        map.put("authenticated", dto.isAuthenticated());
        map.put("userEmail", dto.getUserEmail());
        return map;
    }
    
    private ContactMessageDto convertFromMap(Map<String, Object> map) {
        ContactMessageDto dto = new ContactMessageDto();
        dto.setId((Long) map.get("id"));
        dto.setName((String) map.get("name"));
        dto.setEmail((String) map.get("email"));
        dto.setSubject((String) map.get("subject"));
        dto.setMessage((String) map.get("message"));
        dto.setStatus((String) map.get("status"));
        dto.setAuthenticated((Boolean) map.getOrDefault("authenticated", false));
        dto.setUserEmail((String) map.get("userEmail"));
        return dto;
    }
}

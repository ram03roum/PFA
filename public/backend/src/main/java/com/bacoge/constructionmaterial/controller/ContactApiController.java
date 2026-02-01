package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.ContactMessageDto;
import com.bacoge.constructionmaterial.service.ContactService;
import com.bacoge.constructionmaterial.service.AdminNotificationService;
import com.bacoge.constructionmaterial.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des messages de contact
 */
@RestController
@RequestMapping("/api/contact")

@Tag(name = "Contact", description = "API pour la gestion des messages de contact")
public class ContactApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactApiController.class);
    
    @Autowired
    private ContactService contactService;
    
    @Autowired
    private AdminNotificationService adminNotificationService;
    
    @Autowired
    private ConversationService conversationService;
    
    /**
     * Soumet un nouveau message de contact via API
     */
    @PostMapping("/submit")
    @Operation(summary = "Soumettre un message de contact", description = "Permet de soumettre un nouveau message de contact")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Message soumis avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur")
    })
    public ResponseEntity<Map<String, Object>> submitContactMessage(
            @RequestBody ContactMessageDto contactMessage,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validation de base
            if (contactMessage.getName() == null || contactMessage.getName().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Le nom est obligatoire");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (contactMessage.getEmail() == null || contactMessage.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "L'email est obligatoire");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (contactMessage.getSubject() == null || contactMessage.getSubject().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Le sujet est obligatoire");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (contactMessage.getMessage() == null || contactMessage.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Le message est obligatoire");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Ajouter les informations d'authentification
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                contactMessage.setAuthenticated(true);
                contactMessage.setUserEmail(authentication.getName());
            } else {
                contactMessage.setAuthenticated(false);
            }
            
            // Soumettre le message
            Map<String, Object> savedMessageData = contactService.submitContactMessage(convertToMap(contactMessage));
            ContactMessageDto savedMessage = convertFromMap(savedMessageData);
            
            response.put("success", true);
            response.put("message", "Votre message a été envoyé avec succès !");
            response.put("messageId", savedMessage.getId());
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("id", savedMessage.getId());
            messageData.put("name", savedMessage.getName());
            messageData.put("email", savedMessage.getEmail());
            messageData.put("subject", savedMessage.getSubject());
            messageData.put("message", savedMessage.getMessage());
            messageData.put("status", savedMessage.getStatus());
            messageData.put("createdAt", savedMessage.getCreatedAt());
            response.put("data", messageData);
            
            logger.info("Message de contact soumis via API - ID: {}", savedMessage.getId());
            // Broadcast admin notification in realtime
            try {
                adminNotificationService.createContactMessageNotification(
                    savedMessage.getName(), 
                    savedMessage.getEmail(), 
                    savedMessage.getSubject(), 
                    savedMessage.getMessage(), 
                    savedMessage.getId()
                );
            } catch (Exception ignored) {}
            
            // Créer une conversation persistée pour permettre aux admins de répondre
            try {
                String emailForLink = savedMessage.getUserEmail() != null ? savedMessage.getUserEmail() : savedMessage.getEmail();
                conversationService.createConversationFromContact(
                    savedMessage.getSubject(),
                    (savedMessage.getMessage() != null ? savedMessage.getMessage() : "") +
                    (savedMessage.getName() != null || savedMessage.getEmail() != null ?
                        ("\n\n[Envoyé par: " + (savedMessage.getName() != null ? savedMessage.getName() : "") +
                         (savedMessage.getEmail() != null ? " <" + savedMessage.getEmail() + ">" : "") + "]") : ""),
                    emailForLink
                );
            } catch (Exception ignored) {}
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la soumission du message de contact via API: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Une erreur est survenue lors de l'envoi de votre message");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Récupère tous les messages de contact (pour l'administration)
     */
    @GetMapping("/messages")
    @Operation(summary = "Récupérer tous les messages", description = "Récupère la liste de tous les messages de contact")
    public ResponseEntity<Map<String, Object>> getAllMessages() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> messagesData = contactService.getAllContactMessages();
            List<ContactMessageDto> messages = messagesData.stream()
                .map(this::convertFromMap)
                .collect(java.util.stream.Collectors.toList());
            List<Map<String, Object>> messageData = new java.util.ArrayList<>();
            for (ContactMessageDto msg : messages) {
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("id", msg.getId());
                msgMap.put("name", msg.getName());
                msgMap.put("email", msg.getEmail());
                msgMap.put("subject", msg.getSubject());
                msgMap.put("message", msg.getMessage());
                msgMap.put("status", msg.getStatus());
                msgMap.put("createdAt", msg.getCreatedAt());
                messageData.add(msgMap);
            }
            
            response.put("success", true);
            response.put("data", messageData);
            response.put("total", messages.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des messages de contact: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des messages");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Récupère un message de contact par ID
     */
    @GetMapping("/messages/{id}")
    @Operation(summary = "Récupérer un message par ID", description = "Récupère un message de contact spécifique")
    public ResponseEntity<Map<String, Object>> getMessageById(
            @Parameter(description = "ID du message") @PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> messageData = contactService.getContactMessageById(id);
            ContactMessageDto message = convertFromMap(messageData);
            
            response.put("success", true);
            response.put("data", message);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du message {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération du message");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Met à jour le statut d'un message
     */
    @PutMapping("/messages/{id}/status")
    @Operation(summary = "Mettre à jour le statut", description = "Met à jour le statut d'un message de contact")
    public ResponseEntity<Map<String, Object>> updateMessageStatus(
            @Parameter(description = "ID du message") @PathVariable Long id,
            @Parameter(description = "Nouveau statut") @RequestParam String status) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> updatedMessageData = contactService.updateMessageStatus(id, status);
            ContactMessageDto updatedMessage = convertFromMap(updatedMessageData);
            
            response.put("success", true);
            response.put("message", "Statut mis à jour avec succès");
            response.put("data", updatedMessage);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du statut du message {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erreur lors de la mise à jour du statut");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Récupère les messages par statut
     */
    @GetMapping("/messages/status/{status}")
    @Operation(summary = "Récupérer les messages par statut", description = "Récupère les messages ayant un statut spécifique")
    public ResponseEntity<Map<String, Object>> getMessagesByStatus(
            @Parameter(description = "Statut des messages") @PathVariable String status) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> messagesData = contactService.getMessagesByStatus(status);
            List<ContactMessageDto> messages = messagesData.stream()
                .map(this::convertFromMap)
                .collect(java.util.stream.Collectors.toList());
            List<Map<String, Object>> messageData = new java.util.ArrayList<>();
            for (ContactMessageDto msg : messages) {
                Map<String, Object> msgMap = new HashMap<>();
                msgMap.put("id", msg.getId());
                msgMap.put("name", msg.getName());
                msgMap.put("email", msg.getEmail());
                msgMap.put("subject", msg.getSubject());
                msgMap.put("message", msg.getMessage());
                msgMap.put("status", msg.getStatus());
                msgMap.put("createdAt", msg.getCreatedAt());
                messageData.add(msgMap);
            }
            
            response.put("success", true);
            response.put("data", messageData);
            response.put("total", messages.size());
            response.put("status", status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des messages avec le statut {}: {}", status, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des messages");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Récupère les statistiques des messages de contact
     */
    @GetMapping("/stats")
    @Operation(summary = "Statistiques des contacts", description = "Récupère les statistiques des messages de contact")
    public ResponseEntity<Map<String, Object>> getContactStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = contactService.getContactStats();
            
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des statistiques de contact: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Erreur lors de la récupération des statistiques");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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

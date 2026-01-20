package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.model.Notification;
import com.bacoge.constructionmaterial.dto.NotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service pour l'envoi de notifications en temps réel via WebSocket
 */
@Service
public class WebSocketNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Envoie une notification à tous les administrateurs connectés
     * @param notification La notification à envoyer
     */
    public void sendNotificationToAdmins(Notification notification) {
        try {
            Map<String, Object> message = createNotificationMessage(notification);
            
            // Envoie à tous les administrateurs via le topic
            messagingTemplate.convertAndSend("/topic/admin/notifications", message);
            
            logger.info("Notification envoyée aux admins: {} - {}", 
                       notification.getType(), notification.getTitle());
                       
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification WebSocket: ", e);
        }
    }

    /**
     * Envoie une notification à tous les administrateurs connectés (via DTO)
     * @param dto Le DTO de notification à envoyer
     */
    public void sendNotificationToAdmins(NotificationDto dto) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("id", dto.getId());
            message.put("type", dto.getType());
            message.put("title", dto.getTitle());
            message.put("message", dto.getMessage());
            message.put("priority", dto.getPriority());
            message.put("read", dto.getIsRead());
            message.put("createdAt", dto.getCreatedAt());
            message.put("timestamp", System.currentTimeMillis());
            if (dto.getRelatedEntityId() != null && dto.getRelatedEntityType() != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("entityId", dto.getRelatedEntityId());
                metadata.put("entityType", dto.getRelatedEntityType());
                message.put("metadata", metadata);
            }
            messagingTemplate.convertAndSend("/topic/admin/notifications", message);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification WebSocket (DTO): ", e);
        }
    }

    /**
     * Envoie une notification à un administrateur spécifique
     * @param adminId ID de l'administrateur
     * @param notification La notification à envoyer
     */
    public void sendNotificationToAdmin(Long adminId, Notification notification) {
        try {
            Map<String, Object> message = createNotificationMessage(notification);
            
            // Envoie à un administrateur spécifique
            messagingTemplate.convertAndSendToUser(
                adminId.toString(), 
                "/queue/notifications", 
                message
            );
            
            logger.info("Notification envoyée à l'admin {}: {} - {}", 
                       adminId, notification.getType(), notification.getTitle());
                       
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification WebSocket à l'admin {}: ", adminId, e);
        }
    }

    /**
     * Envoie une mise à jour du compteur de notifications
     * @param unreadCount Nombre de notifications non lues
     */
    public void sendNotificationCountUpdate(int unreadCount) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "NOTIFICATION_COUNT_UPDATE");
            message.put("unreadCount", unreadCount);
            message.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/admin/notification-count", message);
            
            logger.debug("Mise à jour du compteur de notifications envoyée: {}", unreadCount);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la mise à jour du compteur: ", e);
        }
    }

    /**
     * Envoie un événement de réinitialisation du badge
     */
    public void sendBadgeResetEvent() {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "BADGE_RESET");
            message.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/admin/badge-reset", message);
            
            logger.debug("Événement de réinitialisation du badge envoyé");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'événement de réinitialisation du badge: ", e);
        }
    }

    /**
     * Envoie une notification en temps réel à un client spécifique (par ID utilisateur)
     * Canal de diffusion: /topic/client/{userId}/notifications
     */
    public void sendNotificationToClient(Long userId, Notification notification) {
        if (userId == null || notification == null) return;
        try {
            Map<String, Object> message = createNotificationMessage(notification);
            messagingTemplate.convertAndSend("/topic/client/" + userId + "/notifications", message);
            logger.info("Notification envoyée au client {}: {} - {}", userId, notification.getType(), notification.getTitle());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification au client {}: ", userId, e);
        }
    }

    /**
     * Envoie une notification (DTO) en temps réel à un client spécifique (par ID utilisateur)
     */
    public void sendNotificationToClient(Long userId, NotificationDto dto) {
        if (userId == null || dto == null) return;
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("id", dto.getId());
            message.put("type", dto.getType());
            message.put("title", dto.getTitle());
            message.put("message", dto.getMessage());
            message.put("priority", dto.getPriority());
            message.put("read", dto.getIsRead());
            message.put("createdAt", dto.getCreatedAt());
            message.put("timestamp", System.currentTimeMillis());
            if (dto.getRelatedEntityId() != null && dto.getRelatedEntityType() != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("entityId", dto.getRelatedEntityId());
                metadata.put("entityType", dto.getRelatedEntityType());
                message.put("metadata", metadata);
            }
            messagingTemplate.convertAndSend("/topic/client/" + userId + "/notifications", message);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification (DTO) au client {}: ", userId, e);
        }
    }

    /**
     * Envoie une mise à jour du compteur de notifications pour un client spécifique
     * Canal: /topic/client/{userId}/notification-count
     */
    public void sendUserNotificationCount(Long userId, long unreadCount) {
        if (userId == null) return;
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "USER_NOTIFICATION_COUNT_UPDATE");
            message.put("unreadCount", unreadCount);
            message.put("timestamp", System.currentTimeMillis());
            messagingTemplate.convertAndSend("/topic/client/" + userId + "/notification-count", message);
            logger.debug("Mise à jour du compteur client envoyée (userId={}, count={})", userId, unreadCount);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la mise à jour du compteur client {}: ", userId, e);
        }
    }

    /**
     * Crée un message de notification formaté pour WebSocket
     * @param notification La notification source
     * @return Map contenant les données de la notification
     */
    private Map<String, Object> createNotificationMessage(Notification notification) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", notification.getId());
        message.put("type", notification.getType().name());
        message.put("title", notification.getTitle());
        message.put("message", notification.getMessage());
        message.put("priority", notification.getPriority().name());
        message.put("read", notification.getIsRead());
        message.put("createdAt", notification.getCreatedAt());
        message.put("timestamp", System.currentTimeMillis());
        
        // Ajouter des métadonnées basées sur l'entité liée
        if (notification.getRelatedEntityId() != null && notification.getRelatedEntityType() != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("entityId", notification.getRelatedEntityId());
            metadata.put("entityType", notification.getRelatedEntityType());
            message.put("metadata", metadata);
        }
        
        return message;
    }
}
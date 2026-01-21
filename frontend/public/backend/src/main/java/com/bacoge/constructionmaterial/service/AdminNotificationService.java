package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.NotificationDto;
import com.bacoge.constructionmaterial.model.*;
import com.bacoge.constructionmaterial.repository.NotificationRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminNotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;
    
    // Créer une notification pour un achat de produit
    public NotificationDto createOrderNotification(Order order) {
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.ORDER_CREATED);
            notification.setTitle("Nouvelle commande");
            notification.setMessage(String.format("Nouvelle commande #%s de %s %s pour %.2f€", 
                    order.getId(),
                    order.getUser().getFirstName(),
                    order.getUser().getLastName(),
                    order.getTotalAmount()));
            notification.setRelatedEntityId(order.getId());
            notification.setRelatedEntityType("ORDER");
            notification.setPriority(Notification.NotificationPriority.HIGH);
            notification.setActionUrl("/admin/orders/" + order.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));
            
            Notification saved = notificationRepository.save(notification);
            logger.info("Notification de commande créée: {}", saved.getId());
            // Broadcast realtime to admins
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification de commande", e);
            throw new RuntimeException("Erreur lors de la création de la notification", e);
        }
    }

    // Créer une notification pour un nouvel avis client
    public NotificationDto createNewReviewNotification(Long productId, Long userId, Integer rating, String comment) {
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.NEW_REVIEW);
            notification.setTitle("Nouvel avis client");
            String safeComment = comment != null ? (comment.length() > 80 ? comment.substring(0, 77) + "..." : comment) : "";
            notification.setMessage(String.format("Avis %d/5 pour le produit #%d%s", 
                    rating != null ? rating : 0, 
                    productId != null ? productId : 0,
                    safeComment.isEmpty() ? "" : " — \"" + safeComment + "\""));
            notification.setRelatedEntityId(productId);
            notification.setRelatedEntityType("PRODUCT");
            notification.setPriority(Notification.NotificationPriority.LOW);
            notification.setActionUrl(productId != null ? "/admin/products/" + productId : "/admin/reviews");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));

            Notification saved = notificationRepository.save(notification);
            logger.info("Notification d'avis créée: {}", saved.getId());
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification d'avis", e);
            throw new RuntimeException("Erreur lors de la création de la notification d'avis", e);
        }
    }

    // Créer une notification pour changement de statut de commande
    public NotificationDto createOrderStatusChangedNotification(Order order, Order.OrderStatus newStatus) {
        try {
            Notification notification = new Notification();
            // Use existing enum to avoid compile issues (icons and UI still show proper title)
            notification.setType(Notification.NotificationType.ORDER_CREATED);
            notification.setTitle("Statut de commande modifié");
            notification.setMessage(String.format("Commande #%s maintenant %s", 
                    order.getOrderNumber() != null ? order.getOrderNumber() : order.getId(),
                    newStatus != null ? newStatus.name() : (order.getStatus() != null ? order.getStatus().name() : "")));
            notification.setRelatedEntityId(order.getId());
            notification.setRelatedEntityType("ORDER");
            notification.setPriority(Notification.NotificationPriority.MEDIUM);
            notification.setActionUrl("/admin/orders/" + order.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));

            Notification saved = notificationRepository.save(notification);
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification de changement de statut", e);
            throw new RuntimeException("Erreur lors de la création de la notification de statut", e);
        }
    }
    
    // Créer une notification pour un nouvel utilisateur
    public NotificationDto createUserRegistrationNotification(User user) {
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.USER_REGISTERED);
            notification.setTitle("Nouvel utilisateur");
            notification.setMessage(String.format("Nouvel utilisateur inscrit: %s %s (%s)", 
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()));
            notification.setRelatedEntityId(user.getId());
            notification.setRelatedEntityType("USER");
            notification.setPriority(Notification.NotificationPriority.MEDIUM);
            notification.setActionUrl("/admin/users/" + user.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));
            
            Notification saved = notificationRepository.save(notification);
            logger.info("Notification d'inscription créée: {}", saved.getId());
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification d'inscription", e);
            throw new RuntimeException("Erreur lors de la création de la notification", e);
        }
    }
    
    // Créer une notification pour suppression de compte
    public NotificationDto createUserDeletionNotification(User user) {
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.USER_DELETED);
            notification.setTitle("Utilisateur supprimé");
            notification.setMessage(String.format("Utilisateur supprimé: %s %s (%s)", 
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()));
            notification.setRelatedEntityId(user.getId());
            notification.setRelatedEntityType("USER");
            notification.setPriority(Notification.NotificationPriority.MEDIUM);
            notification.setActionUrl("/admin/users");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));
            
            Notification saved = notificationRepository.save(notification);
            logger.info("Notification de suppression d'utilisateur créée: {}", saved.getId());
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification de suppression", e);
            throw new RuntimeException("Erreur lors de la création de la notification", e);
        }
    }
    
    // Créer une notification pour rupture de stock
    public NotificationDto createStockOutNotification(Product product) {
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.STOCK_OUT);
            notification.setTitle("Rupture de stock");
            notification.setMessage(String.format("Produit en rupture de stock: %s (SKU: %s)", 
                    product.getName(),
                    product.getSku()));
            notification.setRelatedEntityId(product.getId());
            notification.setRelatedEntityType("PRODUCT");
            notification.setPriority(Notification.NotificationPriority.URGENT);
            notification.setActionUrl("/admin/products/" + product.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));
            
            Notification saved = notificationRepository.save(notification);
            logger.info("Notification de rupture de stock créée: {}", saved.getId());
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification de stock", e);
            throw new RuntimeException("Erreur lors de la création de la notification", e);
        }
    }
    
    // Créer une notification pour stock faible
    public NotificationDto createLowStockNotification(Product product, int currentStock) {
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.STOCK_LOW);
            notification.setTitle("Stock faible");
            notification.setMessage(String.format("Stock faible pour: %s (SKU: %s) - Quantité restante: %d", 
                    product.getName(),
                    product.getSku(),
                    currentStock));
            notification.setRelatedEntityId(product.getId());
            notification.setRelatedEntityType("PRODUCT");
            notification.setPriority(Notification.NotificationPriority.HIGH);
            notification.setActionUrl("/admin/products/" + product.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));
            
            Notification saved = notificationRepository.save(notification);
            logger.info("Notification de stock faible créée: {}", saved.getId());
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification de stock faible", e);
            throw new RuntimeException("Erreur lors de la création de la notification", e);
        }
    }
    
    // Créer une notification pour message de contact
    public NotificationDto createContactMessageNotification(String senderName, String senderEmail, String subject, String message, Long messageId) {
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.CONTACT_MESSAGE);
            notification.setTitle("Nouveau message de contact");
            notification.setMessage(String.format("Message de %s (%s): %s", 
                    senderName,
                    senderEmail,
                    subject));
            notification.setRelatedEntityId(messageId);
            notification.setRelatedEntityType("CONTACT");
            notification.setPriority(Notification.NotificationPriority.HIGH);
            notification.setActionUrl("/admin/contact/messages/" + messageId);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));
            
            Notification saved = notificationRepository.save(notification);
            logger.info("Notification de message de contact créée: {}", saved.getId());
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification de contact", e);
            throw new RuntimeException("Erreur lors de la création de la notification", e);
        }
    }
    
    /**
     * Crée une notification de test simple pour les administrateurs
     */
    public NotificationDto createTestNotification() {
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.SYSTEM_ALERT);
            notification.setTitle("Notification de test");
            notification.setMessage("Ceci est une notification de test générée par l'administrateur.");
            notification.setPriority(Notification.NotificationPriority.MEDIUM);
            notification.setRelatedEntityType("SYSTEM");
            notification.setActionUrl("/admin/notifications");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));

            Notification saved = notificationRepository.save(notification);
            logger.info("Notification de test créée: {}", saved.getId());
            NotificationDto dto = new NotificationDto(saved);
            try { webSocketNotificationService.sendNotificationToAdmins(dto); } catch (Exception ignore) {}
            return dto;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification de test", e);
            throw new RuntimeException("Erreur lors de la création de la notification de test", e);
        }
    }
    
    // Récupérer toutes les notifications avec pagination
    public Page<NotificationDto> getAllNotifications(int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                       Sort.by(sortBy).descending() : 
                       Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Notification> notifications = notificationRepository.findByRelatedUserIsNull(pageable);
            
            return notifications.map(NotificationDto::new);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications", e);
            throw new RuntimeException("Erreur lors de la récupération des notifications", e);
        }
    }
    
    // Récupérer les notifications non lues
    public List<NotificationDto> getUnreadNotifications() {
        try {
            List<Notification> notifications = notificationRepository.findAdminUnreadOrderByCreatedAtDesc();
            return notifications.stream()
                    .map(NotificationDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications non lues", e);
            throw new RuntimeException("Erreur lors de la récupération des notifications non lues", e);
        }
    }
    
    // Récupérer les notifications récentes (dernières 24h)
    public List<NotificationDto> getRecentNotifications(int limit) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
            Page<Notification> notifications = notificationRepository.findAdminByCreatedAtAfter(since, pageable);
            
            return notifications.getContent().stream()
                    .map(NotificationDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications récentes", e);
            throw new RuntimeException("Erreur lors de la récupération des notifications récentes", e);
        }
    }
    
    // Marquer une notification comme lue
    public NotificationDto markAsRead(Long notificationId) {
        try {
            Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
            if (optionalNotification.isPresent()) {
                Notification notification = optionalNotification.get();
                notification.markAsRead();
                Notification saved = notificationRepository.save(notification);
                logger.info("Notification {} marquée comme lue", notificationId);
                return new NotificationDto(saved);
            } else {
                throw new RuntimeException("Notification non trouvée: " + notificationId);
            }
        } catch (Exception e) {
            logger.error("Erreur lors du marquage de la notification comme lue", e);
            throw new RuntimeException("Erreur lors du marquage de la notification", e);
        }
    }
    
    // Marquer toutes les notifications comme lues
    public int markAllAsRead() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int updated = notificationRepository.markAllAsReadForAdmins(now);
            logger.info("{} notifications marquées comme lues", updated);
            return updated;
        } catch (Exception e) {
            logger.error("Erreur lors du marquage de toutes les notifications comme lues", e);
            throw new RuntimeException("Erreur lors du marquage des notifications", e);
        }
    }
    
    // Supprimer une notification
    public void deleteNotification(Long notificationId) {
        try {
            if (notificationRepository.existsById(notificationId)) {
                notificationRepository.deleteById(notificationId);
                logger.info("Notification {} supprimée", notificationId);
            } else {
                throw new RuntimeException("Notification non trouvée: " + notificationId);
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la notification", e);
            throw new RuntimeException("Erreur lors de la suppression de la notification", e);
        }
    }
    
    // Supprimer toutes les notifications
    public void deleteAllNotifications() {
        try {
            long count = notificationRepository.deleteAllAdmins();
            logger.info("{} notifications supprimées", count);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de toutes les notifications", e);
            throw new RuntimeException("Erreur lors de la suppression des notifications", e);
        }
    }
    
    // Supprimer les notifications lues
    public int deleteReadNotifications() {
        try {
            int deleted = notificationRepository.deleteReadAdmins();
            logger.info("{} notifications lues supprimées", deleted);
            return deleted;
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression des notifications lues", e);
            throw new RuntimeException("Erreur lors de la suppression des notifications lues", e);
        }
    }
    
    // Supprimer les notifications expirées
    public int deleteExpiredNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int deleted = notificationRepository.deleteExpiredNotificationsForAdmins(now);
            logger.info("{} notifications expirées supprimées", deleted);
            return deleted;
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression des notifications expirées", e);
            throw new RuntimeException("Erreur lors de la suppression des notifications expirées", e);
        }
    }
    
    // Compter les notifications non lues
    public long countUnreadNotifications() {
        try {
            return notificationRepository.countAdminUnread();
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des notifications non lues", e);
            return 0;
        }
    }
    
    // Récupérer les notifications par type
    public List<NotificationDto> getNotificationsByType(Notification.NotificationType type) {
        try {
            List<Notification> notifications = notificationRepository.findAdminByTypeOrderByCreatedAtDesc(type);
            return notifications.stream()
                    .map(NotificationDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications par type", e);
            throw new RuntimeException("Erreur lors de la récupération des notifications", e);
        }
    }
    
    // Récupérer les notifications par priorité
    public List<NotificationDto> getNotificationsByPriority(Notification.NotificationPriority priority) {
        try {
            List<Notification> notifications = notificationRepository.findAdminByPriorityOrderByCreatedAtDesc(priority);
            return notifications.stream()
                    .map(NotificationDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des notifications par priorité", e);
            throw new RuntimeException("Erreur lors de la récupération des notifications", e);
        }
    }
    
    // Nettoyer les anciennes notifications (plus de 30 jours et lues)
    public int cleanupOldNotifications() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            int deleted = notificationRepository.deleteOldReadNotificationsForAdmins(thirtyDaysAgo);
            logger.info("{} anciennes notifications nettoyées", deleted);
            return deleted;
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage des anciennes notifications", e);
            return 0;
        }
    }
    
    // Récupérer une notification par ID
    public Optional<NotificationDto> getNotificationById(Long id) {
        try {
            Optional<Notification> notification = notificationRepository.findById(id);
            return notification.map(NotificationDto::new);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la notification par ID", e);
            return Optional.empty();
        }
    }
}
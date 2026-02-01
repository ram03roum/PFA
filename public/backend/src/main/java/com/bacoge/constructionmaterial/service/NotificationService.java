package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.model.Order;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.model.Notification;
import com.bacoge.constructionmaterial.repository.NotificationRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               WebSocketNotificationService webSocketNotificationService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.webSocketNotificationService = webSocketNotificationService;
    }
    
    /**
     * Envoyer une notification de création de commande
     */
    public void sendOrderCreatedNotification(Order order) {
        if (order == null || order.getUser() == null) return;
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.ORDER_CREATED);
            notification.setTitle("Commande passée");
            notification.setMessage("Votre commande #" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()) + " a été enregistrée.");
            notification.setRelatedUser(order.getUser());
            notification.setRelatedEntityId(order.getId());
            notification.setRelatedEntityType("ORDER");
            notification.setPriority(Notification.NotificationPriority.HIGH);
            notification.setActionUrl("/orders/" + order.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));

            Notification saved = notificationRepository.save(notification);
            // Broadcast to client
            try { webSocketNotificationService.sendNotificationToClient(order.getUser().getId(), saved); } catch (Exception ignore) {}
            // Update client unread count
            long unread = notificationRepository.countByRelatedUserIdAndIsReadFalse(order.getUser().getId());
            try { webSocketNotificationService.sendUserNotificationCount(order.getUser().getId(), unread); } catch (Exception ignore) {}
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de création de commande", e);
        }
    }
    
    /**
     * Envoyer une notification de statut de commande
     */
    public void sendOrderStatusNotification(Order order, String newStatus) {
        if (order == null || order.getUser() == null) return;
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.ORDER_STATUS_CHANGED);
            notification.setTitle("Mise à jour de votre commande");
            notification.setMessage("Le statut de la commande #" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()) + " est maintenant " + newStatus + ".");
            notification.setRelatedUser(order.getUser());
            notification.setRelatedEntityId(order.getId());
            notification.setRelatedEntityType("ORDER");
            notification.setPriority(Notification.NotificationPriority.MEDIUM);
            notification.setActionUrl("/orders/" + order.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));

            Notification saved = notificationRepository.save(notification);
            try { webSocketNotificationService.sendNotificationToClient(order.getUser().getId(), saved); } catch (Exception ignore) {}
            long unread = notificationRepository.countByRelatedUserIdAndIsReadFalse(order.getUser().getId());
            try { webSocketNotificationService.sendUserNotificationCount(order.getUser().getId(), unread); } catch (Exception ignore) {}
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de statut de commande", e);
        }
    }
    
    /**
     * Envoyer une notification de paiement
     */
    public void sendPaymentNotification(Order order, String paymentStatus) {
        if (order == null || order.getUser() == null) return;
        try {
            Notification notification = new Notification();
            notification.setType("PAID".equalsIgnoreCase(paymentStatus) ? Notification.NotificationType.PAYMENT_RECEIVED : Notification.NotificationType.PAYMENT_FAILED);
            notification.setTitle("Paiement");
            notification.setMessage("Le statut de paiement de la commande #" + (order.getOrderNumber() != null ? order.getOrderNumber() : order.getId()) + " est: " + paymentStatus + ".");
            notification.setRelatedUser(order.getUser());
            notification.setRelatedEntityId(order.getId());
            notification.setRelatedEntityType("ORDER");
            notification.setPriority(Notification.NotificationPriority.HIGH);
            notification.setActionUrl("/orders/" + order.getId());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));

            Notification saved = notificationRepository.save(notification);
            try { webSocketNotificationService.sendNotificationToClient(order.getUser().getId(), saved); } catch (Exception ignore) {}
            long unread = notificationRepository.countByRelatedUserIdAndIsReadFalse(order.getUser().getId());
            try { webSocketNotificationService.sendUserNotificationCount(order.getUser().getId(), unread); } catch (Exception ignore) {}
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de paiement", e);
        }
    }
    
    /**
     * Envoyer une notification de stock faible
     */
    public void sendLowStockNotification(String productName, int currentStock) {
        // Géré côté admin via AdminNotificationService
        logger.debug("Low stock event: {} ({} left)", productName, currentStock);
    }
    
    /**
     * Envoyer une notification de nouveau message de contact
     */
    public void sendContactMessageNotification(String senderName, String senderEmail, String subject) {
        // Géré côté admin via AdminNotificationService
        logger.debug("Contact message event from {} <{}>: {}", senderName, senderEmail, subject);
    }
    
    /**
     * Envoyer une notification de nouveau utilisateur
     */
    public void sendNewUserNotification(User user) {
        if (user == null) return;
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.USER_REGISTERED);
            notification.setTitle("Bienvenue !");
            notification.setMessage("Votre compte a été créé avec succès.");
            notification.setRelatedUser(user);
            notification.setRelatedEntityId(user.getId());
            notification.setRelatedEntityType("USER");
            notification.setPriority(Notification.NotificationPriority.LOW);
            notification.setActionUrl("/profile");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));

            Notification saved = notificationRepository.save(notification);
            try { webSocketNotificationService.sendNotificationToClient(user.getId(), saved); } catch (Exception ignore) {}
            long unread = notificationRepository.countByRelatedUserIdAndIsReadFalse(user.getId());
            try { webSocketNotificationService.sendUserNotificationCount(user.getId(), unread); } catch (Exception ignore) {}
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification d'inscription", e);
        }
    }
    
    @Transactional
    public int markAllAsRead(Long userId) {
        if (userId == null) return 0;
        int updated = notificationRepository.markAllAsReadForUser(userId);
        long unread = notificationRepository.countByRelatedUserIdAndIsReadFalse(userId);
        try { webSocketNotificationService.sendUserNotificationCount(userId, unread); } catch (Exception ignore) {}
        return updated;
    }
    
    public Integer getUnreadCount(Long userId) {
        if (userId == null) return 0;
        return (int) notificationRepository.countByRelatedUserIdAndIsReadFalse(userId);
    }
    
    public Notification createNotification(Notification.NotificationType type,
                                 String title, String message, User user,
                                 Object relatedEntity1, Object relatedEntity2, Object relatedEntity3) {
        if (user == null) {
            logger.warn("createNotification: user is null");
            return null;
        }
        try {
            Notification notification = new Notification();
            notification.setType(type != null ? type : Notification.NotificationType.SYSTEM_UPDATE);
            notification.setTitle(title != null ? title : "Notification");
            notification.setMessage(message != null ? message : "");
            notification.setRelatedUser(user);
            notification.setPriority(type != null ? type.getDefaultPriority() : Notification.NotificationPriority.MEDIUM);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setExpiresAt(LocalDateTime.now().plusDays(30));
            // Note: relatedEntity fields can be set by callers via repository if needed

            Notification saved = notificationRepository.save(notification);
            try { webSocketNotificationService.sendNotificationToClient(user.getId(), saved); } catch (Exception ignore) {}
            long unread = notificationRepository.countByRelatedUserIdAndIsReadFalse(user.getId());
            try { webSocketNotificationService.sendUserNotificationCount(user.getId(), unread); } catch (Exception ignore) {}
            return saved;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la notification", e);
            throw e;
        }
    }
    
    public void sendUserDeletedNotification(User user) {
        // Géré côté admin via AdminNotificationService
        logger.debug("User deleted: {}", user != null ? user.getUsername() : "null");
    }
    
    public void sendNewConversationMessageNotification(Long userId, String senderName, String message, boolean isFromAdmin) {
        if (userId == null) return;
        userRepository.findById(userId).ifPresent(user -> {
            Notification n = new Notification();
            n.setType(Notification.NotificationType.MESSAGE_RECEIVED);
            n.setTitle(isFromAdmin ? "Message de l'administration" : "Nouveau message");
            n.setMessage((senderName != null ? senderName + ": " : "") + (message != null ? message : ""));
            n.setRelatedUser(user);
            n.setPriority(Notification.NotificationPriority.MEDIUM);
            n.setCreatedAt(LocalDateTime.now());
            Notification saved = notificationRepository.save(n);
            try { webSocketNotificationService.sendNotificationToClient(userId, saved); } catch (Exception ignore) {}
            long unread = notificationRepository.countByRelatedUserIdAndIsReadFalse(userId);
            try { webSocketNotificationService.sendUserNotificationCount(userId, unread); } catch (Exception ignore) {}
        });
    }
    
    public void sendUserRegisteredNotification(User user) {
        // Alias de sendNewUserNotification
        sendNewUserNotification(user);
    }
    
    public Page<Notification> getRecentNotifications(Long userId, int page, int size) {
        if (userId == null) return Page.empty();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findByRelatedUserId(userId, pageable);
    }
    
    public Page<Notification> findWithFilters(Long userId, List<Notification.NotificationType> types, Boolean read, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Specification<Notification> spec = (root, query, cb) -> cb.conjunction();
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("relatedUser").get("id"), userId));
        }
        if (types != null && !types.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("type").in(types));
        }
        if (read != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isRead"), read));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findAll(spec, pageable);
    }
    
    @Transactional
    public int deleteReadNotifications(Long userId) {
        if (userId == null) return 0;
        return notificationRepository.deleteReadByUserId(userId);
    }
    
    @Transactional
    public int deleteAllUserNotifications(Long userId) {
        if (userId == null) return 0;
        return notificationRepository.deleteAllByUserId(userId);
    }
    
    @Transactional
    public int deleteExpiredNotifications() {
        return notificationRepository.deleteExpiredNotifications(LocalDateTime.now());
    }
    
    @Transactional
    public void markAsRead(Long notificationId) {
        if (notificationId == null) return;
        Optional<Notification> opt = notificationRepository.findById(notificationId);
        if (opt.isPresent()) {
            Notification n = opt.get();
            if (Boolean.FALSE.equals(n.getIsRead())) {
                n.markAsRead();
                notificationRepository.save(n);
                Long userId = n.getRelatedUser() != null ? n.getRelatedUser().getId() : null;
                if (userId != null) {
                    long unread = notificationRepository.countByRelatedUserIdAndIsReadFalse(userId);
                    try { webSocketNotificationService.sendUserNotificationCount(userId, unread); } catch (Exception ignore) {}
                }
            }
        }
    }
}
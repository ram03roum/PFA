package com.bacoge.constructionmaterial.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User relatedUser; // Utilisateur concerné par la notification (optionnel)
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId; // ID de l'entité liée (commande, produit, etc.)
    
    @Column(name = "related_entity_type")
    private String relatedEntityType; // Type d'entité (ORDER, PRODUCT, USER, CONTACT)
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority = NotificationPriority.MEDIUM;
    
    @Column(name = "action_url")
    private String actionUrl; // URL d'action pour la notification
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Date d'expiration de la notification
    
    // Constructeurs
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Notification(NotificationType type, String title, String message) {
        this();
        this.type = type;
        this.title = title;
        this.message = message;
    }
    
    public Notification(NotificationType type, String title, String message, NotificationPriority priority) {
        this(type, title, message);
        this.priority = priority;
    }
    
    // Énumérations
    public enum NotificationType {
        // Order related
        ORDER_CREATED("Nouvelle commande", NotificationPriority.HIGH),
        ORDER_STATUS_CHANGED("Statut de commande modifié", NotificationPriority.MEDIUM),
        ORDER_CANCELLED("Commande annulée", NotificationPriority.HIGH),
        ORDER_SHIPPED("Commande expédiée", NotificationPriority.MEDIUM),
        ORDER_DELIVERED("Commande livrée", NotificationPriority.MEDIUM),
        
        // User related
        USER_REGISTERED("Nouvel utilisateur", NotificationPriority.MEDIUM),
        USER_EMAIL_VERIFIED("Email vérifié", NotificationPriority.LOW),
        USER_PROFILE_UPDATED("Profil utilisateur mis à jour", NotificationPriority.LOW),
        USER_PASSWORD_CHANGED("Mot de passe modifié", NotificationPriority.MEDIUM),
        USER_DELETED("Compte utilisateur supprimé", NotificationPriority.HIGH),
        USER_LOGGED_IN("Connexion utilisateur", NotificationPriority.LOW),
        USER_LOGGED_IN_FAILED("Échec de connexion", NotificationPriority.MEDIUM),
        
        // Stock related
        STOCK_LOW("Stock faible", NotificationPriority.MEDIUM),
        STOCK_CRITICAL("Stock critique", NotificationPriority.HIGH),
        STOCK_OUT("Rupture de stock", NotificationPriority.HIGH),
        STOCK_RESTOCKED("Stock réapprovisionné", NotificationPriority.MEDIUM),
        
        // Contact & Messages
        CONTACT_MESSAGE("Nouveau message de contact", NotificationPriority.MEDIUM),
        MESSAGE_RECEIVED("Nouveau message", NotificationPriority.MEDIUM),
        MESSAGE_REPLY("Réponse à votre message", NotificationPriority.MEDIUM),
        
        // Payment related
        PAYMENT_RECEIVED("Paiement reçu", NotificationPriority.HIGH),
        PAYMENT_FAILED("Paiement échoué", NotificationPriority.HIGH),
        PAYMENT_REFUNDED("Remboursement effectué", NotificationPriority.MEDIUM),
        
        // System & Admin
        SYSTEM_ALERT("Alerte système", NotificationPriority.URGENT),
        SYSTEM_MAINTENANCE("Maintenance planifiée", NotificationPriority.HIGH),
        SYSTEM_UPDATE("Mise à jour du système", NotificationPriority.MEDIUM),
        
        // Promotions & Marketing
        PROMOTION_CREATED("Nouvelle promotion", NotificationPriority.MEDIUM),
        PROMOTION_EXPIRING_SOON("Promotion bientôt expirée", NotificationPriority.MEDIUM),
        
        // Reviews & Feedback
        NEW_REVIEW("Nouvel avis client", NotificationPriority.LOW),
        FEEDBACK_RECEIVED("Avis reçu", NotificationPriority.LOW);
        
        private final String displayName;
        private final NotificationPriority defaultPriority;
        
        NotificationType(String displayName, NotificationPriority defaultPriority) {
            this.displayName = displayName;
            this.defaultPriority = defaultPriority;
        }
        
        NotificationType(String displayName) {
            this(displayName, NotificationPriority.MEDIUM);
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public NotificationPriority getDefaultPriority() {
            return defaultPriority;
        }
    }
    
    public enum NotificationPriority {
        LOW("Faible"),
        MEDIUM("Moyenne"),
        HIGH("Élevée"),
        URGENT("Urgente");
        
        private final String displayName;
        
        NotificationPriority(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Méthodes utilitaires
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void setExpirationDays(int days) {
        this.expiresAt = LocalDateTime.now().plusDays(days);
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public User getRelatedUser() {
        return relatedUser;
    }
    
    public void setRelatedUser(User relatedUser) {
        this.relatedUser = relatedUser;
    }
    
    public Long getRelatedEntityId() {
        return relatedEntityId;
    }
    
    public void setRelatedEntityId(Long relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    public String getRelatedEntityType() {
        return relatedEntityType;
    }
    
    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (isRead && readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }
    
    public NotificationPriority getPriority() {
        return priority;
    }
    
    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                '}';
    }
}
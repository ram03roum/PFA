package com.bacoge.constructionmaterial.dto;

import com.bacoge.constructionmaterial.model.Notification;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationDto {
    
    private Long id;
    private String type;
    private String typeDisplayName;
    private String title;
    private String message;
    private Long relatedUserId;
    private String relatedUserName;
    private Long relatedEntityId;
    private String relatedEntityType;
    private Boolean isRead;
    private String priority;
    private String priorityDisplayName;
    private String actionUrl;
    
    // Use ISO format with 'T' for better browser parsing
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private boolean expired;
    private String timeAgo;
    
    // Constructeurs
    public NotificationDto() {}
    
    public NotificationDto(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType().name();
        this.typeDisplayName = notification.getType().getDisplayName();
        this.title = notification.getTitle();
        this.message = notification.getMessage();
        
        if (notification.getRelatedUser() != null) {
            try {
                // Always expose the ID; names only if initialized to avoid LazyInitializationException
                this.relatedUserId = notification.getRelatedUser().getId();
                if (Hibernate.isInitialized(notification.getRelatedUser())) {
                    String first = notification.getRelatedUser().getFirstName();
                    String last = notification.getRelatedUser().getLastName();
                    this.relatedUserName = ((first != null ? first : "").trim() + " " + (last != null ? last : "").trim()).trim();
                }
            } catch (Exception ignored) {
                // Fallback: only set ID, keep name null
                try { this.relatedUserId = notification.getRelatedUser().getId(); } catch (Exception ignore2) {}
            }
        }
        
        this.relatedEntityId = notification.getRelatedEntityId();
        this.relatedEntityType = notification.getRelatedEntityType();
        this.isRead = notification.getIsRead();
        this.priority = notification.getPriority().name();
        this.priorityDisplayName = notification.getPriority().getDisplayName();
        this.actionUrl = notification.getActionUrl();
        this.createdAt = notification.getCreatedAt();
        this.readAt = notification.getReadAt();
        this.expiresAt = notification.getExpiresAt();
        this.expired = notification.isExpired();
        this.timeAgo = calculateTimeAgo(notification.getCreatedAt());
    }
    
    // Méthode utilitaire pour calculer le temps écoulé
    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) {
            return "À l'instant";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (minutes < 1440) { // 24 heures
            long hours = minutes / 60;
            return hours + " heure" + (hours > 1 ? "s" : "") + " ago";
        } else {
            long days = minutes / 1440;
            return days + " jour" + (days > 1 ? "s" : "") + " ago";
        }
    }
    
    // Méthodes statiques pour créer des notifications spécifiques
    public static NotificationDto createOrderNotification(Long orderId, String customerName, String amount) {
        NotificationDto dto = new NotificationDto();
        dto.setType(Notification.NotificationType.ORDER_CREATED.name());
        dto.setTypeDisplayName(Notification.NotificationType.ORDER_CREATED.getDisplayName());
        dto.setTitle("Nouvelle commande");
        dto.setMessage(String.format("Nouvelle commande de %s pour %s€", customerName, amount));
        dto.setRelatedEntityId(orderId);
        dto.setRelatedEntityType("ORDER");
        dto.setPriority(Notification.NotificationPriority.HIGH.name());
        dto.setPriorityDisplayName(Notification.NotificationPriority.HIGH.getDisplayName());
        dto.setActionUrl("/admin/orders/" + orderId);
        dto.setIsRead(false);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
    
    public static NotificationDto createStockNotification(Long productId, String productName, int stockLevel) {
        NotificationDto dto = new NotificationDto();
        dto.setType(stockLevel == 0 ? 
                   Notification.NotificationType.STOCK_OUT.name() : 
                   Notification.NotificationType.STOCK_LOW.name());
        dto.setTypeDisplayName(stockLevel == 0 ? 
                              Notification.NotificationType.STOCK_OUT.getDisplayName() : 
                              Notification.NotificationType.STOCK_LOW.getDisplayName());
        dto.setTitle(stockLevel == 0 ? "Rupture de stock" : "Stock faible");
        dto.setMessage(String.format("Produit %s : %s", productName, 
                      stockLevel == 0 ? "en rupture de stock" : "stock faible (" + stockLevel + ")"));
        dto.setRelatedEntityId(productId);
        dto.setRelatedEntityType("PRODUCT");
        dto.setPriority(stockLevel == 0 ? 
                       Notification.NotificationPriority.URGENT.name() : 
                       Notification.NotificationPriority.HIGH.name());
        dto.setPriorityDisplayName(stockLevel == 0 ? 
                                  Notification.NotificationPriority.URGENT.getDisplayName() : 
                                  Notification.NotificationPriority.HIGH.getDisplayName());
        dto.setActionUrl("/admin/products/" + productId);
        dto.setIsRead(false);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
    
    public static NotificationDto createUserNotification(Long userId, String userName, boolean isRegistration) {
        NotificationDto dto = new NotificationDto();
        dto.setType(isRegistration ? 
                   Notification.NotificationType.USER_REGISTERED.name() : 
                   Notification.NotificationType.USER_DELETED.name());
        dto.setTypeDisplayName(isRegistration ? 
                              Notification.NotificationType.USER_REGISTERED.getDisplayName() : 
                              Notification.NotificationType.USER_DELETED.getDisplayName());
        dto.setTitle(isRegistration ? "Nouvel utilisateur" : "Utilisateur supprimé");
        dto.setMessage(String.format("%s : %s", 
                      isRegistration ? "Nouvel utilisateur inscrit" : "Utilisateur supprimé", 
                      userName));
        dto.setRelatedEntityId(userId);
        dto.setRelatedEntityType("USER");
        dto.setPriority(Notification.NotificationPriority.MEDIUM.name());
        dto.setPriorityDisplayName(Notification.NotificationPriority.MEDIUM.getDisplayName());
        dto.setActionUrl("/admin/users/" + userId);
        dto.setIsRead(false);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
    
    public static NotificationDto createContactNotification(Long messageId, String senderName, String subject) {
        NotificationDto dto = new NotificationDto();
        dto.setType(Notification.NotificationType.CONTACT_MESSAGE.name());
        dto.setTypeDisplayName(Notification.NotificationType.CONTACT_MESSAGE.getDisplayName());
        dto.setTitle("Nouveau message de contact");
        dto.setMessage(String.format("Message de %s : %s", senderName, subject));
        dto.setRelatedEntityId(messageId);
        dto.setRelatedEntityType("CONTACT");
        dto.setPriority(Notification.NotificationPriority.HIGH.name());
        dto.setPriorityDisplayName(Notification.NotificationPriority.HIGH.getDisplayName());
        dto.setActionUrl("/admin/contact/messages/" + messageId);
        dto.setIsRead(false);
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTypeDisplayName() {
        return typeDisplayName;
    }
    
    public void setTypeDisplayName(String typeDisplayName) {
        this.typeDisplayName = typeDisplayName;
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
    
    public Long getRelatedUserId() {
        return relatedUserId;
    }
    
    public void setRelatedUserId(Long relatedUserId) {
        this.relatedUserId = relatedUserId;
    }
    
    public String getRelatedUserName() {
        return relatedUserName;
    }
    
    public void setRelatedUserName(String relatedUserName) {
        this.relatedUserName = relatedUserName;
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
    }
    
    // Expose "read" for frontend compatibility (notification.read)
    @JsonProperty("read")
    public Boolean getRead() {
        return isRead;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getPriorityDisplayName() {
        return priorityDisplayName;
    }
    
    public void setPriorityDisplayName(String priorityDisplayName) {
        this.priorityDisplayName = priorityDisplayName;
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
        this.timeAgo = calculateTimeAgo(createdAt);
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
    
    public boolean isExpired() {
        return expired;
    }
    
    public void setExpired(boolean expired) {
        this.expired = expired;
    }
    
    public String getTimeAgo() {
        return timeAgo;
    }
    
    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationDto that = (NotificationDto) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "NotificationDto{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", priority='" + priority + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
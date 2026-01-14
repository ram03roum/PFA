package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.NotificationDto;
import com.bacoge.constructionmaterial.model.Notification;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.service.NotificationService;
import com.bacoge.constructionmaterial.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class ClientNotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public ClientNotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/client")
    public ResponseEntity<?> getClientNotifications(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }
            
            User user = userService.findByUsername(authentication.getName());
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            org.springframework.data.domain.Page<com.bacoge.constructionmaterial.model.Notification> notifications = notificationService.getRecentNotifications(user.getId(), 0, 100);
            List<NotificationDto> notificationDtos = notifications.getContent().stream()
                .map(NotificationDto::new)
                .collect(java.util.stream.Collectors.toList());

            long unreadCount = notificationService.getUnreadCount(user.getId()).longValue();

            return ResponseEntity.ok(new ClientNotificationsResponse(notificationDtos, unreadCount));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }
            
            User user = userService.findByUsername(authentication.getName());
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }
            
            User user = userService.findByUsername(authentication.getName());
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            notificationService.markAllAsRead(user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getUnreadCount(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }
            
            User user = userService.findByUsername(authentication.getName());
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            long count = notificationService.getUnreadCount(user.getId());
            return ResponseEntity.ok(new NotificationCountResponse(count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint pour envoyer une notification de test (admin uniquement)
    @PostMapping("/test/send")
    public ResponseEntity<?> sendTestNotification(@RequestBody TestNotificationRequest request, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).build();
            }
            
            // Vérifier que l'utilisateur est admin
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);
            if (!isAdmin) {
                return ResponseEntity.status(403).build();
            }

            // Validation des paramètres
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new TestNotificationResponse(false, "Nom d'utilisateur requis"));
            }

            // Trouver l'utilisateur cible
            User targetUser = userService.findByUsername(request.getUsername());
            if (targetUser == null) {
                return ResponseEntity.status(404)
                        .body(new TestNotificationResponse(false, "Utilisateur non trouvé"));
            }

            // Créer la notification avec gestion d'erreur pour le type
            Notification.NotificationType notificationType;
            try {
                notificationType = Notification.NotificationType.valueOf(
                        request.getType() != null ? request.getType() : "SYSTEM_ALERT"
                );
            } catch (IllegalArgumentException e) {
                notificationType = Notification.NotificationType.SYSTEM_ALERT;
            }
            
            Notification notification = notificationService.createNotification(
                    notificationType,
                    request.getTitle() != null ? request.getTitle() : "Notification",
                    request.getMessage() != null ? request.getMessage() : "",
                    targetUser,
                    null,
                    null,
                    null
            );

            return ResponseEntity.ok(new TestNotificationResponse(true, "Notification envoyée avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new TestNotificationResponse(false, "Erreur: " + e.getMessage()));
        }
    }

    public static class TestNotificationRequest {
        private String username;
        private String title;
        private String message;
        private String type;

        // Getters et setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class TestNotificationResponse {
        private boolean success;
        private String message;

        public TestNotificationResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType() != null ? notification.getType().name() : "INFO");
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    // Classes de réponse
    public static class ClientNotificationsResponse {
        private List<NotificationDto> notifications;
        private long unreadCount;

        public ClientNotificationsResponse(List<NotificationDto> notifications, long unreadCount) {
            this.notifications = notifications;
            this.unreadCount = unreadCount;
        }

        public List<NotificationDto> getNotifications() {
            return notifications;
        }

        public void setNotifications(List<NotificationDto> notifications) {
            this.notifications = notifications;
        }

        public long getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
        }
    }

    public static class NotificationCountResponse {
        private long count;

        public NotificationCountResponse(long count) {
            this.count = count;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}

package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.NotificationDto;
import com.bacoge.constructionmaterial.service.AdminNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
 

@RestController
@RequestMapping("/api/admin/notifications")

public class AdminNotificationController {
    
    private final AdminNotificationService notificationService;

    public AdminNotificationController(AdminNotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    /**
     * Get all notifications with pagination and filtering
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        // Use repository-backed service to get notifications (sorted)
        Page<com.bacoge.constructionmaterial.dto.NotificationDto> notifications = notificationService.getAllNotifications(page, size, sortBy, sortDir);
        List<com.bacoge.constructionmaterial.dto.NotificationDto> notificationDtos = notifications.getContent();
            
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notificationDtos);
        response.put("currentPage", notifications.getNumber());
        response.put("totalItems", notifications.getTotalElements());
        response.put("totalPages", notifications.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get recent notifications (last 10 by default)
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getRecentNotifications(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<NotificationDto> notificationDtos = notificationService.getRecentNotifications(limit);
            
        return ResponseEntity.ok(notificationDtos);
    }
    
    /**
     * Get notification by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        // In a real implementation, you would fetch the notification by ID
        // For now, we'll return 404 as we're still implementing this
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Mark a notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Mark all notifications as read for the current admin
     */
    @PutMapping("/read-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> markAllAsRead() {
        int count = notificationService.markAllAsRead();
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }
    
    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        // In a real implementation, you would delete the notification by ID
        // For now, we'll return 204 No Content as a placeholder
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete all read notifications for the current admin
     */
    @DeleteMapping("/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> deleteReadNotifications() {
        int count = notificationService.deleteReadNotifications();
        return ResponseEntity.ok(Collections.singletonMap("deletedCount", count));
    }
    
    /**
     * Delete all notifications for the current admin
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> deleteAllNotifications() {
        notificationService.deleteAllNotifications();
        return ResponseEntity.ok(Collections.singletonMap("deletedCount", 0));
    }
    
    /**
     * Cleanup expired notifications
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> cleanupOldNotifications() {
        int count = notificationService.deleteExpiredNotifications();
        return ResponseEntity.ok(Collections.singletonMap("deletedCount", count));
    }
    
    /**
     * Get unread notification count for the current admin
     */
    @GetMapping("/count/unread")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.countUnreadNotifications();
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }
    
    /**
     * Get total notification count
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getTotalCount() {
        long count = notificationService.getAllNotifications(0, 1, "createdAt", "desc").getTotalElements();
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }
    
    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        
        // In a real implementation, you would calculate various statistics
        // For now, we'll return a simple response
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", notificationService.getAllNotifications(0, 1, "createdAt", "desc").getTotalElements());
        stats.put("unread", notificationService.countUnreadNotifications());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Debug endpoint to check notifications count (no auth required)
     */
    @GetMapping("/debug/count")
    public ResponseEntity<Map<String, Object>> debugNotificationsCount() {
        try {
            // Get total count using pagination
            long totalCount = notificationService.getAllNotifications(0, 1, "createdAt", "desc").getTotalElements();
            long unreadCount = notificationService.countUnreadNotifications();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalNotifications", totalCount);
            response.put("unreadNotifications", unreadCount);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Create a test notification for debugging
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createTestNotification() {
        try {
            NotificationDto dto = notificationService.createTestNotification();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("notification", dto);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }



    // Helper method to get current admin ID from security context
    private Long getCurrentAdminId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                    // For admin users, we can return a default admin ID or parse from username
                    // This is a simplified implementation - adjust based on your User entity structure
                    return 1L; // Default admin ID
                }
            }
            return 1L; // Fallback admin ID
        } catch (Exception e) {
            return 1L; // Fallback admin ID
        }
    }
}

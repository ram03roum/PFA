package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.Notification;
import com.bacoge.constructionmaterial.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    
    // ========== BASIC QUERIES ==========
    
    /**
     * Find all notifications for a specific user with pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedUser.id = :userId")
    Page<Notification> findByRelatedUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find all unread notifications for a specific user
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedUser.id = :userId AND n.isRead = false")
    List<Notification> findByRelatedUserIdAndIsReadFalse(@Param("userId") Long userId);
    
    /**
     * Find all read notifications for a specific user
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedUser.id = :userId AND n.isRead = true")
    List<Notification> findByRelatedUserIdAndIsReadTrue(@Param("userId") Long userId);
    
    /**
     * Count unread notifications for a specific user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.relatedUser.id = :userId AND n.isRead = false")
    long countByRelatedUserIdAndIsReadFalse(@Param("userId") Long userId);
    
    /**
     * Find all notifications that have expired
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt < :date")
    List<Notification> findByExpiresAtBefore(@Param("date") LocalDateTime date);
    
    // ========== ADVANCED QUERIES ==========
    
    /**
     * Find notifications with complex filtering criteria using Specification
     */
    @NonNull
    Page<Notification> findAll(@NonNull Specification<Notification> spec, @NonNull Pageable pageable);
    
    // ========== BULK OPERATIONS ==========
    
    /**
     * Mark all notifications as read for a specific user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.relatedUser.id = :userId AND n.isRead = false")
    int markAllAsReadForUser(@Param("userId") Long userId);
    
    /**
     * Delete all read notifications for a specific user
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedUser.id = :userId AND n.isRead = true")
    int deleteReadByUserId(@Param("userId") Long userId);
    
    /**
     * Delete all notifications for a specific user
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedUser.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
    
    // ========== LEGACY QUERIES (to be reviewed/deprecated) ==========
    
    /**
     * Find all unread notifications (legacy)
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findByIsReadFalseOrderByCreatedAtDesc();
    
    /**
     * Find all notifications by type (legacy)
     */
    @Query("SELECT n FROM Notification n WHERE n.type = :type ORDER BY n.createdAt DESC")
    List<Notification> findByTypeOrderByCreatedAtDesc(@Param("type") Notification.NotificationType type);
    
    /**
     * Find all notifications by priority (legacy)
     */
    @Query("SELECT n FROM Notification n WHERE n.priority = :priority ORDER BY n.createdAt DESC")
    List<Notification> findByPriorityOrderByCreatedAtDesc(@Param("priority") Notification.NotificationPriority priority);
    
    /**
     * Find all unread notifications by priority (legacy)
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = false AND n.priority = :priority ORDER BY n.createdAt DESC")
    List<Notification> findByIsReadFalseAndPriorityOrderByCreatedAtDesc(@Param("priority") Notification.NotificationPriority priority);
    
    /**
     * Find all notifications for a specific user (legacy)
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedUser = :user ORDER BY n.createdAt DESC")
    List<Notification> findByRelatedUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * Find all notifications for a specific entity (legacy)
     */
    @Query("SELECT n FROM Notification n WHERE n.relatedEntityType = :entityType AND n.relatedEntityId = :entityId ORDER BY n.createdAt DESC")
    List<Notification> findByRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(
            @Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    /**
     * Trouve toutes les notifications avec pagination
     */
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Trouve toutes les notifications non lues avec pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findByIsReadFalseOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Compte le nombre de notifications non lues
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false")
    long countByIsReadFalse();
    
    /**
     * Compte le nombre de notifications par statut de lecture
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = :isRead")
    long countByIsRead(@Param("isRead") boolean isRead);
    
    /**
     * Trouve les notifications par statut de lecture
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = :isRead ORDER BY n.createdAt DESC")
    List<Notification> findByIsReadOrderByCreatedAtDesc(@Param("isRead") boolean isRead);
    
    /**
     * Trouve les notifications créées après une date avec pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt > :date")
    Page<Notification> findByCreatedAtAfter(@Param("date") LocalDateTime date, Pageable pageable);
    
    /**
     * Compte le nombre de notifications non lues par priorité
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false AND n.priority = :priority")
    long countByIsReadFalseAndPriority(@Param("priority") Notification.NotificationPriority priority);
    
    /**
     * Trouve les notifications créées après une date donnée
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt > :date ORDER BY n.createdAt DESC")
    List<Notification> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("date") LocalDateTime date);
    
    /**
     * Trouve les notifications non expirées
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NULL OR n.expiresAt > :now ORDER BY n.createdAt DESC")
    List<Notification> findNonExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Trouve les notifications expirées
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now ORDER BY n.createdAt DESC")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Marque toutes les notifications comme lues
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.isRead = false")
    int markAllAsRead(@Param("readAt") LocalDateTime readAt);
    
    /**
     * Marque les notifications d'un type spécifique comme lues
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.type = :type AND n.isRead = false")
    int markAsReadByType(@Param("type") Notification.NotificationType type, @Param("readAt") LocalDateTime readAt);
    
    /**
     * Supprime les notifications expirées
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now")
    int deleteExpiredNotifications(@Param("now") LocalDateTime now);
    
    /**
     * Supprime les notifications lues plus anciennes qu'une date donnée
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt <= :date")
    int deleteOldReadNotifications(@Param("date") LocalDateTime date);
    
    /**
     * Supprime les notifications par statut de lecture
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = :isRead")
    int deleteByIsRead(@Param("isRead") boolean isRead);
    
    /**
     * Trouve les notifications récentes (dernières 24h)
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("since") LocalDateTime since);

    // ========== ADMIN-SCOPED QUERIES (relatedUser IS NULL) ==========

    /** Page all admin (global) notifications */
    Page<Notification> findByRelatedUserIsNull(Pageable pageable);

    /** Count unread admin notifications */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isRead = false AND n.relatedUser IS NULL")
    long countAdminUnread();

    /** Admin recent notifications page */
    @Query("SELECT n FROM Notification n WHERE n.createdAt > :date AND n.relatedUser IS NULL")
    Page<Notification> findAdminByCreatedAtAfter(@Param("date") LocalDateTime date, Pageable pageable);

    /** Admin unread list ordered */
    @Query("SELECT n FROM Notification n WHERE n.isRead = false AND n.relatedUser IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findAdminUnreadOrderByCreatedAtDesc();

    /** Admin filter by type */
    @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.relatedUser IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findAdminByTypeOrderByCreatedAtDesc(@Param("type") Notification.NotificationType type);

    /** Admin filter by priority */
    @Query("SELECT n FROM Notification n WHERE n.priority = :priority AND n.relatedUser IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findAdminByPriorityOrderByCreatedAtDesc(@Param("priority") Notification.NotificationPriority priority);

    /** Admin mark all as read */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.isRead = false AND n.relatedUser IS NULL")
    int markAllAsReadForAdmins(@Param("readAt") LocalDateTime readAt);

    /** Admin delete read */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.relatedUser IS NULL")
    int deleteReadAdmins();

    /** Admin delete all */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedUser IS NULL")
    int deleteAllAdmins();

    /** Admin delete expired */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= :now AND n.relatedUser IS NULL")
    int deleteExpiredNotificationsForAdmins(@Param("now") LocalDateTime now);

    /** Admin cleanup old read */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt <= :date AND n.relatedUser IS NULL")
    int deleteOldReadNotificationsForAdmins(@Param("date") LocalDateTime date);

    // ========== CLIENT-SCOPED HELPERS ==========

    /** Count client unread by allowed types to avoid seeing admin-only notifications created in the past */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.relatedUser.id = :userId AND n.isRead = false AND n.type IN :types")
    long countClientUnreadByTypes(@Param("userId") Long userId, @Param("types") List<Notification.NotificationType> types);
    
    /**
     * Trouve les notifications par type et entité liée
     */
    @Query("SELECT n FROM Notification n WHERE n.type = :type AND n.relatedEntityType = :entityType AND n.relatedEntityId = :entityId ORDER BY n.createdAt DESC")
    List<Notification> findByTypeAndRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(
            @Param("type") Notification.NotificationType type, @Param("entityType") String entityType, @Param("entityId") Long entityId);
    
    /**
     * Statistiques des notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.createdAt >= :startDate AND n.createdAt <= :endDate")
    long countNotificationsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Trouve les notifications prioritaires non lues
     */
    @Query("SELECT n FROM Notification n WHERE n.isRead = false AND n.priority IN ('HIGH', 'URGENT') ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findHighPriorityUnreadNotifications();
}
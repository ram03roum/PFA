package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
    

    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate")
    List<User> findUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :date")
    long countByLastLoginAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findUsersWithFilters(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("role") User.UserRole role,
            @Param("status") User.UserStatus status,
            Pageable pageable
    );
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    long countUsersCreatedBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :date")
    long countByCreatedAtAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT FUNCTION('date_format', u.createdAt, '%Y-%m'), COUNT(u) FROM User u " +
           "WHERE u.createdAt >= :startDate GROUP BY FUNCTION('date_format', u.createdAt, '%Y-%m') " +
           "ORDER BY FUNCTION('date_format', u.createdAt, '%Y-%m') DESC")
    List<Object[]> countSignupsByMonth(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countByRole();
    
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countByStatus();

    @Query("SELECT COALESCE(u.city, 'Inconnu'), COUNT(u) FROM User u GROUP BY u.city")
    List<Object[]> countByCity();

    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate ORDER BY u.createdAt DESC")
    List<User> findRecentUsersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :date")
    List<User> findActiveUsersSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin IS NOT NULL AND u.lastLogin >= :date")
    long countActiveUsers(@Param("date") LocalDateTime date);
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Méthodes pour les statistiques du dashboard
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findTopByOrderByCreatedAtDesc(Pageable pageable);
    
    default List<User> findTopByOrderByCreatedAtDesc(int limit) {
        return findTopByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startOfDay AND u.createdAt < :endOfDay")
    long countNewUsersToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // Requête optimisée pour les statistiques utilisateurs - récupère toutes les stats en une seule requête
    @Query("SELECT " +
           "COUNT(u), " +
           "SUM(CASE WHEN u.role = 'CLIENT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN u.role = 'ADMIN' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN u.status = 'ACTIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN u.status = 'INACTIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN u.status = 'LOCKED' THEN 1 ELSE 0 END) " +
           "FROM User u")
    Object[] getUserStatsBatch();
    
    // Requête pour les inscriptions du mois actuel et précédent
    @Query("SELECT " +
           "SUM(CASE WHEN u.createdAt >= :currentMonthStart AND u.createdAt < :nextMonthStart THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN u.createdAt >= :previousMonthStart AND u.createdAt < :currentMonthStart THEN 1 ELSE 0 END) " +
           "FROM User u WHERE u.createdAt >= :previousMonthStart")
    Object[] getCurrentAndPreviousMonthRegistrations(@Param("previousMonthStart") LocalDateTime previousMonthStart, 
                                                     @Param("currentMonthStart") LocalDateTime currentMonthStart, 
                                                     @Param("nextMonthStart") LocalDateTime nextMonthStart);
    
    default Map<String, Long> getMonthlyUserRegistrations(int months) {
        // Générer des données de test pour les inscriptions mensuelles
        Map<String, Long> registrations = new java.util.HashMap<>();
        LocalDateTime current = LocalDateTime.now().minusMonths(months);
        
        for (int i = 0; i < months; i++) {
            String monthLabel = current.getMonth().name() + " " + current.getYear();
            registrations.put(monthLabel, (long)(Math.random() * 50 + 10));
            current = current.plusMonths(1);
        }
        
        return registrations;
    }
    
    default long countUsersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return countUsersCreatedBetweenDates(startDate, endDate);
    }
}
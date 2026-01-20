package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.Order;
import com.bacoge.constructionmaterial.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumber(@Param("orderNumber") String orderNumber);
    
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);
    
    @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.createdAt DESC")
    List<Order> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatus(@Param("status") Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :paymentStatus")
    List<Order> findByPaymentStatus(@Param("paymentStatus") Order.PaymentStatus paymentStatus);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Order o " +
           "WHERE (:orderNumber IS NULL OR o.orderNumber LIKE CONCAT('%', :orderNumber, '%')) " +
           "AND (:userId IS NULL OR o.user.id = :userId) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)")
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findOrdersWithFilters(
            @Param("orderNumber") String orderNumber,
            @Param("userId") Long userId,
            @Param("status") Order.OrderStatus status,
            @Param("paymentStatus") Order.PaymentStatus paymentStatus,
            Pageable pageable
    );
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") Order.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.paymentStatus = :paymentStatus")
    long countByPaymentStatus(@Param("paymentStatus") Order.PaymentStatus paymentStatus);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getTotalRevenueBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Méthodes pour les statistiques du dashboard
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product ORDER BY o.createdAt DESC")
    List<Order> findTopByOrderByCreatedAtDesc(Pageable pageable);
    
    default List<Order> findTopByOrderByCreatedAtDesc(int limit) {
        return findTopByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);
    
    default List<Order> findRecentOrders(int limit) {
        return findRecentOrders(PageRequest.of(0, limit));
    }
    
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Requête optimisée pour les statistiques du dashboard - récupère toutes les stats en une seule requête
    @Query("SELECT " +
           "COUNT(o), " +
           "SUM(CASE WHEN o.status = 'PENDING' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.status = 'CONFIRMED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.status = 'PROCESSING' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.status = 'SHIPPED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END) " +
           "FROM Order o")
    Object[] getOrderStatsBatch();
    
    // Requête optimisée pour les revenus du mois actuel et précédent
    @Query("SELECT " +
           "SUM(CASE WHEN o.createdAt >= :currentMonthStart AND o.createdAt < :nextMonthStart AND o.status = 'DELIVERED' THEN o.totalAmount ELSE 0 END), " +
           "SUM(CASE WHEN o.createdAt >= :previousMonthStart AND o.createdAt < :currentMonthStart AND o.status = 'DELIVERED' THEN o.totalAmount ELSE 0 END) " +
           "FROM Order o WHERE o.createdAt >= :previousMonthStart")
    Object[] getCurrentAndPreviousMonthRevenue(@Param("previousMonthStart") LocalDateTime previousMonthStart, 
                                               @Param("currentMonthStart") LocalDateTime currentMonthStart, 
                                               @Param("nextMonthStart") LocalDateTime nextMonthStart);
    
    // Requête pour compter les nouvelles commandes d'aujourd'hui
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay")
    long countNewOrdersToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // Méthodes pour les statistiques de vente
    @Query("SELECT c.name, SUM(oi.totalPrice) FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.category c WHERE o.status = 'DELIVERED' GROUP BY c.name")
    List<Object[]> getRevenueByCategory();
    
    @Query("SELECT c.name, COUNT(o) FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.category c WHERE o.status = 'DELIVERED' GROUP BY c.name")
    List<Object[]> getOrderCountByCategory();
    
    @Query("SELECT FUNCTION('date_format', o.createdAt, '%Y-%m'), SUM(o.totalAmount) " +
           "FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate " +
           "GROUP BY FUNCTION('date_format', o.createdAt, '%Y-%m') " +
           "ORDER BY FUNCTION('date_format', o.createdAt, '%Y-%m')")
    List<Object[]> getMonthlySales(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT p.name, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi JOIN oi.product p JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY p.id, p.name " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
    
    default List<Object[]> findTopSellingProducts(int limit) {
        return findTopSellingProducts(PageRequest.of(0, limit));
    }
    
    @Query("SELECT p.name, c.name, SUM(oi.quantity) as totalQuantity, SUM(oi.totalPrice) as totalRevenue " +
           "FROM OrderItem oi JOIN oi.product p JOIN p.category c JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY p.id, p.name, c.name ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    default List<Object[]> findTopSellingProductsBetween(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return findTopSellingProductsBetween(startDate, endDate, PageRequest.of(0, limit));
    }
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :date")
    long countCompletedOrdersAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :date")
    long countOrdersAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT FUNCTION('date_format', o.createdAt, '%Y-%m'), COUNT(o) " +
           "FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY FUNCTION('date_format', o.createdAt, '%Y-%m') " +
           "ORDER BY FUNCTION('date_format', o.createdAt, '%Y-%m')")
    List<Object[]> getMonthlyOrderCount(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT AVG(r.rating) FROM Review r JOIN Product p ON r.productId = p.id JOIN OrderItem oi ON oi.product.id = p.id JOIN Order o ON oi.order.id = o.id WHERE o.status = 'DELIVERED' AND r.isApproved = true")
    Double getAverageCustomerRating();
    
    // Méthodes pour le système de paiement
    @Query("SELECT o FROM Order o WHERE o.transactionId = :transactionId")
    Optional<Order> findByTransactionId(@Param("transactionId") String transactionId);
    
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.paymentStatus = :paymentStatus")
    List<Order> findByUserIdAndPaymentStatus(@Param("userId") Long userId, @Param("paymentStatus") Order.PaymentStatus paymentStatus);
    
    @Query("SELECT o FROM Order o WHERE o.paymentMethod = :paymentMethod AND o.createdAt >= :startDate")
    List<Order> findByPaymentMethodAndCreatedAtAfter(@Param("paymentMethod") String paymentMethod, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.paymentMethod, SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate GROUP BY o.paymentMethod")
    List<Object[]> getRevenueByPaymentMethodBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.name, SUM(oi.totalPrice) FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.category c WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate GROUP BY c.name")
    List<Object[]> getRevenueByCategoryBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.name, COUNT(o) FROM Order o JOIN o.orderItems oi JOIN oi.product p JOIN p.category c WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate GROUP BY c.name")
    List<Object[]> getOrderCountByCategoryBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
package com.bacoge.constructionmaterial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoRepositoryBean
public interface DashboardRepository<T, ID> extends JpaRepository<T, ID> {
    
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal sumTotalAmount();
    
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    BigDecimal sumTotalAmountByDateRange(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT p.category.name, SUM(oi.quantity * oi.unitPrice) " +
           "FROM OrderItem oi " +
           "JOIN oi.product p " +
           "GROUP BY p.category.name")
    List<Object[]> getSalesByCategory();
    
    @Query("SELECT FUNCTION('date_format', o.createdAt, '%Y-%m'), SUM(o.totalAmount) " +
           "FROM Order o " +
           "WHERE o.createdAt >= :monthsAgo " +
           "GROUP BY FUNCTION('date_format', o.createdAt, '%Y-%m')")
    List<Object[]> getMonthlySales(int monthsAgo);
    
    @Query("SELECT p.name, SUM(oi.quantity) as totalSold " +
           "FROM OrderItem oi " +
           "JOIN oi.product p " +
           "GROUP BY p.id, p.name " +
           "ORDER BY totalSold DESC " +
           "LIMIT :limit")
    List<Object[]> findTopSellingProducts(int limit);
    
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countByStatus();
    
    @Query("SELECT FUNCTION('date_format', u.createdAt, '%Y-%m'), COUNT(u) " +
           "FROM User u " +
           "WHERE u.createdAt >= :monthsAgo " +
           "GROUP BY FUNCTION('date_format', u.createdAt, '%Y-%m')")
    List<Object[]> countSignupsByMonth(int monthsAgo);
}

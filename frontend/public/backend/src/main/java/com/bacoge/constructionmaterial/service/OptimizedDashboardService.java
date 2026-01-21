package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.model.Order;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.OrderRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service optimisé pour le tableau de bord administrateur
 * Utilise des requêtes batch pour améliorer les performances
 */
@Service
@Transactional(readOnly = true)
public class OptimizedDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OptimizedDashboardService(OrderRepository orderRepository,
                                   UserRepository userRepository,
                                   ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    /**
     * Récupère toutes les statistiques des commandes en une seule requête
     */
    @Cacheable(value = "dashboard-stats", key = "'order-stats-batch'")
    public Map<String, Object> getOrderStatsBatch() {
        Object[] stats = orderRepository.getOrderStatsBatch();
        Map<String, Object> orderStats = new HashMap<>();
        
        if (stats != null && stats.length >= 7) {
            orderStats.put("total", ((Number) stats[0]).longValue());
            orderStats.put("pending", ((Number) stats[1]).longValue());
            orderStats.put("confirmed", ((Number) stats[2]).longValue());
            orderStats.put("processing", ((Number) stats[3]).longValue());
            orderStats.put("shipped", ((Number) stats[4]).longValue());
            orderStats.put("delivered", ((Number) stats[5]).longValue());
            orderStats.put("cancelled", ((Number) stats[6]).longValue());
        } else {
            // Valeurs par défaut en cas d'erreur
            orderStats.put("total", 0L);
            orderStats.put("pending", 0L);
            orderStats.put("confirmed", 0L);
            orderStats.put("processing", 0L);
            orderStats.put("shipped", 0L);
            orderStats.put("delivered", 0L);
            orderStats.put("cancelled", 0L);
        }
        
        return orderStats;
    }

    /**
     * Récupère toutes les statistiques des utilisateurs en une seule requête
     */
    @Cacheable(value = "dashboard-stats", key = "'user-stats-batch'")
    public Map<String, Object> getUserStatsBatch() {
        Object[] stats = userRepository.getUserStatsBatch();
        Map<String, Object> userStats = new HashMap<>();
        
        if (stats != null && stats.length >= 6) {
            userStats.put("total", ((Number) stats[0]).longValue());
            userStats.put("customers", ((Number) stats[1]).longValue());
            userStats.put("admins", ((Number) stats[2]).longValue());
            userStats.put("active", ((Number) stats[3]).longValue());
            userStats.put("inactive", ((Number) stats[4]).longValue());
            userStats.put("suspended", ((Number) stats[5]).longValue());
        } else {
            // Valeurs par défaut en cas d'erreur
            userStats.put("total", 0L);
            userStats.put("customers", 0L);
            userStats.put("admins", 0L);
            userStats.put("active", 0L);
            userStats.put("inactive", 0L);
            userStats.put("suspended", 0L);
        }
        
        return userStats;
    }

    /**
     * Récupère toutes les statistiques des produits en une seule requête
     */
    @Cacheable(value = "dashboard-stats", key = "'product-stats-batch'")
    public Map<String, Object> getProductStatsBatch() {
        Object[] stats = productRepository.getProductStatsBatch();
        Map<String, Object> productStats = new HashMap<>();
        
        if (stats != null && stats.length >= 7) {
            productStats.put("total", ((Number) stats[0]).longValue());
            productStats.put("active", ((Number) stats[1]).longValue());
            productStats.put("inactive", ((Number) stats[2]).longValue());
            productStats.put("outOfStock", ((Number) stats[3]).longValue());
            productStats.put("totalStockQuantity", ((Number) stats[4]).longValue());
            productStats.put("lowStockCount", ((Number) stats[5]).longValue());
            productStats.put("averagePrice", stats[6] != null ? ((Number) stats[6]).doubleValue() : 0.0);
        } else {
            // Valeurs par défaut en cas d'erreur
            productStats.put("total", 0L);
            productStats.put("active", 0L);
            productStats.put("inactive", 0L);
            productStats.put("outOfStock", 0L);
            productStats.put("totalStockQuantity", 0L);
            productStats.put("lowStockCount", 0L);
            productStats.put("averagePrice", 0.0);
        }
        
        return productStats;
    }

    /**
     * Récupère les revenus du mois actuel et précédent en une seule requête
     */
    @Cacheable(value = "dashboard-stats", key = "'revenue-comparison'")
    public Map<String, Object> getRevenueComparison() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentMonthStart = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDateTime nextMonthStart = currentMonthStart.plusMonths(1);
        
        Object[] revenueStats = orderRepository.getCurrentAndPreviousMonthRevenue(
            previousMonthStart, currentMonthStart, nextMonthStart);
        
        Map<String, Object> revenueComparison = new HashMap<>();
        
        if (revenueStats != null && revenueStats.length >= 2) {
            BigDecimal currentRevenue = revenueStats[0] != null ? (BigDecimal) revenueStats[0] : BigDecimal.ZERO;
            BigDecimal previousRevenue = revenueStats[1] != null ? (BigDecimal) revenueStats[1] : BigDecimal.ZERO;
            
            revenueComparison.put("currentMonth", currentRevenue);
            revenueComparison.put("previousMonth", previousRevenue);
            
            // Calcul du taux de croissance
            double growthRate = 0.0;
            if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
                growthRate = currentRevenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            }
            revenueComparison.put("growthRate", growthRate);
        } else {
            revenueComparison.put("currentMonth", BigDecimal.ZERO);
            revenueComparison.put("previousMonth", BigDecimal.ZERO);
            revenueComparison.put("growthRate", 0.0);
        }
        
        return revenueComparison;
    }

    /**
     * Récupère les inscriptions utilisateurs du mois actuel et précédent
     */
    @Cacheable(value = "dashboard-stats", key = "'registration-comparison'")
    public Map<String, Object> getRegistrationComparison() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentMonthStart = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDateTime nextMonthStart = currentMonthStart.plusMonths(1);
        
        Object[] registrationStats = userRepository.getCurrentAndPreviousMonthRegistrations(
            previousMonthStart, currentMonthStart, nextMonthStart);
        
        Map<String, Object> registrationComparison = new HashMap<>();
        
        if (registrationStats != null && registrationStats.length >= 2) {
            long currentRegistrations = ((Number) registrationStats[0]).longValue();
            long previousRegistrations = ((Number) registrationStats[1]).longValue();
            
            registrationComparison.put("currentMonth", currentRegistrations);
            registrationComparison.put("previousMonth", previousRegistrations);
            
            // Calcul du taux de croissance
            double growthRate = 0.0;
            if (previousRegistrations > 0) {
                growthRate = ((double) (currentRegistrations - previousRegistrations) / previousRegistrations) * 100;
            }
            registrationComparison.put("growthRate", growthRate);
        } else {
            registrationComparison.put("currentMonth", 0L);
            registrationComparison.put("previousMonth", 0L);
            registrationComparison.put("growthRate", 0.0);
        }
        
        return registrationComparison;
    }

    /**
     * Récupère les statistiques d'aujourd'hui (nouvelles commandes, utilisateurs, produits)
     */
    @Cacheable(value = "dashboard-stats", key = "'today-stats'")
    public Map<String, Object> getTodayStats() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        
        Map<String, Object> todayStats = new HashMap<>();
        
        // Nouvelles commandes aujourd'hui
        long newOrdersToday = orderRepository.countNewOrdersToday(startOfDay, endOfDay);
        todayStats.put("newOrders", newOrdersToday);
        
        // Nouveaux utilisateurs aujourd'hui
        long newUsersToday = userRepository.countNewUsersToday(startOfDay, endOfDay);
        todayStats.put("newUsers", newUsersToday);
        
        // Nouveaux produits aujourd'hui
        long newProductsToday = productRepository.countNewProductsToday(startOfDay, endOfDay);
        todayStats.put("newProducts", newProductsToday);
        
        return todayStats;
    }

    /**
     * Récupère toutes les statistiques optimisées du dashboard en une seule méthode
     */
    @Cacheable(value = "dashboard-stats", key = "'complete-dashboard-stats'")
    public Map<String, Object> getCompleteDashboardStats() {
        Map<String, Object> completeStats = new HashMap<>();
        
        // Statistiques par batch
        completeStats.put("orders", getOrderStatsBatch());
        completeStats.put("users", getUserStatsBatch());
        completeStats.put("products", getProductStatsBatch());
        
        // Comparaisons mensuelles
        completeStats.put("revenue", getRevenueComparison());
        completeStats.put("registrations", getRegistrationComparison());
        
        // Statistiques d'aujourd'hui
        completeStats.put("today", getTodayStats());
        
        // Timestamp pour le cache
        completeStats.put("timestamp", LocalDateTime.now());
        
        return completeStats;
    }
}
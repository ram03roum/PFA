package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.DashboardStats;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service principal pour le tableau de bord
 * Fournit des statistiques et des données pour l'interface d'administration
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    private static final int MAX_TOP_PRODUCTS = 5;
    private static final int DEFAULT_MONTHS = 6;
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    
    public DashboardService(UserRepository userRepository,
                          ProductRepository productRepository,
                          OrderRepository orderRepository,
                          CategoryRepository categoryRepository,
                          ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
    }
    
    /**
     * Récupère les statistiques principales du tableau de bord
     * @return Objet DashboardStats contenant toutes les statistiques
     */
    @Cacheable(value = "dashboardStats", key = "'main'")
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime startOfYear = now.withDayOfYear(1).with(LocalTime.MIN);
        
        try {
            // 1. Compteurs de base
            long totalUsers = userRepository.count();
            logger.info("Total users: {}", totalUsers);
            stats.setTotalUsers(totalUsers);
            
            long totalProducts = productRepository.count();
            logger.info("Total products: {}", totalProducts);
            stats.setTotalProducts(totalProducts);
            
            long totalOrders = orderRepository.count();
            logger.info("Total orders: {}", totalOrders);
            stats.setTotalOrders(totalOrders);
            
            long totalCategories = categoryRepository.count();
            logger.info("Total categories: {}", totalCategories);
            stats.setTotalCategories(totalCategories);
            
            // 1.1. Statistiques détaillées des produits
            Map<String, Long> productStats = new HashMap<>();
            productStats.put("total", productRepository.count());
            productStats.put("active", productRepository.countByStatus(Product.ProductStatus.ACTIVE));
            productStats.put("outOfStock", productRepository.countByStatus(Product.ProductStatus.OUT_OF_STOCK));
            productStats.put("lowStock", productRepository.countLowStockProducts());
            stats.setProductStats(productStats);
            
            // 2. Revenus
            BigDecimal totalRevenue = orderRepository.getTotalRevenue();
            BigDecimal todayRevenue = orderRepository.getRevenueByDateRange(startOfDay, now);
            BigDecimal monthlyRevenue = orderRepository.getRevenueByDateRange(startOfMonth, now);
            BigDecimal yearlyRevenue = orderRepository.getRevenueByDateRange(startOfYear, now);
            
            stats.setTotalRevenue(totalRevenue != null ? totalRevenue.doubleValue() : 0.0);
            stats.setTodayRevenue(todayRevenue != null ? todayRevenue.doubleValue() : 0.0);
            stats.setMonthlyRevenue(monthlyRevenue != null ? monthlyRevenue.doubleValue() : 0.0);
            stats.setYearlyRevenue(yearlyRevenue != null ? yearlyRevenue.doubleValue() : 0.0);
            
            // 3. Utilisateurs
            long newUsersToday = userRepository.countByCreatedAtAfter(startOfDay);
            long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);
            // Utilisation de la méthode de requête personnalisée
            long activeUsers = userRepository.countByLastLoginAfter(now.minusMonths(1));
            
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("newToday", newUsersToday);
            userStats.put("newThisMonth", newUsersThisMonth);
            userStats.put("active", activeUsers);
            stats.setUserStats(userStats);
            
            // 4. Commandes - utilisation de la méthode optimisée
            Object[] orderStatsArray = orderRepository.getOrderStatsBatch();
            Map<String, Long> orderStats = new HashMap<>();
            if (orderStatsArray != null && orderStatsArray.length >= 7) {
                orderStats.put("total", ((Number) orderStatsArray[0]).longValue());
                orderStats.put("pending", ((Number) orderStatsArray[1]).longValue());
                orderStats.put("confirmed", ((Number) orderStatsArray[2]).longValue());
                orderStats.put("processing", ((Number) orderStatsArray[3]).longValue());
                orderStats.put("shipped", ((Number) orderStatsArray[4]).longValue());
                orderStats.put("delivered", ((Number) orderStatsArray[5]).longValue());
                orderStats.put("cancelled", ((Number) orderStatsArray[6]).longValue());
                
                // Calcul des commandes complétées (delivered + shipped) et en attente (pending + confirmed + processing)
    orderStats.put("completed", orderStats.get("delivered") + orderStats.get("shipped"));
    orderStats.put("pending", orderStats.get("pending") + orderStats.get("confirmed") + orderStats.get("processing"));
            }
            
            // Ajout des commandes d'aujourd'hui
            long ordersToday = orderRepository.countNewOrdersToday(startOfDay, startOfDay.plusDays(1));
            orderStats.put("today", ordersToday);
            
            stats.setOrderStatusCount(orderStats);
            
            // 5. Produits les plus vendus (exemple simplifié - à adapter selon votre modèle de données)
            List<Map<String, Object>> topProducts = productRepository.findAll(PageRequest.of(0, MAX_TOP_PRODUCTS))
                .stream()
                .map(product -> {
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("id", product.getId());
                    productData.put("name", product.getName());
                    // Exemple: utiliser la quantité en stock comme indicateur de vente
                    // À remplacer par votre logique métier réelle
                    productData.put("sales", product.getStockQuantity() != null ? 100 - product.getStockQuantity() : 0);
                    // Exemple de calcul de revenu (prix * ventes estimées)
                    BigDecimal revenue = product.getPrice() != null ? 
                        product.getPrice().multiply(BigDecimal.valueOf((Integer)productData.get("sales"))) : 
                        BigDecimal.ZERO;
                    productData.put("revenue", revenue);
                    return productData;
                })
                .sorted((p1, p2) -> ((Integer)p2.get("sales")).compareTo((Integer)p1.get("sales")))
                .collect(Collectors.toList());
            stats.setTopProducts(topProducts);
            
            // 6. Statistiques de vente par catégorie
            Map<String, Double> salesByCategory = productRepository.getSalesByCategory()
                .stream()
                .collect(Collectors.toMap(
                    tuple -> tuple[0].toString(),
                    tuple -> ((Number) tuple[1]).doubleValue(),
                    (existing, replacement) -> existing + ((Number) replacement).doubleValue()
                ));
            stats.setSalesByCategory(salesByCategory);
            
            // 7. Évolution des ventes
            Map<String, Object> salesTrends = getSalesTrends(DEFAULT_MONTHS);
            stats.setSalesTrends(salesTrends);

            // 8. Taux de conversion (exemple : commandes complétées / utilisateurs actifs * 100)
            double conversionRate = (stats.getCompletedOrders() > 0 && stats.getActiveUsers() > 0) ?
                (double) stats.getCompletedOrders() / stats.getActiveUsers() * 100 : 0.0;
            stats.setConversionRate(conversionRate);

            // 9. Satisfaction client (moyenne globale des notes)
            Double customerSatisfaction = reviewRepository.findGlobalAverageRating();
            stats.setCustomerSatisfaction(customerSatisfaction != null ? customerSatisfaction : 0.0);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats", e);
            return stats; // Retourne les statistiques déjà calculées
        }
    }

    /**
     * Récupère l'évolution des ventes sur une période donnée
     * @param months Nombre de mois à inclure dans la tendance
     * @return Map contenant les données de tendance des ventes
     */
    @Cacheable(value = "salesTrends", key = "#months")
    public Map<String, Object> getSalesTrends(int months) {
        Map<String, Object> result = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> salesData = new ArrayList<>();
        
        // Obtenir la date actuelle et la date de début (il y a X mois)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);
        
        // Générer les mois entre startDate et endDate
        LocalDate current = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);
        
        while (!current.isAfter(endDate)) {
            // Ajouter le mois courant aux labels
            labels.add(current.format(formatter));
            
            // Calculer le début et la fin du mois
            LocalDateTime monthStart = current.atStartOfDay();
            LocalDateTime monthEnd = current.withDayOfMonth(current.lengthOfMonth()).atTime(23, 59, 59);
            
            // Récupérer le chiffre d'affaires pour le mois
            BigDecimal monthlySales = orderRepository.getRevenueByDateRange(monthStart, monthEnd);
            salesData.add(monthlySales != null ? monthlySales : BigDecimal.ZERO);
            
            // Passer au mois suivant
            current = current.plusMonths(1);
        }
        
        result.put("labels", labels);
        result.put("sales", salesData);
        
        // Calculer la tendance (hausse/baisse)
        if (salesData.size() >= 2) {
            BigDecimal currentSales = salesData.get(salesData.size() - 1);
            BigDecimal previousSales = salesData.get(salesData.size() - 2);
            
            if (previousSales.compareTo(BigDecimal.ZERO) != 0) {
                double growth = currentSales.subtract(previousSales)
                    .divide(previousSales, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
                
                result.put("trend", growth);
                result.put("trendDirection", growth >= 0 ? "up" : "down");
            } else {
                result.put("trend", 0.0);
                result.put("trendDirection", "stable");
            }
        }
        
        return result;
    }
    
    /**
     * Récupère les activités récentes (commandes, inscriptions, etc.)
     * @return Liste des activités récentes
     */
    @Cacheable(value = "recentActivity")
    public List<Map<String, Object>> getRecentActivity() {
        try {
            return orderRepository.findRecentOrders(10).stream()
                .map(order -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", order.getId());
                    activity.put("type", "ORDER");
                    activity.put("description", "Nouvelle commande #" + order.getId());
                    activity.put("timestamp", order.getCreatedAt());
                    activity.put("status", order.getStatus());
                    activity.put("amount", order.getTotalAmount());
                    return activity;
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching recent activity", e);
            return List.of();
        }
    }

    public Map<String, Object> getSalesData() {
        Map<String, Object> salesData = new HashMap<>();
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        
        try {
            // Derniers 6 mois de données de vente
            List<Object[]> monthlySalesData = orderRepository.getMonthlySales(sixMonthsAgo);
            // Formater les données pour le frontend
            List<Map<String, Object>> formattedMonthlySales = monthlySalesData.stream()
                .map(row -> {
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("month", row[0]); // La date formatée
                    monthData.put("amount", row[1]); // Le montant total
                    return monthData;
                })
                .collect(Collectors.toList());
            salesData.put("monthlySales", formattedMonthlySales);
            
            // Meilleurs produits - utilise directement le résultat formaté de findTopSellingProducts
            List<Map<String, Object>> topProducts = productRepository.findTopSellingProducts(5);
            salesData.put("topProducts", topProducts);
            
            return salesData;
            
        } catch (Exception e) {
            logger.error("Error fetching sales data", e);
            return salesData; // Retourne les données déjà ajoutées
        }
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> userStats = new HashMap<>();
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        
        try {
            // Nombre d'utilisateurs par statut
            List<Object[]> usersByStatus = userRepository.countByStatus();
            userStats.put("byStatus", usersByStatus);
            
            // Inscriptions par mois
            List<Object[]> signupsByMonth = userRepository.countSignupsByMonth(sixMonthsAgo);
            userStats.put("signupsByMonth", signupsByMonth);
            
            // Utilisateurs actifs (dernier login dans les 30 derniers jours)
            long activeUsers = userRepository.countActiveUsers(LocalDateTime.now().minusDays(30));
            userStats.put("activeUsers", activeUsers);
            
            return userStats;
            
        } catch (Exception e) {
            logger.error("Error fetching user stats", e);
            return userStats; // Retourne les données déjà ajoutées
        }
    }
}

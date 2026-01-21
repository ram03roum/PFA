package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.DashboardStats;
import com.bacoge.constructionmaterial.service.DashboardService;
import com.bacoge.constructionmaterial.service.admin.AdminPromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AdminPromotionService adminPromotionService;

    public DashboardController(DashboardService dashboardService, AdminPromotionService adminPromotionService) {
        this.dashboardService = dashboardService;
        this.adminPromotionService = adminPromotionService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        System.out.println("Entering getDashboardStats endpoint");
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            Map<String, Object> response = new HashMap<>();
            
            // Statistiques de base
            response.put("totalUsers", stats.getTotalUsers());
            response.put("totalProducts", stats.getTotalProducts());
            response.put("totalOrders", stats.getTotalOrders());
            response.put("totalCategories", stats.getTotalCategories());
            
            // Revenus
            response.put("totalRevenue", stats.getTotalRevenue());
            response.put("todayRevenue", stats.getTodayRevenue());
            response.put("monthlyRevenue", stats.getMonthlyRevenue());
            response.put("yearlyRevenue", stats.getYearlyRevenue());
            
            // Statistiques de croissance
            response.put("revenueGrowth", stats.getRevenueGrowth());
            response.put("monthlyGrowth", stats.getMonthlyGrowth());
            
            // Utilisateurs
            response.put("newUsersToday", stats.getNewUsersToday());
            response.put("newUsersThisMonth", stats.getNewUsersThisMonth());
            response.put("activeUsers", stats.getActiveUsers());
            
            // Commandes
            response.put("pendingOrders", stats.getPendingOrders());
            response.put("completedOrders", stats.getCompletedOrders());
            response.put("conversionRate", stats.getConversionRate());
            response.put("customerSatisfaction", stats.getCustomerSatisfaction());

            // Promotions
            response.put("totalPromotions", adminPromotionService.getTotalPromotions());
            response.put("activePromotions", adminPromotionService.getActivePromotionsCount());
            response.put("expiredPromotions", adminPromotionService.getExpiredPromotionsCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des statistiques");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<?> getRecentActivity() {
        try {
            List<Map<String, Object>> activities = dashboardService.getRecentActivity();
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des activités récentes");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/sales-data")
    public ResponseEntity<?> getSalesData() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            Map<String, Object> response = new HashMap<>();
            
            // Données de tendance des ventes
            response.put("salesTrends", stats.getSalesTrends());
            
            // Meilleurs produits
            response.put("topProducts", stats.getTopProducts() != null ? stats.getTopProducts() : List.of());
            
            // Ventes par catégorie
            response.put("salesByCategory", stats.getSalesByCategory() != null ? stats.getSalesByCategory() : Map.of());
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des données de vente");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/user-stats")
    public ResponseEntity<?> getUserStats() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            Map<String, Object> response = new HashMap<>();
            
            // Statistiques utilisateurs
            response.put("totalUsers", stats.getTotalUsers());
            response.put("newUsersToday", stats.getNewUsersToday());
            response.put("newUsersThisMonth", stats.getNewUsersThisMonth());
            response.put("activeUsers", stats.getActiveUsers());
            
            // Statistiques de commandes
            response.put("pendingOrders", stats.getPendingOrders());
            response.put("completedOrders", stats.getCompletedOrders());
            
            // Autres statistiques utilisateurs (à implémenter si nécessaire)
            response.put("userRegistrationStats", stats.getUserStats());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des statistiques utilisateurs");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/charts-data")
    public ResponseEntity<Map<String, Object>> getChartsData() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            Map<String, Object> response = new HashMap<>();
            
            // Données pour les graphiques
            response.put("salesTrends", stats.getSalesTrends());
            response.put("salesByCategory", stats.getSalesByCategory());
            response.put("topProducts", stats.getTopProducts());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors du chargement des données des graphiques");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/performance-metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            Map<String, Object> response = new HashMap<>();
            
            response.put("revenueGrowth", stats.getRevenueGrowth());
            response.put("monthlyGrowth", stats.getMonthlyGrowth());
            response.put("userGrowth", stats.getUserGrowth());
            response.put("orderGrowth", stats.getOrderGrowth());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/monthly-sales")
    public ResponseEntity<Map<String, Object>> getMonthlySales() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            Map<String, Object> response = new HashMap<>();
            
            response.put("salesTrends", stats.getSalesTrends());
            response.put("monthlyRevenue", stats.getMonthlyRevenue());
            response.put("yearlyRevenue", stats.getYearlyRevenue());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/category-distribution")
    public ResponseEntity<Map<String, Object>> getCategoryDistribution() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            Map<String, Object> response = new HashMap<>();
            
            response.put("salesByCategory", stats.getSalesByCategory());
            response.put("totalCategories", stats.getTotalCategories());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats();
            Map<String, Object> response = new HashMap<>();
            
            response.put("totalRevenue", stats.getTotalRevenue());
            response.put("todayRevenue", stats.getTodayRevenue());
            response.put("monthlyRevenue", stats.getMonthlyRevenue());
            response.put("yearlyRevenue", stats.getYearlyRevenue());
            response.put("revenueGrowth", stats.getRevenueGrowth());
            response.put("monthlyGrowth", stats.getMonthlyGrowth());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des données de revenus");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Méthode getRecentActivity() supprimée car elle était en double
}

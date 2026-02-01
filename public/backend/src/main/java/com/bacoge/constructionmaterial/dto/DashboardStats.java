package com.bacoge.constructionmaterial.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    // Compteurs de base
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private long totalCategories;
    
    // Revenus
    private Double totalRevenue;
    private Double todayRevenue;
    private Double monthlyRevenue;
    private Double yearlyRevenue;
    
    // Statistiques détaillées
    private Map<String, Long> orderStatusCount;
    private Map<String, Object> userStats;
    private Map<String, Long> productStats;
    private Map<String, Double> salesByCategory;
    private List<Map<String, Object>> topProducts;
    private Map<String, Object> salesTrends;
    private double conversionRate;
    private double customerSatisfaction;
    private List<Map<String, Object>> recentActivity;
    
    // Méthodes utilitaires pour faciliter l'accès aux données
    public long getPendingOrders() {
        return orderStatusCount != null ? orderStatusCount.getOrDefault("pending", 0L) : 0L;
    }
    
    public long getCompletedOrders() {
        return orderStatusCount != null ? orderStatusCount.getOrDefault("completed", 0L) : 0L;
    }
    
    public long getTodayOrders() {
        return orderStatusCount != null ? orderStatusCount.getOrDefault("today", 0L) : 0L;
    }
    
    
    
    public long getNewUsersToday() {
        if (userStats == null) return 0L;
        Object value = userStats.getOrDefault("newToday", 0L);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
    
    public long getNewUsersThisMonth() {
        if (userStats == null) return 0L;
        Object value = userStats.getOrDefault("newThisMonth", 0L);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
    
    public long getActiveUsers() {
        if (userStats == null) return 0L;
        Object value = userStats.getOrDefault("active", 0L);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
    
    public long getLowStockProducts() {
        return productStats != null ? productStats.getOrDefault("lowStock", 0L) : 0L;
    }
    
    public long getOutOfStockProducts() {
        return productStats != null ? productStats.getOrDefault("outOfStock", 0L) : 0L;
    }
    
    public long getActiveProducts() {
        return productStats != null ? productStats.getOrDefault("active", 0L) : 0L;
    }
    
    public double getRevenueGrowth() {
        if (monthlyRevenue == null || todayRevenue == null) return 0.0;
        
        double previousMonth = monthlyRevenue - todayRevenue;
        if (previousMonth == 0.0) return 0.0;
        
        return ((todayRevenue - previousMonth) / previousMonth) * 100.0;
    }
    
    public double getUserGrowth() {
        if (userStats == null) return 0.0;
        Object newThisMonthObj = userStats.getOrDefault("newThisMonth", 0L);
        Object newTodayObj = userStats.getOrDefault("newToday", 0L);
        
        long newThisMonth = newThisMonthObj instanceof Number ? ((Number) newThisMonthObj).longValue() : 0L;
        long newToday = newTodayObj instanceof Number ? ((Number) newTodayObj).longValue() : 0L;
        
        if (newThisMonth == 0) return 0.0;
        return ((double) newToday / newThisMonth) * 100;
    }
    
    public double getOrderGrowth() {
        if (orderStatusCount == null) return 0.0;
        long todayOrders = orderStatusCount.getOrDefault("today", 0L);
        long totalOrders = orderStatusCount.getOrDefault("total", 0L);
        
        if (totalOrders == 0) return 0.0;
        return ((double) todayOrders / totalOrders) * 100;
    }
    
    public List<Object> getRecentOrders() {
        return recentActivity != null ? new ArrayList<>(recentActivity) : List.of();
    }
    
    public List<Object> getRecentUsers() {
        return topProducts != null ? new ArrayList<>(topProducts) : List.of();
    }
    
    public double getMonthlyGrowth() {
        if (monthlyRevenue == null || yearlyRevenue == null) return 0.0;
        
        double previousYear = yearlyRevenue - monthlyRevenue;
        if (previousYear == 0.0) return 0.0;
        
        return ((monthlyRevenue - previousYear) / previousYear) * 100.0;
    }
    
    public List<Map<String, Object>> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<Map<String, Object>> recentActivity) {
        this.recentActivity = recentActivity;
    }

    public Map<String, Object> getSalesTrends() {
        return salesTrends;
    }

    public void setSalesTrends(Map<String, Object> salesTrends) {
        this.salesTrends = salesTrends;
    }

    public Double getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(Double monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public Double getYearlyRevenue() {
        return yearlyRevenue;
    }

    public void setYearlyRevenue(Double yearlyRevenue) {
        this.yearlyRevenue = yearlyRevenue;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public Map<String, Object> getUserStats() {
        return userStats;
    }

    public void setUserStats(Map<String, Object> userStats) {
        this.userStats = userStats;
    }

    public Map<String, Double> getSalesByCategory() {
        return salesByCategory;
    }

    public void setSalesByCategory(Map<String, Double> salesByCategory) {
        this.salesByCategory = salesByCategory;
    }

    public List<Map<String, Object>> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<Map<String, Object>> topProducts) {
        this.topProducts = topProducts;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public double getCustomerSatisfaction() {
        return customerSatisfaction;
    }

    public void setCustomerSatisfaction(double customerSatisfaction) {
        this.customerSatisfaction = customerSatisfaction;
    }

    public Double getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(Double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Map<String, Long> getProductStats() {
        return productStats;
    }

    public void setProductStats(Map<String, Long> productStats) {
        this.productStats = productStats;
    }

    public Map<String, Long> getOrderStatusCount() {
        return orderStatusCount;
    }

    public void setOrderStatusCount(Map<String, Long> orderStatusCount) {
        this.orderStatusCount = orderStatusCount;
    }
}

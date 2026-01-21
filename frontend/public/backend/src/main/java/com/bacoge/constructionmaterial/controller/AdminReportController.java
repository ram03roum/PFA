package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.service.admin.AdminOrderService;
import com.bacoge.constructionmaterial.service.admin.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {
    private final AdminOrderService adminOrderService;
    private final AdminUserService adminUserService;

    public AdminReportController(AdminOrderService adminOrderService, AdminUserService adminUserService) {
        this.adminOrderService = adminOrderService;
        this.adminUserService = adminUserService;
    }

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);

        BigDecimal totalRevenue = adminOrderService.getTotalRevenueBetweenDates(start, end);
        long totalOrders = adminOrderService.getOrdersCountBetweenDates(start, end);
        BigDecimal avgOrderValue = (totalOrders > 0 && totalRevenue != null)
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        long newUsers = adminUserService.getUsersCountBetweenDates(start, end);
        double conversionRate = totalOrders > 0 ? (double) newUsers / totalOrders * 100.0 : 0.0;

        Map<String, Object> body = new HashMap<>();
        body.put("totalRevenue", totalRevenue);
        body.put("totalOrders", totalOrders);
        body.put("avgOrderValue", avgOrderValue);
        body.put("conversionRate", conversionRate);
        body.put("revenueByCategory", adminOrderService.getRevenueByCategoryBetween(start, end));
        body.put("ordersByCategory", adminOrderService.getOrderCountByCategoryBetween(start, end));
        body.put("revenueByPayment", adminOrderService.getRevenueByPaymentMethodBetween(start, end));
        body.put("topProducts", adminOrderService.getTopSellingProductsBetween(start, end, 5));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsersReport(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);

        long totalUsers = adminUserService.getTotalUsers();
        long newUsers = adminUserService.getUsersCountBetweenDates(start, end);
        long activeUsers = adminUserService.getUsersCountByStatus(com.bacoge.constructionmaterial.model.User.UserStatus.ACTIVE);
        double conversionRate = totalUsers > 0 ? (double) newUsers / totalUsers * 100.0 : 0.0;

        Map<String, Object> body = new HashMap<>();
        body.put("totalUsers", totalUsers);
        body.put("newUsers", newUsers);
        body.put("activeUsers", activeUsers);
        body.put("conversionRate", conversionRate);
        body.put("usersByRole", adminUserService.getUsersByRole());
        body.put("usersByRegion", adminUserService.getUsersByCity());
        body.put("topUsers", adminUserService.getTopActiveUsersBetween(start, end, 5));
        return ResponseEntity.ok(body);
    }
}


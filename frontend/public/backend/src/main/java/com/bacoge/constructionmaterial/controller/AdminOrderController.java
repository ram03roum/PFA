package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.admin.CreateOrderRequest;
import com.bacoge.constructionmaterial.dto.admin.OrderDto;
import com.bacoge.constructionmaterial.model.Order;
import com.bacoge.constructionmaterial.service.admin.AdminOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")

@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    
    private final AdminOrderService adminOrderService;
    
    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus) {
        
        Pageable pageable = PageRequest.of(page, size);
        Order.OrderStatus orderStatus = status != null ? Order.OrderStatus.valueOf(status.toUpperCase()) : null;
        Order.PaymentStatus orderPaymentStatus = paymentStatus != null ? Order.PaymentStatus.valueOf(paymentStatus.toUpperCase()) : null;
        
        Page<OrderDto> orders = adminOrderService.getAllOrders(orderNumber, userId, orderStatus, orderPaymentStatus, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders.getContent());
        response.put("currentPage", orders.getNumber());
        response.put("totalElements", orders.getTotalElements());
        response.put("totalPages", orders.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        OrderDto order = adminOrderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDto order = adminOrderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            OrderDto order = adminOrderService.createOrder(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("order", order);
            response.put("message", "Commande créée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            OrderDto order = adminOrderService.updateOrderStatus(id, orderStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("order", order);
            response.put("message", "Statut de la commande mis à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable Long id, 
            @RequestParam String paymentStatus) {
        try {
            Order.PaymentStatus orderPaymentStatus = Order.PaymentStatus.valueOf(paymentStatus.toUpperCase());
            OrderDto order = adminOrderService.updatePaymentStatus(id, orderPaymentStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("order", order);
            response.put("message", "Statut de paiement mis à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersByUser(@PathVariable Long userId) {
        List<OrderDto> orders = adminOrderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) {
        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        List<OrderDto> orders = adminOrderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/by-payment-status/{paymentStatus}")
    public ResponseEntity<List<OrderDto>> getOrdersByPaymentStatus(@PathVariable String paymentStatus) {
        Order.PaymentStatus orderPaymentStatus = Order.PaymentStatus.valueOf(paymentStatus.toUpperCase());
        List<OrderDto> orders = adminOrderService.getOrdersByPaymentStatus(orderPaymentStatus);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/between-dates")
    public ResponseEntity<?> getOrdersBetweenDates(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);
            
            List<OrderDto> orders = adminOrderService.getOrdersBetweenDates(start, end);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Format de date invalide. Utilisez yyyy-MM-dd'T'HH:mm:ss");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", adminOrderService.getTotalOrders());
        stats.put("pendingOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.PENDING));
        stats.put("confirmedOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.CONFIRMED));
        stats.put("processingOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.PROCESSING));
        stats.put("shippedOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.SHIPPED));
        stats.put("deliveredOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.DELIVERED));
        stats.put("cancelledOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.CANCELLED));
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueBetweenDates(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);
            
            BigDecimal revenue = adminOrderService.getTotalRevenueBetweenDates(start, end);
            long ordersCount = adminOrderService.getOrdersCountBetweenDates(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("revenue", revenue);
            response.put("ordersCount", ordersCount);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Format de date invalide. Utilisez yyyy-MM-dd'T'HH:mm:ss");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

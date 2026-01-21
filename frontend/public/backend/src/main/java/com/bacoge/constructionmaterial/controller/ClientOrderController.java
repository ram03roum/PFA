package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.client.CreateOrderRequest;
import com.bacoge.constructionmaterial.dto.client.OrderDisplayDto;
import com.bacoge.constructionmaterial.service.ClientOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/client/orders")

public class ClientOrderController {
    
    private final ClientOrderService clientOrderService;
    
    public ClientOrderController(ClientOrderService clientOrderService) {
        this.clientOrderService = clientOrderService;
    }
    
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            OrderDisplayDto order = clientOrderService.createOrder(request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDisplayDto> orders = clientOrderService.getUserOrders(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("orders", orders.getContent());
        response.put("currentPage", orders.getNumber());
        response.put("totalItems", orders.getTotalElements());
        response.put("totalPages", orders.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderDisplayDto> getOrderById(@PathVariable Long id) {
        OrderDisplayDto order = clientOrderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id) {
        try {
            clientOrderService.cancelOrder(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Commande annulée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/{id}/confirm-received")
    public ResponseEntity<Map<String, Object>> confirmOrderReceived(@PathVariable Long id) {
        try {
            clientOrderService.confirmOrderReceived(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Commande marquée comme livrée. Merci pour votre confirmation !");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/api/history")
    public ResponseEntity<java.util.List<OrderDisplayDto>> getOrderHistory() {
        java.util.List<OrderDisplayDto> orders = clientOrderService.getOrderHistory();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/api/{id}")
    public ResponseEntity<OrderDisplayDto> getOrderDetailsForHistory(@PathVariable Long id) {
        OrderDisplayDto order = clientOrderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/api/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id) {
        try {
            byte[] invoiceData = clientOrderService.generateInvoice(id);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=facture-" + id + ".pdf")
                    .body(invoiceData);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

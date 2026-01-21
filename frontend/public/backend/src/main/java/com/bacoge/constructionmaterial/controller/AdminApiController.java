package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.admin.OrderDto;
import com.bacoge.constructionmaterial.dto.admin.ProductDto;
import com.bacoge.constructionmaterial.dto.admin.PromotionDto;
import com.bacoge.constructionmaterial.dto.admin.UserDto;
import com.bacoge.constructionmaterial.service.admin.AdminOrderService;
import com.bacoge.constructionmaterial.service.admin.AdminProductService;
import com.bacoge.constructionmaterial.service.admin.AdminPromotionService;
import com.bacoge.constructionmaterial.service.admin.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api")
@PreAuthorize("hasRole('ADMIN')")

public class AdminApiController {
    
    private final AdminUserService adminUserService;
    private final AdminProductService adminProductService;
    private final AdminPromotionService adminPromotionService;
    private final AdminOrderService adminOrderService;
    
    public AdminApiController(AdminUserService adminUserService, 
                             AdminProductService adminProductService,
                             AdminPromotionService adminPromotionService,
                             AdminOrderService adminOrderService) {
        this.adminUserService = adminUserService;
        this.adminProductService = adminProductService;
        this.adminPromotionService = adminPromotionService;
        this.adminOrderService = adminOrderService;
    }
    
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> users = adminUserService.getAllUsers(null, null, null, null, null, pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> products = adminProductService.getAllProducts(null, null, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/promotions")
    public ResponseEntity<Page<PromotionDto>> getPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PromotionDto> promotions = adminPromotionService.getAllPromotions(name, status, pageable);
        return ResponseEntity.ok(promotions);
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDto>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDto> orders = adminOrderService.getAllOrders(null, null, null, null, pageable);
        return ResponseEntity.ok(orders);
    }
}

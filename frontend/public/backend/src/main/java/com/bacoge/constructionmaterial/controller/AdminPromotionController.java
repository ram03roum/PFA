package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.admin.CreatePromotionRequest;
import com.bacoge.constructionmaterial.dto.admin.PromotionDto;
import com.bacoge.constructionmaterial.model.Promotion;
import com.bacoge.constructionmaterial.service.admin.AdminPromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotions")

@PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionController {
    
    private final AdminPromotionService adminPromotionService;
    
    public AdminPromotionController(AdminPromotionService adminPromotionService) {
        this.adminPromotionService = adminPromotionService;
    }
    
    @GetMapping
    public ResponseEntity<Page<PromotionDto>> getAllPromotionsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PromotionDto> promotions = adminPromotionService.getAllPromotions(name, status, pageable);
        return ResponseEntity.ok(promotions);
    }

    // Backward-compatible list (if needed elsewhere)
    @GetMapping("/list")
    public ResponseEntity<List<PromotionDto>> getAllPromotions() {
        List<PromotionDto> promotions = adminPromotionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<com.bacoge.constructionmaterial.dto.client.PromotionDto>> getActivePromotions() {
        List<com.bacoge.constructionmaterial.dto.client.PromotionDto> promotions = adminPromotionService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }
    
    @GetMapping("/valid")
    public ResponseEntity<List<PromotionDto>> getValidPromotions() {
        List<PromotionDto> promotions = adminPromotionService.getValidPromotions();
        return ResponseEntity.ok(promotions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PromotionDto> getPromotionById(@PathVariable Long id) {
        PromotionDto promotion = adminPromotionService.getPromotionById(id);
        return ResponseEntity.ok(promotion);
    }
    
    @GetMapping("/code/{promoCode}")
    public ResponseEntity<PromotionDto> getPromotionByCode(@PathVariable String promoCode) {
        PromotionDto promotion = adminPromotionService.getPromotionByCode(promoCode);
        return ResponseEntity.ok(promotion);
    }
    
    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Map<String, Object>> createPromotion(@ModelAttribute CreatePromotionRequest request) {
        try {
            PromotionDto promotion = adminPromotionService.createPromotion(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", promotion);
            response.put("message", "Promotion créée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePromotion(@PathVariable Long id, @RequestBody CreatePromotionRequest request) {
        try {
            PromotionDto promotion = adminPromotionService.updatePromotion(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", promotion);
            response.put("message", "Promotion mise à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePromotion(@PathVariable Long id) {
        try {
            adminPromotionService.deletePromotion(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Promotion supprimée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updatePromotionStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            Promotion.PromotionStatus promotionStatus = Promotion.PromotionStatus.valueOf(status.toUpperCase());
            PromotionDto promotion = adminPromotionService.updatePromotionStatus(id, promotionStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("promotion", promotion);
            response.put("message", "Statut de la promotion mis à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<PromotionDto>> searchPromotions(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        Promotion.PromotionStatus promotionStatus = status != null ? Promotion.PromotionStatus.valueOf(status.toUpperCase()) : null;
        List<PromotionDto> promotions = adminPromotionService.searchPromotions(name, promotionStatus);
        return ResponseEntity.ok(promotions);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPromotionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPromotions", adminPromotionService.getTotalPromotions());
        stats.put("activePromotions", adminPromotionService.getActivePromotionsCount());
        stats.put("inactivePromotions", adminPromotionService.getPromotionsCountByStatus(Promotion.PromotionStatus.INACTIVE));
        
        return ResponseEntity.ok(stats);
    }
}

package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.service.admin.AdminPromotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")

public class PublicPromotionController {
    
    private final AdminPromotionService adminPromotionService;
    
    public PublicPromotionController(AdminPromotionService adminPromotionService) {
        this.adminPromotionService = adminPromotionService;
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<com.bacoge.constructionmaterial.dto.client.PromotionDto>> getActivePromotions() {
        List<com.bacoge.constructionmaterial.dto.client.PromotionDto> promotions = adminPromotionService.getActivePromotions();
        return ResponseEntity.ok(promotions);
    }
}

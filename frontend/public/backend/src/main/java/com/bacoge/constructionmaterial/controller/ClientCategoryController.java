package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.client.CategoryDisplayDto;
import com.bacoge.constructionmaterial.service.ClientCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client/categories")

public class ClientCategoryController {
    
    private final ClientCategoryService clientCategoryService;
    
    public ClientCategoryController(ClientCategoryService clientCategoryService) {
        this.clientCategoryService = clientCategoryService;
    }
    
    @GetMapping
    public ResponseEntity<List<CategoryDisplayDto>> getAllActiveCategories() {
        List<CategoryDisplayDto> categories = clientCategoryService.getAllActiveCategoryDisplays();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<CategoryDisplayDto>> getActiveCategories() {
        // Alias for getAllActiveCategories to support legacy endpoints
        return getAllActiveCategories();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDisplayDto> getCategoryById(@PathVariable Long id) {
        CategoryDisplayDto category = CategoryDisplayDto.fromCategory(clientCategoryService.getCategoryById(id));
        return ResponseEntity.ok(category);
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<CategoryDisplayDto>> getFeaturedCategories() {
        List<CategoryDisplayDto> categories = clientCategoryService.getFeaturedCategories()
                .stream()
                .map(CategoryDisplayDto::fromCategory)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(categories);
    }
} 

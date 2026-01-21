package com.bacoge.constructionmaterial.controller.api;

import com.bacoge.constructionmaterial.dto.client.CategoryDisplayDto;
import com.bacoge.constructionmaterial.service.ClientCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")

public class CategoryApiController {
    
    private final ClientCategoryService clientCategoryService;
    
    public CategoryApiController(ClientCategoryService clientCategoryService) {
        this.clientCategoryService = clientCategoryService;
    }
    
    @GetMapping
    public ResponseEntity<List<CategoryDisplayDto>> getAllCategories() {
        List<CategoryDisplayDto> categories = clientCategoryService.getAllActiveCategoryDisplays();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDisplayDto> getCategoryById(@PathVariable Long id) {
        CategoryDisplayDto category = CategoryDisplayDto.fromCategory(clientCategoryService.getCategoryById(id));
        return ResponseEntity.ok(category);
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<CategoryDisplayDto>> getFeaturedCategories() {
        List<CategoryDisplayDto> categories = clientCategoryService.getAllActiveCategoryDisplays();
        return ResponseEntity.ok(categories);
    }
}

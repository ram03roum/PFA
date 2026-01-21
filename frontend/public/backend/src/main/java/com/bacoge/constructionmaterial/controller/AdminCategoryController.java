package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.admin.CategoryDto;
import com.bacoge.constructionmaterial.dto.admin.CreateCategoryRequest;
import com.bacoge.constructionmaterial.model.Category;
import com.bacoge.constructionmaterial.service.admin.AdminCategoryService;
import com.bacoge.constructionmaterial.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")

@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryController.class);
    
    private final AdminCategoryService adminCategoryService;
    private final FileStorageService fileStorageService;
    
    @Autowired
    public AdminCategoryController(AdminCategoryService adminCategoryService, 
                                 FileStorageService fileStorageService) {
        this.adminCategoryService = adminCategoryService;
        this.fileStorageService = fileStorageService;
    }
    
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<CategoryDto> categories = adminCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<CategoryDto>> getActiveCategories() {
        List<CategoryDto> categories = adminCategoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        CategoryDto category = adminCategoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        logger.info("Début de la création d'une catégorie - Nom: {}", name);
        try {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName(name);
            request.setDescription(description);
            request.setStatus(status);
            
            if (image != null && !image.isEmpty()) {
                String fileName = fileStorageService.storeFile(image, "categories");
                request.setImageUrl("/uploads/" + fileName);
            }
            
            CategoryDto category = adminCategoryService.createCategory(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("message", "Catégorie créée avec succès");
            
            logger.info("Catégorie créée avec succès - ID: {}", category.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "status", required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            CreateCategoryRequest request = new CreateCategoryRequest();
            request.setName(name);
            request.setDescription(description);
            request.setStatus(status);
            
            // Handle image upload if a new image is provided
            if (image != null && !image.isEmpty()) {
                // Tenter de supprimer l'ancienne image si elle existe
                try {
                    CategoryDto oldCategory = adminCategoryService.getCategoryById(id);
                    String oldImageUrl = oldCategory.getImageUrl();
                    if (oldImageUrl != null && oldImageUrl.contains("/uploads/")) {
                        String filename = oldImageUrl.substring(oldImageUrl.indexOf("/uploads/") + 9);
                        fileStorageService.deleteFile(filename);
                    }
                } catch (Exception e) {
                    logger.warn("Impossible de récupérer l'ancienne image pour suppression: {}", e.getMessage());
                }

                String fileName = fileStorageService.storeFile(image, "categories");
                request.setImageUrl("/uploads/" + fileName);
            }
            
            CategoryDto category = adminCategoryService.updateCategory(id, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("message", "Catégorie mise à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
            adminCategoryService.deleteCategory(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Catégorie supprimée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateCategoryStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            Category.CategoryStatus categoryStatus = Category.CategoryStatus.valueOf(status.toUpperCase());
            CategoryDto category = adminCategoryService.updateCategoryStatus(id, categoryStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("message", "Statut de la catégorie mis à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDto>> searchCategories(@RequestParam String name) {
        List<CategoryDto> categories = adminCategoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCategories", adminCategoryService.getTotalCategories());
        stats.put("activeCategories", adminCategoryService.getActiveCategoriesCount());
        
        return ResponseEntity.ok(stats);
    }
}

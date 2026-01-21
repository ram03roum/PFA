package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.admin.CreateProductRequest;
import com.bacoge.constructionmaterial.dto.admin.ProductDto;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.service.admin.AdminProductService;
import com.bacoge.constructionmaterial.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin/products")

@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final AdminProductService adminProductService;
    private final FileStorageService fileStorageService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        
        try {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Product.ProductStatus productStatus = null;
            if (status != null && !status.trim().isEmpty() && !"all".equals(status) && !"null".equals(status)) {
                try {
                    productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Ignorer les status invalides
                }
            }
            
            Page<ProductDto> products = adminProductService.getAllProducts(search, productStatus, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("currentPage", products.getNumber());
            response.put("totalItems", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des produits: " + e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        try {
            // Validation de l'ID
            if (id == null || id <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "ID du produit invalide");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            ProductDto product = adminProductService.getProductById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("product", product);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur interne du serveur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestParam("name") @NotBlank(message = "Le nom du produit est obligatoire") String name,
            @RequestParam("description") @NotBlank(message = "La description est obligatoire") String description,
            @RequestParam(value = "longDescription", required = false) @Size(max = 65535, message = "La description longue ne peut pas dépasser 65535 caractères") String longDescription,
            @RequestParam("price") @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0") BigDecimal price,
            @RequestParam("stockQuantity") @Min(value = 0, message = "La quantité en stock ne peut pas être négative") int stockQuantity,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "sku", required = false) String sku,
            @RequestParam(value = "weightKg", required = false) @DecimalMin(value = "0.0", message = "Le poids doit être positif") Double weightKg,
            @RequestParam(value = "dimensions", required = false) String dimensions,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "style", required = false) String style,
            @RequestParam(value = "room", required = false) String room,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "material", required = false) String material,
            @RequestParam(value = "collectionName", required = false) String collectionName,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "status", defaultValue = "ACTIVE") String status,
            @RequestParam(value = "minStockLevel", required = false) @Min(value = 0, message = "Le stock minimum ne peut pas être négatif") Integer minStockLevel,
            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles) {

        log.info("=== Tentative de création d'un nouveau produit ===");
        log.info("Nom du produit: {}", name);
        log.info("Prix: {}", price);
        log.info("Quantité en stock: {}", stockQuantity);
        log.info("Statut: {}", status);
        
        try {
            // Valider le statut
            Product.ProductStatus productStatus;
            try {
                productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Statut de produit invalide: " + status);
            }
            
            // Créer la requête
            CreateProductRequest request = new CreateProductRequest();
            request.setName(name.trim());
            request.setDescription(description.trim());
            request.setLongDescription(longDescription);
            request.setPrice(price);
            request.setStockQuantity(stockQuantity);
            request.setCategoryId(categoryId);
            request.setSku(sku != null ? sku.trim() : null);
            request.setWeightKg(weightKg);
            request.setDimensions(dimensions != null ? dimensions.trim() : null);
            request.setBrand(brand);
            request.setStyle(style);
            request.setRoom(room);
            request.setColor(color);
            request.setMaterial(material);
            request.setCollectionName(collectionName);
            request.setTags(tags);
            request.setStatusEnum(productStatus);
            request.setMinStockLevel(minStockLevel);
            
            // Traiter les images
            if (imageFiles != null && !imageFiles.isEmpty()) {
                try {
                    List<String> savedFilenames = fileStorageService.storeMultipleFiles(imageFiles);
                    List<String> imageUrls = savedFilenames.stream()
                            .map(fileStorageService::getFileUrl)
                            .collect(Collectors.toList());
                    request.setImageUrls(imageUrls);
                    log.info("{} images enregistrées pour le produit: {}", savedFilenames.size(), name);
                } catch (IOException e) {
                    log.error("Erreur lors de l'enregistrement des images: {}", e.getMessage(), e);
                    throw new RuntimeException("Erreur lors de l'enregistrement des images: " + e.getMessage(), e);
                }
            }
            
            // Créer le produit
            ProductDto product = adminProductService.createProduct(request);
            log.info("Produit créé avec succès - ID: {}, Nom: {}", product.getId(), product.getName());
            
            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("product", product);
            response.put("message", "Produit créé avec succès");
            
            log.info("Réponse JSON préparée avec succès pour le produit ID: {}", product.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors de la création du produit: {}", e.getMessage());
            throw e; // handled by GlobalExceptionHandler as 400
        } catch (ConstraintViolationException e) {
            log.warn("Violation de contrainte lors de la création du produit: {}", e.getMessage());
            throw e; // handled by GlobalExceptionHandler as 400
        } catch (DataIntegrityViolationException e) {
            log.warn("Violation d'intégrité des données (ex. contrainte unique) lors de la création du produit: {}", e.getMessage());
            throw e; // handled by GlobalExceptionHandler
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la création du produit: {}", e.getMessage(), e);
            // Laisser gérer par GlobalExceptionHandler pour retourner un JSON d'erreur cohérent
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        }
    }
    
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable @NotNull(message = "L'ID du produit est obligatoire") Long id,
            @RequestParam("name") @NotBlank(message = "Le nom du produit est obligatoire") String name,
            @RequestParam("description") @NotBlank(message = "La description est obligatoire") String description,
            @RequestParam(value = "longDescription", required = false) @Size(max = 65535, message = "La description longue ne peut pas dépasser 65535 caractères") String longDescription,
            @RequestParam("price") @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0") BigDecimal price,
            @RequestParam("stockQuantity") @Min(value = 0, message = "La quantité en stock ne peut pas être négative") int stockQuantity,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "sku", required = false) String sku,
            @RequestParam(value = "weightKg", required = false) @DecimalMin(value = "0.0", message = "Le poids doit être positif") Double weightKg,
            @RequestParam(value = "dimensions", required = false) String dimensions,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "style", required = false) String style,
            @RequestParam(value = "room", required = false) String room,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "material", required = false) String material,
            @RequestParam(value = "collectionName", required = false) String collectionName,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "status", defaultValue = "DRAFT") String status,
            @RequestParam(value = "minStockLevel", required = false) @Min(value = 0, message = "Le stock minimum ne peut pas être négatif") Integer minStockLevel,
            @RequestParam(value = "images", required = false) List<MultipartFile> newImageFiles,
            @RequestParam(value = "existingImages", required = false) List<String> existingImageUrls,
            @RequestParam(value = "deletedImages", required = false) List<String> deletedImageUrls) {
        
        log.info("Tentative de mise à jour du produit ID: {}", id);
        
        // Validation de l'ID
        if (id == null || id <= 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "ID du produit invalide");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // Valider le statut
            Product.ProductStatus productStatus;
            try {
                productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Statut de produit invalide: " + status);
            }
            
            // Créer la requête
            CreateProductRequest request = new CreateProductRequest();
            request.setName(name.trim());
            request.setDescription(description.trim());
            request.setLongDescription(longDescription);
            request.setPrice(price);
            request.setStockQuantity(stockQuantity);
            request.setCategoryId(categoryId);
            request.setSku(sku != null ? sku.trim() : null);
            request.setWeightKg(weightKg);
            request.setDimensions(dimensions != null ? dimensions.trim() : null);
            request.setBrand(brand);
            request.setStyle(style);
            request.setRoom(room);
            request.setColor(color);
            request.setMaterial(material);
            request.setCollectionName(collectionName);
            request.setTags(tags);
            request.setStatusEnum(productStatus);
            request.setMinStockLevel(minStockLevel);
            
            // Gérer les images existantes
            List<String> allImageUrls = new ArrayList<>();
            
            // Ajouter les images existantes non supprimées
            if (existingImageUrls != null) {
                allImageUrls.addAll(existingImageUrls);
                log.debug("Images existantes conservées: {}", existingImageUrls.size());
            }
            
            // Ajouter les nouvelles images
            if (newImageFiles != null && !newImageFiles.isEmpty()) {
                try {
                    List<String> savedFilenames = fileStorageService.storeMultipleFiles(newImageFiles);
                    List<String> newImageUrls = savedFilenames.stream()
                            .map(fileStorageService::getFileUrl)
                            .collect(Collectors.toList());
                    allImageUrls.addAll(newImageUrls);
                    log.info("{} nouvelles images ajoutées au produit ID: {}", newImageUrls.size(), id);
                } catch (IOException e) {
                    log.error("Erreur lors de l'enregistrement des nouvelles images: {}", e.getMessage(), e);
                    throw new RuntimeException("Erreur lors de l'enregistrement des nouvelles images", e);
                }
            }
            
            // Supprimer les images marquées pour suppression
            if (deletedImageUrls != null && !deletedImageUrls.isEmpty()) {
                deletedImageUrls.forEach(url -> {
                    String filename = url.substring(url.lastIndexOf('/') + 1);
                    if (fileStorageService.deleteFile(filename)) {
                        log.debug("Image supprimée: {}", filename);
                    }
                });
                log.info("{} images supprimées pour le produit ID: {}", deletedImageUrls.size(), id);
            }
            
            // Mettre à jour la liste des images du produit
            request.setImageUrls(allImageUrls);
            
            // Mettre à jour le produit
            ProductDto updatedProduct = adminProductService.updateProduct(id, request);
            log.info("Produit mis à jour avec succès - ID: {}", id);
            
            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("product", updatedProduct);
            response.put("message", "Produit mis à jour avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors de la mise à jour du produit ID {}: {}", id, e.getMessage());
            throw e; // Laissé à la gestion des exceptions globales
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour du produit ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Une erreur est survenue lors de la mise à jour du produit", e);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        try {
            // Validation de l'ID
            if (id == null || id <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "ID du produit invalide");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            adminProductService.deleteProduct(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Produit supprimé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la suppression: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateProductStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            if (id == null || id <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "ID du produit invalide");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Product.ProductStatus productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
            ProductDto product = adminProductService.updateProductStatus(id, productStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("product", product);
            response.put("message", "Statut du produit mis à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Statut invalide: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la mise à jour du statut: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(
            @PathVariable Long id, 
            @RequestParam Integer stockQuantity) {
        try {
            if (id == null || id <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "ID du produit invalide");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            ProductDto product = adminProductService.updateStock(id, stockQuantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("product", product);
            response.put("message", "Stock mis à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la mise à jour du stock: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable Long categoryId) {
        try {
            if (categoryId == null || categoryId <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            List<ProductDto> products = adminProductService.getProductsByCategory(categoryId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<ProductDto>> getProductsByStatus(@PathVariable String status) {
        try {
            Product.ProductStatus productStatus = Product.ProductStatus.valueOf(status.toUpperCase());
            List<ProductDto> products = adminProductService.getProductsByStatus(productStatus);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDto>> getLowStockProducts() {
        try {
            List<ProductDto> products = adminProductService.getLowStockProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", adminProductService.getTotalProducts());
            stats.put("activeProducts", adminProductService.getProductsCountByStatus(Product.ProductStatus.ACTIVE));
            stats.put("inactiveProducts", adminProductService.getProductsCountByStatus(Product.ProductStatus.INACTIVE));
            stats.put("outOfStockProducts", adminProductService.getProductsCountByStatus(Product.ProductStatus.OUT_OF_STOCK));
            stats.put("totalStockQuantity", adminProductService.getTotalStockQuantity());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des statistiques");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

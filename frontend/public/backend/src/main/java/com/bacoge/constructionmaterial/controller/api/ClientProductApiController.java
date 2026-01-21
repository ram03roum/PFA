package com.bacoge.constructionmaterial.controller.api;

import com.bacoge.constructionmaterial.dto.client.ProductDisplayDto;
import com.bacoge.constructionmaterial.service.ClientProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")

public class ClientProductApiController {
    
    private final ClientProductService clientProductService;
    
    public ClientProductApiController(ClientProductService clientProductService) {
        this.clientProductService = clientProductService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String material,
            @RequestParam(required = false, name = "collectionName") String collectionName,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));
            
            Page<ProductDisplayDto> products = clientProductService.getActiveProducts(
                search,
                category,
                minPrice != null ? BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? BigDecimal.valueOf(maxPrice) : null,
                style,
                room,
                color,
                material,
                collectionName,
                tags,
                sortBy,
                sortOrder,
                pageable,
                null
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("products", products.getContent());
            response.put("currentPage", products.getNumber());
            response.put("totalPages", products.getTotalPages());
            response.put("totalElements", products.getTotalElements());
            response.put("size", products.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération des produits");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDisplayDto> getProductById(@PathVariable Long id) {
        try {
            ProductDisplayDto product = clientProductService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedProducts() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("products", clientProductService.getFeaturedProducts());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération des produits en vedette");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/new")
    public ResponseEntity<Map<String, Object>> getNewArrivals() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("products", clientProductService.getNewArrivals());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération des nouveaux produits");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/related/{id}")
    public ResponseEntity<List<ProductDisplayDto>> getRelatedProducts(@PathVariable Long id) {
        try {
            List<ProductDisplayDto> relatedProducts = clientProductService.getRelatedProducts(id);
            return ResponseEntity.ok(relatedProducts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

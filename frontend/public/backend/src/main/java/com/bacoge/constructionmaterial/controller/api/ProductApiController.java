package com.bacoge.constructionmaterial.controller.api;

import com.bacoge.constructionmaterial.dto.ProductRequest;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.ProductImage;
import com.bacoge.constructionmaterial.service.ProductImageService;
import com.bacoge.constructionmaterial.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin/products-v2")
public class ProductApiController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    public ProductApiController(ProductService productService, ProductImageService productImageService) {
        this.productService = productService;
        this.productImageService = productImageService;
    }

    @PostMapping(value = "/v2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProductV2(@ModelAttribute ProductRequest productRequest) {
        log.info("=== DEBUT createProduct ===");
        log.info("ProductRequest reçu: {}", productRequest);
        log.info("Détails du produit - Nom: {}, Prix: {}, Catégorie: {}", 
                productRequest.getName(), productRequest.getPrice(), productRequest.getCategoryId());
        
        // Log all fields for debugging
        log.info("Champs du produit:");
        log.info("- name: {}", productRequest.getName());
        log.info("- price: {}", productRequest.getPrice());
        log.info("- categoryId: {}", productRequest.getCategoryId());
        log.info("- description: {}", productRequest.getDescription());
        log.info("- stockQuantity: {}", productRequest.getStockQuantity());
        log.info("- status: {}", productRequest.getStatus());
        
        if (productRequest.getImages() != null) {
            log.info("Images reçues: {} fichier(s)", productRequest.getImages().size());
            for (int i = 0; i < productRequest.getImages().size(); i++) {
                var file = productRequest.getImages().get(i);
                log.info("  Image {}: {} ({} bytes, type: {})", 
                    i, file.getOriginalFilename(), file.getSize(), file.getContentType());
            }
        } else {
            log.warn("Aucune image reçue dans la requête");
        }
        
        try {
            log.info("Appel à productService.createProduct...");
            Product product = productService.createProduct(productRequest);
            log.info("Produit créé avec succès. ID: {}", product.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (Exception e) {
            log.error("ERREUR lors de la création du produit", e);
            log.error("Message d'erreur: {}", e.getMessage());
            log.error("Type d'exception: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.error("Cause: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "status", "error",
                        "message", "Erreur lors de la création du produit: " + e.getMessage(),
                        "errorType", e.getClass().getSimpleName()
                    ));
        } finally {
            log.info("=== FIN createProduct ===");
        }
    }

    @PutMapping(value = "/v2/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductV2(
            @PathVariable Long id,
            @ModelAttribute ProductRequest productRequest) {
        log.info("Mise à jour du produit ID: {}", id);
        try {
            Product updatedProduct = productService.updateProduct(id, productRequest);
            if (updatedProduct != null) {
                return ResponseEntity.ok(updatedProduct);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du produit ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour du produit: " + e.getMessage());
        }
    }

    @GetMapping("/v2/{id}")
    public ResponseEntity<?> getProductV2(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/v2/{id}")
    public ResponseEntity<?> deleteProductV2(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du produit: " + e.getMessage());
        }
    }

    @GetMapping("/v2/{productId}/images")
    public ResponseEntity<List<ProductImage>> getProductImagesV2(@PathVariable Long productId) {
        List<ProductImage> images = productImageService.findByProductId(productId);
        return ResponseEntity.ok(images);
    }

    @PostMapping("/v2/{productId}/images/{imageId}/set-main")
    public ResponseEntity<?> setMainImageV2(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        try {
            productImageService.setMainImage(productId, imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la définition de l'image principale: " + e.getMessage());
        }
    }

    @DeleteMapping("/v2/images/{imageId}")
    public ResponseEntity<?> deleteImageV2(@PathVariable Long imageId) {
        try {
            productImageService.deleteImage(imageId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression de l'image: " + e.getMessage());
        }
    }
}

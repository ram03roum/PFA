package com.bacoge.constructionmaterial.service.admin;

import com.bacoge.constructionmaterial.dto.admin.CreateProductRequest;
import com.bacoge.constructionmaterial.dto.admin.ProductDto;
import com.bacoge.constructionmaterial.dto.client.ProductDisplayDto;
import com.bacoge.constructionmaterial.dto.client.ProductPromotionDto;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.Category;
import com.bacoge.constructionmaterial.model.ProductImage;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    public AdminProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    
    public Page<ProductDto> getAllProducts(String search, Product.ProductStatus status, Pageable pageable) {
        try {
            Page<Product> products;

            if (search != null && !search.trim().isEmpty()) {
                // Recherche par nom ou style
                products = productRepository.findByNameContainingIgnoreCaseOrStyleContainingIgnoreCase(search, pageable);
            } else if (status != null) {
                // Filtrage par statut seulement
                products = productRepository.findByStatus(status, pageable);
            } else {
                // Tous les produits
                products = productRepository.findAllWithImages(pageable);
            }

            return products.map(this::convertToDto);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des produits: " + e.getMessage());
            e.printStackTrace();
            // Return empty page instead of throwing exception
            return Page.empty(pageable);
        }
    }
    
    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findByIdWithPromotions(id)
                .orElseGet(() -> productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id)));
        return convertToDto(product);
    }
    
    public ProductDto createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setLongDescription(request.getLongDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable pour l'ID: " + request.getCategoryId()));
            product.setCategory(category);
        }
        product.setSku(request.getSku());
        product.setWeightKg(request.getWeightKg() != null ? BigDecimal.valueOf(request.getWeightKg()) : null);
        product.setDimensions(request.getDimensions());
        product.setBrand(request.getBrand());
        // Interior design fields
        product.setStyle(request.getStyle());
        product.setRoom(request.getRoom());
        product.setColor(request.getColor());
        product.setMaterial(request.getMaterial());
        product.setCollectionName(request.getCollectionName());
        product.setTags(request.getTags());
        product.setStatus(request.getStatusEnum());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setCreatedAt(LocalDateTime.now());
        
        // Sauvegarder d'abord le produit pour obtenir l'ID
        Product savedProduct = productRepository.save(product);
        
        // Ajouter les images si elles existent
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                String imageUrl = request.getImageUrls().get(i);
                ProductImage productImage = new ProductImage();
                productImage.setImageUrl(imageUrl);
                productImage.setIsMain(i == 0); // La première image est l'image principale
                productImage.setDisplayOrder(i + 1);
                productImage.setProduct(savedProduct);
                
                savedProduct.addImage(productImage);
            }
            
            // Sauvegarder à nouveau pour persister les images
            savedProduct = productRepository.save(savedProduct);
        }
        
        return convertToDto(savedProduct);
    }
    
    public ProductDto updateProduct(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setLongDescription(request.getLongDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        // Set category if provided (allow clearing when null)
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable pour l'ID: " + request.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }
        product.setSku(request.getSku());
        product.setWeightKg(request.getWeightKg() != null ? BigDecimal.valueOf(request.getWeightKg()) : null);
        product.setDimensions(request.getDimensions());
        product.setBrand(request.getBrand());
        // Interior design fields
        product.setStyle(request.getStyle());
        product.setRoom(request.getRoom());
        product.setColor(request.getColor());
        product.setMaterial(request.getMaterial());
        product.setCollectionName(request.getCollectionName());
        product.setTags(request.getTags());
        product.setStatus(request.getStatusEnum());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setUpdatedAt(LocalDateTime.now());
        
        // Gérer les images lors de la mise à jour
        if (request.getImageUrls() != null) {
            // Supprimer toutes les images existantes
            product.getImages().clear();
            
            // Ajouter les nouvelles images
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                String imageUrl = request.getImageUrls().get(i);
                ProductImage productImage = new ProductImage();
                productImage.setImageUrl(imageUrl);
                productImage.setIsMain(i == 0); // La première image est l'image principale
                productImage.setDisplayOrder(i + 1);
                productImage.setProduct(product);
                
                product.addImage(productImage);
            }
        }
        
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }
    
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
    
    public ProductDto updateProductStatus(Long id, Product.ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }
    
    public ProductDto updateStock(Long id, Integer stockQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setStockQuantity(stockQuantity);
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }
    
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return productRepository.findAll().stream()
                .filter(product -> product.getCategoryId() != null && product.getCategoryId().equals(categoryId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductDto> getProductsByStatus(Product.ProductStatus status) {
        return productRepository.findAll().stream()
                .filter(product -> product.getStatus() == status)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductDto> getLowStockProducts() {
        return productRepository.findAll().stream()
                .filter(product -> product.getMinStockLevel() != null && 
                                 product.getStockQuantity() <= product.getMinStockLevel())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public long getTotalProducts() {
        return productRepository.count();
    }
    
    public long getProductsCountByStatus(Product.ProductStatus status) {
        return productRepository.findAll().stream()
                .filter(product -> product.getStatus() == status)
                .count();
    }
    
    public long getTotalStockQuantity() {
        return productRepository.findAll().stream()
                .mapToLong(Product::getStockQuantity)
                .sum();
    }
    
    public List<ProductDto> searchProducts(String name, Long categoryId, Product.ProductStatus status, 
                                          BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findAll().stream()
                .filter(product -> name == null || product.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(product -> categoryId == null || product.getCategoryId().equals(categoryId))
                .filter(product -> status == null || product.getStatus() == status)
                .filter(product -> minPrice == null || product.getPrice().compareTo(minPrice) >= 0)
                .filter(product -> maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setLongDescription(product.getLongDescription());
        dto.setPrice(product.getPrice());
        try {
            dto.setDiscountPrice(product.getDiscountedPrice());
        } catch (Exception ignored) {
            dto.setDiscountPrice(null);
        }
        dto.setStockQuantity(product.getStockQuantity());
        dto.setMinStockLevel(product.getMinStockLevel());
        dto.setCategoryId(product.getCategoryId());
        dto.setSku(product.getSku());
        dto.setWeightKg(product.getWeightKg());
        dto.setDimensions(product.getDimensions());
        dto.setBrand(product.getBrand());
        // Interior design fields
        dto.setStyle(product.getStyle());
        dto.setRoom(product.getRoom());
        dto.setColor(product.getColor());
        dto.setMaterial(product.getMaterial());
        dto.setCollectionName(product.getCollectionName());
        dto.setTags(product.getTags());
        dto.setStatus(product.getStatus().name());
        // Category name (defensive against lazy loading)
        try {
            if (product.getCategory() != null) {
                dto.setCategoryName(product.getCategory().getName());
            }
        } catch (Exception ignored) {}

        // Handle images with defensive programming
        try {
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                ProductImage mainImage = product.getImages().stream()
                        .filter(ProductImage::getIsMain)
                        .findFirst()
                        .orElse(product.getImages().get(0)); // Fallback to first image if no main image
                dto.setImageUrl(mainImage.getImageUrl());
                // Additional images (excluding main)
                dto.setAdditionalImages(
                        product.getImages().stream()
                                .filter(img -> !Boolean.TRUE.equals(img.getIsMain()))
                                .map(ProductImage::getImageUrl)
                                .collect(Collectors.toList())
                );
            } else {
                dto.setImageUrl(null); // No image available
                dto.setAdditionalImages(java.util.Collections.emptyList());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'accès aux images du produit " + product.getId() + ": " + e.getMessage());
            dto.setImageUrl(null);
            dto.setAdditionalImages(java.util.Collections.emptyList());
        }

        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<ProductDisplayDto> getPromotionalProducts() {
        try {
            return productRepository.findProductsWithActivePromotions(LocalDateTime.now()).stream()
                    .map(this::convertToDisplayDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Erreur getPromotionalProducts: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    private ProductDisplayDto convertToDisplayDto(Product product) {
        ProductDisplayDto dto = new ProductDisplayDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountedPrice(product.getDiscountedPrice());
        dto.setCurrency("MAD"); // Default currency
        dto.setStockQuantity(product.getStockQuantity());
        dto.setInStock(product.getStockQuantity() > 0);
        dto.setLowStock(product.getStockQuantity() < 10);
        dto.setImageUrl(product.getImageUrl());
        dto.setSku(product.getSku());
        dto.setWeightKg(product.getWeightKg());
        dto.setDimensions(product.getDimensions());
        dto.setBrand(product.getBrand());
        dto.setStatus(product.getStatus() != null ? product.getStatus().name() : null);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setAverageRating(0.0); // Default value, should be calculated
        dto.setReviewCount(0); // Default value
        
        // Set promotion if exists
        if (product.getPromotion() != null) {
            ProductPromotionDto promotionDto = new ProductPromotionDto();
            promotionDto.setId(product.getPromotion().getId());
            promotionDto.setName(product.getPromotion().getName());
            promotionDto.setDescription(product.getPromotion().getDescription());
            promotionDto.setDiscountPercentage(product.getPromotion().getDiscountPercentage());
            promotionDto.setDiscountAmount(product.getPromotion().getDiscountAmount());
            promotionDto.setDiscountType(product.getPromotion().getDiscountType() != null ? 
                product.getPromotion().getDiscountType().name() : null);
            dto.setPromotion(promotionDto);
        }
        
        // Set category
        if (product.getCategory() != null) {
            dto.setCategory(product.getCategory().getName());
            dto.setCategoryId(product.getCategory().getId());
        }
        
        return dto;
    }
    
    public static class ProductStatsDto {
        private Long totalProducts;
        private Long activeProducts;
        private Long inactiveProducts;
        private Long lowStockProducts;
        
        public ProductStatsDto() {}
        
        public ProductStatsDto(Long totalProducts, Long activeProducts, Long inactiveProducts, Long lowStockProducts) {
            this.totalProducts = totalProducts;
            this.activeProducts = activeProducts;
            this.inactiveProducts = inactiveProducts;
            this.lowStockProducts = lowStockProducts;
        }
        
        // Getters and setters
        public Long getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Long totalProducts) { this.totalProducts = totalProducts; }
        
        public Long getActiveProducts() { return activeProducts; }
        public void setActiveProducts(Long activeProducts) { this.activeProducts = activeProducts; }
        
        public Long getInactiveProducts() { return inactiveProducts; }
        public void setInactiveProducts(Long inactiveProducts) { this.inactiveProducts = inactiveProducts; }
        
        public Long getLowStockProducts() { return lowStockProducts; }
        public void setLowStockProducts(Long lowStockProducts) { this.lowStockProducts = lowStockProducts; }
    }
}

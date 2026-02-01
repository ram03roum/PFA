package com.bacoge.constructionmaterial.dto.client;

import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.Promotion;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDisplayDto {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private String currency;
    private Integer stockQuantity;
    private Boolean inStock;
    private Boolean lowStock;
    private String imageUrl;
    private List<String> imageUrls;
    private String sku;
    private BigDecimal weightKg;
    private String dimensions;
    private String brand;
    private String style;
    private String room;
    private String color;
    private String material;
    private String collectionName;
    private String tags;
    private String status;
    private LocalDateTime createdAt;
    private Double averageRating;
    private Integer reviewCount;
    private ProductPromotionDto promotion;
    private String category;
    private Long categoryId;
    
    public boolean isOnSale() {
        return discountedPrice != null && discountedPrice.compareTo(price) < 0;
    }
    
    public boolean isInStock() {
        return inStock != null ? inStock : false;
    }
    
    public boolean isLowStock() {
        return lowStock != null ? lowStock : false;
    }
    
    public String getCategory() {
        return category;
    }
    
    public static ProductDisplayDto fromProduct(Product product) {
        if (product == null) {
            return null;
        }
        
        ProductDisplayDto dto = new ProductDisplayDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCurrency("EUR");
        dto.setStockQuantity(product.getStockQuantity());
        dto.setInStock(product.getStockQuantity() > 0);
        dto.setLowStock(product.getMinStockLevel() != null && product.getStockQuantity() <= product.getMinStockLevel());
        dto.setSku(product.getSku());
        dto.setWeightKg(BigDecimal.valueOf(1.0)); // Default weight to avoid issues
        dto.setDimensions(product.getDimensions());
        dto.setStatus(product.getStatus() != null ? product.getStatus().name() : null);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setBrand(product.getBrand());
        dto.setStyle(product.getStyle());
        dto.setRoom(product.getRoom());
        dto.setColor(product.getColor());
        dto.setMaterial(product.getMaterial());
        dto.setCollectionName(product.getCollectionName());
        dto.setTags(product.getTags());
        
        // Set category information - with lazy loading protection
        try {
            if (product.getCategory() != null) {
                dto.setCategory(product.getCategory().getName());
                dto.setCategoryId(product.getCategory().getId());
            } else if (product.getCategoryId() != null) {
                dto.setCategoryId(product.getCategoryId());
            }
        } catch (Exception e) {
            // Fallback en cas de LazyInitializationException pour la catégorie
            if (product.getCategoryId() != null) {
                dto.setCategoryId(product.getCategoryId());
                dto.setCategory("Catégorie");
            }
        }
        
        // Handle images from ProductImage entities - with lazy loading protection
        try {
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                List<String> imageUrls = product.getImages().stream()
                        .map(productImage -> productImage.getImageUrl())
                        .collect(java.util.stream.Collectors.toList());
                dto.setImageUrls(imageUrls);
                
                // Prefer main image; fallback to first image if none is flagged main
                String mainUrl = product.getImageUrl();
                if (mainUrl == null && !imageUrls.isEmpty()) {
                    mainUrl = imageUrls.get(0);
                }
                dto.setImageUrl(mainUrl != null ? mainUrl : "/images/placeholder-product.jpg");
            } else {
                dto.setImageUrl("/images/placeholder-product.jpg");
            }
        } catch (Exception e) {
            // Fallback en cas de LazyInitializationException
            dto.setImageUrl("/images/placeholder-product.jpg");
            dto.setImageUrls(java.util.Arrays.asList("/images/placeholder-product.jpg"));
        }
        
        // Handle promotions - with lazy loading protection
        try {
            Promotion activePromotion = product.getPromotion();
            if (activePromotion != null) {
                ProductPromotionDto promotionDto = new ProductPromotionDto();
                promotionDto.setId(activePromotion.getId());
                promotionDto.setName(activePromotion.getName());
                promotionDto.setDescription(activePromotion.getDescription());
                promotionDto.setDiscountPercentage(activePromotion.getDiscountPercentage());
                promotionDto.setDiscountAmount(activePromotion.getDiscountAmount());
                promotionDto.setDiscountType(activePromotion.getDiscountType() != null ? 
                    activePromotion.getDiscountType().name() : null);
                dto.setPromotion(promotionDto);
                dto.setDiscountedPrice(product.getDiscountedPrice());
            } else {
                dto.setDiscountedPrice(product.getPrice());
            }
        } catch (Exception e) {
            // Fallback en cas de LazyInitializationException pour les promotions
            dto.setDiscountedPrice(product.getPrice());
        }
        
        // Default values for fields not in Product entity
        dto.setAverageRating(0.0);
        dto.setReviewCount(0);
        
        return dto;
    }
}
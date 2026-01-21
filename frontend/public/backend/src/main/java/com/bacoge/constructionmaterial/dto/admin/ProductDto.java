package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    private Long id;
    private String name;
    private String description;
    private String longDescription;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stockQuantity;
    private Integer minStockLevel;
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
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private List<String> additionalImages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Review statistics
    private Double averageRating;
    private Long reviewCount;
}
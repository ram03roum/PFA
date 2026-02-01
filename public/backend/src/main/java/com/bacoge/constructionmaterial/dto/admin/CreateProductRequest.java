package com.bacoge.constructionmaterial.dto.admin;

import com.bacoge.constructionmaterial.model.Product;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    
    private String name;
    private String description;
    
    @Size(max = 65535, message = "La description longue ne peut pas dépasser 65535 caractères")
    private String longDescription;
    
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
    private Double weightKg;
    private String dimensions;
    private String brand;
    private String style;
    private String room;
    private String color;
    private String material;
    private String collectionName;
    private String tags;
    private Long categoryId;
    private Product.ProductStatus statusEnum;
    private Integer minStockLevel;
    private List<String> imageUrls;
}
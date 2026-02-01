package com.bacoge.constructionmaterial.dto.admin;

import com.bacoge.constructionmaterial.model.Category;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String status;
    private Long productsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper method to convert from entity to DTO
    public static CategoryDto fromEntity(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setStatus(category.getStatus() != null ? category.getStatus().name() : null);
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        // Gérer le lazy loading pour les produits
        try {
            if (category.getProducts() != null && org.hibernate.Hibernate.isInitialized(category.getProducts())) {
                dto.setProductsCount((long) category.getProducts().size());
            } else {
                dto.setProductsCount(0L);
            }
        } catch (Exception e) {
            // En cas d'erreur de lazy loading, mettre 0
            dto.setProductsCount(0L);
        }
        
        return dto;
    }
}
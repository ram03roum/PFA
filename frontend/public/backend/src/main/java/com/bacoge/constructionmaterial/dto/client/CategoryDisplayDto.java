package com.bacoge.constructionmaterial.dto.client;

import java.time.LocalDateTime;
import com.bacoge.constructionmaterial.model.Category;

public class CategoryDisplayDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
    private int productCount;
    
    public CategoryDisplayDto() {}
    
    public CategoryDisplayDto(Long id, String name, String description, String imageUrl, String status, LocalDateTime createdAt, int productCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.productCount = productCount;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public int getProductCount() {
        return productCount;
    }
    
    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }
    
    public static CategoryDisplayDto fromCategory(Category category) {
        CategoryDisplayDto dto = new CategoryDisplayDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setStatus("ACTIVE"); // Default status
        dto.setCreatedAt(category.getCreatedAt());
        
        // Compter les produits dans la catégorie
        int productCount = category.getProducts() != null ? category.getProducts().size() : 0;
        dto.setProductCount(productCount);
        
        return dto;
    }
}
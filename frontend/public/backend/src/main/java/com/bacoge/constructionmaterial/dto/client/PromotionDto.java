package com.bacoge.constructionmaterial.dto.client;

import com.bacoge.constructionmaterial.model.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromotionDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private String discountType;
    private String promoCode;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal minOrderAmount;
    private Integer maxUses;
    private Integer currentUses;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static PromotionDto fromPromotion(Promotion promotion) {
        if (promotion == null) return null;
        
        PromotionDto dto = new PromotionDto();
        dto.setId(promotion.getId());
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setDiscountPercentage(promotion.getDiscountPercentage());
        dto.setDiscountAmount(promotion.getDiscountAmount());
        dto.setDiscountType(promotion.getDiscountType() != null ? 
                promotion.getDiscountType().name() : null);
        dto.setPromoCode(promotion.getPromoCode());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setMinOrderAmount(promotion.getMinOrderAmount());
        dto.setMaxUses(promotion.getMaxUses());
        dto.setCurrentUses(promotion.getCurrentUses());
        dto.setStatus(promotion.getStatus() != null ? promotion.getStatus().name() : null);
        dto.setCreatedAt(promotion.getCreatedAt());
        dto.setUpdatedAt(promotion.getUpdatedAt());
        
        return dto;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    
    public Integer getCurrentUses() { return currentUses; }
    public void setCurrentUses(Integer currentUses) { this.currentUses = currentUses; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

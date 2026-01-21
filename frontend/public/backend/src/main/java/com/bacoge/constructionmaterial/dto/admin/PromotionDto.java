package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDto {
    
    private Long id;
    private String name;
    private String description;
    private String promoCode;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private String discountType;
    private String status;
    private BigDecimal minOrderAmount;
    private Integer maxUses;
    private Integer currentUses;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.bacoge.constructionmaterial.dto.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPromotionDto {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private String discountType;
}
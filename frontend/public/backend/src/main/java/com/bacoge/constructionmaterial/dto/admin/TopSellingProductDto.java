package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductDto {
    
    private Long productId;
    private String productName;
    private String categoryName;
    private Long salesCount;
    private BigDecimal salesValue;
    private BigDecimal averagePrice;
    private Integer stockQuantity;
}
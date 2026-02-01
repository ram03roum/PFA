package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsDto {
    
    private String categoryName;
    private Long productCount;
    private Long totalStock;
    private BigDecimal totalValue;
    private Long salesCount;
    private BigDecimal salesValue;
}
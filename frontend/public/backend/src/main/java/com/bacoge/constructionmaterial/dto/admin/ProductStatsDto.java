package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatsDto {
    
    private Long totalProducts;
    private Long activeProducts;
    private Long inactiveProducts;
    private Long outOfStockProducts;
    private Long lowStockProducts;
    private BigDecimal totalStockValue;
    private Long totalStockQuantity;
    private LocalDateTime lastUpdated;
}

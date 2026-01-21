package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatsDto {
    
    private Long totalProducts;
    private Long lowStockProducts;
    private Long outOfStockProducts;
    private BigDecimal totalStockValue;
    private Long totalStockQuantity;
}

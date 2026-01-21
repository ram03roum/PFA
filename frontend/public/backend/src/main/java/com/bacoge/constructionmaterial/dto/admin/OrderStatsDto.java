package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsDto {
    
    private Long totalOrders;
    private Long pendingOrders;
    private Long processingOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private LocalDateTime lastUpdated;
}

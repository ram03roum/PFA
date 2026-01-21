package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsDto {
    
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal weeklyRevenue;
    private BigDecimal dailyRevenue;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}

package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementStatsDto {
    
    private Long totalMovements;
    private Long incomingMovements;
    private Long outgoingMovements;
    private Long adjustmentMovements;
    private Long returnMovements;
    private Long newToday;
    private Long newThisWeek;
    private Long newThisMonth;
    private LocalDateTime lastUpdated;
}
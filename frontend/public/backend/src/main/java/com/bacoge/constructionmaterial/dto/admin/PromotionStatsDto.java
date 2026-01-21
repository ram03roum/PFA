package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionStatsDto {
    
    private Long totalPromotions;
    private Long activePromotions;
    private Long inactivePromotions;
    private Long expiredPromotions;
    private Long newToday;
    private Long newThisWeek;
    private Long newThisMonth;
    private LocalDateTime lastUpdated;
}
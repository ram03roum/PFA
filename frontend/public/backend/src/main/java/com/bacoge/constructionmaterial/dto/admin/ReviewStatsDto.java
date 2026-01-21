package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsDto {
    
    private Long totalReviews;
    private Long approvedReviews;
    private Long pendingReviews;
    private Long rejectedReviews;
    private Double averageRating;
    private Long newToday;
    private Long newThisWeek;
    private Long newThisMonth;
    private LocalDateTime lastUpdated;
}
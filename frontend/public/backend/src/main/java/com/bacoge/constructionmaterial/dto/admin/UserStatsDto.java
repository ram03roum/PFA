package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Double conversionRate;
    private LocalDateTime lastUpdated;
}

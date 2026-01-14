package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReportDto {
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Long totalUsers;
    private Long newUsers;
    private Long activeUsers;
    private Double conversionRate;
    private Long totalRegistrations;
}

package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsDto {
    
    private Long totalNotifications;
    private Long unreadNotifications;
    private Long readNotifications;
    private Long sentToday;
    private Long sentThisWeek;
    private Long sentThisMonth;
    private LocalDateTime lastUpdated;
}
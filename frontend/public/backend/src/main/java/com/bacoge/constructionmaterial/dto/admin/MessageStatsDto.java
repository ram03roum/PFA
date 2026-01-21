package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatsDto {
    
    private Long totalMessages;
    private Long unreadMessages;
    private Long readMessages;
    private Long sentToday;
    private Long sentThisWeek;
    private Long sentThisMonth;
    private LocalDateTime lastUpdated;
}
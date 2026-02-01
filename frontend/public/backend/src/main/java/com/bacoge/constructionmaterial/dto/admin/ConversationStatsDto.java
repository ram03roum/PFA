package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationStatsDto {
    
    private Long totalConversations;
    private Long activeConversations;
    private Long closedConversations;
    private Long newToday;
    private Long newThisWeek;
    private Long newThisMonth;
    private LocalDateTime lastUpdated;
}
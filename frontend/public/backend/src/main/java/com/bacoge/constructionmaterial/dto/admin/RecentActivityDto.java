package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
    
    private Long id;
    private String type;
    private String description;
    private String userEmail;
    private LocalDateTime timestamp;
    private String details;
    private String status;
}
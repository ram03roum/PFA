package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserChartDto {
    
    private LocalDateTime date;
    private Long users;
    private String status;
    private Long count;
}

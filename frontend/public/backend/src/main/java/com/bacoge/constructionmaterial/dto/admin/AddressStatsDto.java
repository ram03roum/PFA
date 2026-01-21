package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressStatsDto {
    
    private Long totalAddresses;
    private Long defaultAddresses;
    private Long nonDefaultAddresses;
    private Long newToday;
    private Long newThisWeek;
    private Long newThisMonth;
    private LocalDateTime lastUpdated;
}
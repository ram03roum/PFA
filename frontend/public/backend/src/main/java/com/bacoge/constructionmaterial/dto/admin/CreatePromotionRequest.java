package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePromotionRequest {
    
    private String name;
    private String description;
    private String promoCode;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private String discountType;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;
    // Champs additionnels pour cohérence avec le formulaire Thymeleaf
    private BigDecimal minOrderAmount;
    private Integer maxUses;
    private String status; // ACTIVE, INACTIVE, EXPIRED

    // Sélections multiples depuis la page (multi-select)
    private List<Long> applicableCategoryIds;
    private List<Long> applicableProductIds;

    // Champs simples conservés pour rétrocompatibilité éventuelle
    private Long categoryId;
    private Long productId;
}
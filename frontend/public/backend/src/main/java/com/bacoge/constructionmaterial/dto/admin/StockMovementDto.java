package com.bacoge.constructionmaterial.dto.admin;

import com.bacoge.constructionmaterial.model.StockMovement;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementDto {
    
    private Long id;
    private Long productId;
    private String productName;
    private String movementType;
    private Integer quantity;
    private String reason;
    private LocalDateTime createdAt;
    
    public static StockMovementDto fromStockMovement(StockMovement stockMovement) {
        if (stockMovement == null) {
            return null;
        }
        
        StockMovementDto dto = new StockMovementDto();
        dto.setId(stockMovement.getId());
        dto.setProductId(stockMovement.getProductId());
        dto.setProductName(stockMovement.getProductName());
        dto.setMovementType(stockMovement.getMovementType() != null ? stockMovement.getMovementType().name() : null);
        dto.setQuantity(stockMovement.getQuantity());
        dto.setReason(stockMovement.getReason());
        dto.setCreatedAt(stockMovement.getCreatedAt());
        return dto;
    }
}
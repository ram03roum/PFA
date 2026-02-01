package com.bacoge.constructionmaterial.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(name = "product_name")
    private String productName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type")
    private MovementType movementType;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum MovementType {
        IN, OUT, ADJUSTMENT, RETURN
    }
}
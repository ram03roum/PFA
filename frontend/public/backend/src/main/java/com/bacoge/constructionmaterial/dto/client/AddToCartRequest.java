package com.bacoge.constructionmaterial.dto.client;

import com.bacoge.constructionmaterial.dto.validation.ValidProductId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddToCartRequest {
    
    @NotNull(message = "L'ID du produit est obligatoire")
    @ValidProductId(message = "Le produit spécifié n'existe pas")
    private Long productId;
    
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    @Max(value = 999, message = "La quantité ne peut pas dépasser 999")
    private Integer quantity;
    
    public AddToCartRequest() {}
    
    public AddToCartRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
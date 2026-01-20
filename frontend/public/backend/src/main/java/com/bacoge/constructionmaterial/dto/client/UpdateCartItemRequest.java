package com.bacoge.constructionmaterial.dto.client;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateCartItemRequest {
    
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantity;
    
    public UpdateCartItemRequest() {}
    
    public UpdateCartItemRequest(Integer quantity) {
        this.quantity = quantity;
    }
    
    // Getters and Setters
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
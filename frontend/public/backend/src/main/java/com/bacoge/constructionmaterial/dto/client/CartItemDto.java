package com.bacoge.constructionmaterial.dto.client;

import com.bacoge.constructionmaterial.model.CartItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItemDto {
    
    private Long id;
    private Long cartId;
    private ProductDisplayDto product;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountedPrice;
    private PromotionDto promotion;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static CartItemDto fromCartItem(CartItem cartItem) {
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setCartId(cartItem.getCart() != null ? cartItem.getCart().getId() : null);
        dto.setProduct(cartItem.getProduct() != null ? 
                ProductDisplayDto.fromProduct(cartItem.getProduct()) : null);
        dto.setQuantity(cartItem.getQuantity());
        dto.setUnitPrice(cartItem.getUnitPrice());
        dto.setDiscountedPrice(cartItem.getDiscountedPrice());
        dto.setPromotion(cartItem.getPromotion() != null ? 
                PromotionDto.fromPromotion(cartItem.getPromotion()) : null);
        dto.setTotalPrice(cartItem.getTotalPrice());
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());
        return dto;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }
    
    public ProductDisplayDto getProduct() { return product; }
    public void setProduct(ProductDisplayDto product) { this.product = product; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getTotalPrice() { 
        if (totalPrice != null) {
            return totalPrice;
        }
        // Calculer le total si non défini
        if (quantity == null) return BigDecimal.ZERO;
        BigDecimal priceToUse = discountedPrice != null ? discountedPrice : unitPrice;
        return priceToUse != null ? priceToUse.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Getters et setters pour les nouveaux champs
    public BigDecimal getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }
    
    public PromotionDto getPromotion() { return promotion; }
    public void setPromotion(PromotionDto promotion) { this.promotion = promotion; }
    
    // Méthode utilitaire pour obtenir l'ID du produit
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }
    
    // Méthode utilitaire pour obtenir le prix unitaire final (après réduction)
    public BigDecimal getFinalUnitPrice() {
        return discountedPrice != null ? discountedPrice : unitPrice;
    }
}
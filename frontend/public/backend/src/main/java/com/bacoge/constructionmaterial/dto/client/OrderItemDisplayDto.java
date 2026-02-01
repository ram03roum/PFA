package com.bacoge.constructionmaterial.dto.client;

import java.math.BigDecimal;

public class OrderItemDisplayDto {
    private Long id;
    private ProductDisplayDto product;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    
    public OrderItemDisplayDto() {}
    
    public OrderItemDisplayDto(Long id, ProductDisplayDto product, Integer quantity, BigDecimal price, BigDecimal totalPrice) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ProductDisplayDto getProduct() {
        return product;
    }
    
    public void setProduct(ProductDisplayDto product) {
        this.product = product;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
} 
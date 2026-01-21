package com.bacoge.constructionmaterial.dto.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDisplayDto {
    private Long id;
    private String orderNumber;
    private List<OrderItemDisplayDto> orderItems;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public OrderDisplayDto() {}
    
    public OrderDisplayDto(Long id, String orderNumber, List<OrderItemDisplayDto> orderItems, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal shippingCost, BigDecimal totalAmount, String status, String shippingAddress, String billingAddress, String paymentMethod, String paymentStatus, String notes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.orderItems = orderItems;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.shippingCost = shippingCost;
        this.totalAmount = totalAmount;
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public List<OrderItemDisplayDto> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemDisplayDto> orderItems) {
        this.orderItems = orderItems;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }
    
    public BigDecimal getShippingCost() {
        return shippingCost;
    }
    
    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 
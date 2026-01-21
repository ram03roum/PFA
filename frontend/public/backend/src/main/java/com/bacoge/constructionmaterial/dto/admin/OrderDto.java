package com.bacoge.constructionmaterial.dto.admin;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    
    private Long id;
    private String orderNumber;
    private UserDto user;
    private com.bacoge.constructionmaterial.model.Order.OrderStatus status;
    private com.bacoge.constructionmaterial.model.Order.PaymentStatus paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String billingAddress;
    private List<OrderItemDto> orderItems;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
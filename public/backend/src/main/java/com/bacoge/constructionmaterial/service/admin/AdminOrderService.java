package com.bacoge.constructionmaterial.service.admin;

import com.bacoge.constructionmaterial.dto.UserResponse;
import com.bacoge.constructionmaterial.dto.admin.CreateOrderRequest;
import com.bacoge.constructionmaterial.dto.admin.OrderDto;
import com.bacoge.constructionmaterial.dto.admin.OrderItemDto;
import com.bacoge.constructionmaterial.dto.admin.UserDto;
import com.bacoge.constructionmaterial.model.Order;
import com.bacoge.constructionmaterial.model.OrderItem;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class AdminOrderService {
    private static final Logger logger = LoggerFactory.getLogger(AdminOrderService.class);

    private final com.bacoge.constructionmaterial.repository.OrderRepository orderRepository;
    private final com.bacoge.constructionmaterial.repository.UserRepository userRepository;
    private final com.bacoge.constructionmaterial.repository.OrderItemRepository orderItemRepository;
    private final NotificationService notificationService;
    
    public AdminOrderService(com.bacoge.constructionmaterial.repository.OrderRepository orderRepository,
                           com.bacoge.constructionmaterial.repository.UserRepository userRepository,
                           com.bacoge.constructionmaterial.repository.OrderItemRepository orderItemRepository,
                           NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.notificationService = notificationService;
    }
    
    public Page<OrderDto> getAllOrders(String search, Long userId, Order.OrderStatus status, 
                                      Order.PaymentStatus paymentStatus, Pageable pageable) {
        Page<Order> orders = orderRepository.findOrdersWithFilters(search, userId, status, paymentStatus, pageable);
        return orders.map(this::convertToDto);
    }
    
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return convertToDto(order);
    }
    
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findAll().stream()
                .filter(o -> o.getOrderNumber().equals(orderNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
        return convertToDto(order);
    }
    
    @org.springframework.transaction.annotation.Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        // Note: User relationship would need to be set via User entity
        // order.setUser(userRepository.findById(request.getUserId()).orElse(null));
        order.setTotalAmount(request.getTotalAmount());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }
    
    @org.springframework.transaction.annotation.Transactional
    public OrderDto updateOrder(Long id, CreateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setTotalAmount(request.getTotalAmount());
        // Update other fields as needed
        
        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }
    
    @org.springframework.transaction.annotation.Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
    
    @org.springframework.transaction.annotation.Transactional
    public OrderDto updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        try {
            notificationService.sendOrderStatusNotification(savedOrder, status != null ? status.name() : (savedOrder.getStatus() != null ? savedOrder.getStatus().name() : null));
        } catch (Exception e) {
            logger.warn("Failed to send client order status notification: {}", e.getMessage());
        }
        return convertToDto(savedOrder);
    }
    
    @org.springframework.transaction.annotation.Transactional
    public OrderDto updatePaymentStatus(Long id, Order.PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        order.setPaymentStatus(paymentStatus);
        Order savedOrder = orderRepository.save(order);
        try {
            notificationService.sendPaymentNotification(savedOrder, paymentStatus != null ? paymentStatus.name() : (savedOrder.getPaymentStatus() != null ? savedOrder.getPaymentStatus().name() : null));
        } catch (Exception e) {
            logger.warn("Failed to send client payment notification: {}", e.getMessage());
        }
        return convertToDto(savedOrder);
    }
    
    public List<OrderDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto> getOrdersByStatus(Order.OrderStatus status) {
        Page<Order> page = orderRepository.findOrdersWithFilters(null, null, status, null, org.springframework.data.domain.PageRequest.of(0, 1000));
        return page.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto> getOrdersByPaymentStatus(Order.PaymentStatus paymentStatus) {
        Page<Order> page = orderRepository.findOrdersWithFilters(null, null, null, paymentStatus, org.springframework.data.domain.PageRequest.of(0, 1000));
        return page.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getOrderDate().isAfter(startDate) && order.getOrderDate().isBefore(endDate))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public long getTotalOrders() {
        return orderRepository.count();
    }
    
    public long getOrdersCountByStatus(Order.OrderStatus status) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == status)
                .count();
    }
    
    public BigDecimal getTotalRevenueBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getRevenueByDateRange(startDate, endDate);
    }
    
    public long getOrdersCountBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.countOrdersBetween(startDate, endDate);
    }

    public Map<String, BigDecimal> getRevenueByPaymentMethodBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getRevenueByPaymentMethodBetween(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    public Map<String, BigDecimal> getRevenueByCategoryBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getRevenueByCategoryBetween(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    public Map<String, Long> getOrderCountByCategoryBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.getOrderCountByCategoryBetween(startDate, endDate)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    public List<Map<String, Object>> getTopSellingProductsBetween(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return orderRepository.findTopSellingProductsBetween(startDate, endDate, limit)
                .stream()
                .map(row -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("productName", (String) row[0]);
                    m.put("categoryName", (String) row[1]);
                    m.put("totalQuantity", (Long) row[2]);
                    m.put("totalRevenue", (java.math.BigDecimal) row[3]);
                    return m;
                })
                .collect(Collectors.toList());
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
    
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Addresses
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());

        if (order.getUser() != null) {
            dto.setUser(convertToUserDto(order.getUser()));
        }

        if (order.getOrderItems() != null) {
            dto.setOrderItems(order.getOrderItems().stream().map(this::convertToOrderItemDto).collect(Collectors.toList()));
        }

        // Monetary fields with safe fallbacks
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingCost(order.getShippingCost());

        // Compute missing monetary fields if needed
        java.math.BigDecimal computedSubtotal = (dto.getOrderItems() != null)
            ? dto.getOrderItems().stream()
                .map(oi -> oi.getTotalPrice() != null ? oi.getTotalPrice() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
            : java.math.BigDecimal.ZERO;
        if (dto.getSubtotal() == null) dto.setSubtotal(computedSubtotal);
        if (dto.getTaxAmount() == null) dto.setTaxAmount(java.math.BigDecimal.ZERO);
        if (dto.getShippingCost() == null) dto.setShippingCost(java.math.BigDecimal.ZERO);
        if (dto.getTotalAmount() == null) {
            dto.setTotalAmount(dto.getSubtotal().add(dto.getShippingCost()));
        }

        return dto;
    }

    private com.bacoge.constructionmaterial.dto.admin.UserDto convertToUserDto(User user) {
        com.bacoge.constructionmaterial.dto.admin.UserDto userDto = new com.bacoge.constructionmaterial.dto.admin.UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    private com.bacoge.constructionmaterial.dto.admin.OrderItemDto convertToOrderItemDto(OrderItem orderItem) {
        com.bacoge.constructionmaterial.dto.admin.OrderItemDto orderItemDto = new com.bacoge.constructionmaterial.dto.admin.OrderItemDto();
        orderItemDto.setId(orderItem.getId());
        if (orderItem.getProduct() != null) {
            orderItemDto.setProductName(orderItem.getProduct().getName());
            orderItemDto.setProductId(orderItem.getProduct().getId());
        }
        orderItemDto.setQuantity(orderItem.getQuantity());
        // Fallback to product price when line price is null
        java.math.BigDecimal unit = orderItem.getPrice();
        if (unit == null && orderItem.getProduct() != null) unit = orderItem.getProduct().getPrice();
        orderItemDto.setUnitPrice(unit);

        java.math.BigDecimal total = orderItem.getTotalPrice();
        if (total == null && unit != null && orderItem.getQuantity() != null) {
            total = unit.multiply(java.math.BigDecimal.valueOf(orderItem.getQuantity()));
        }
        orderItemDto.setTotalPrice(total);
        return orderItemDto;
    }
}

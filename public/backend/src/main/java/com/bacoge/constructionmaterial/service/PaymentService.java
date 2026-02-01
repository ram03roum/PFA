package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.PaymentRequestDto;
import com.bacoge.constructionmaterial.dto.PaymentResponseDto;
import com.bacoge.constructionmaterial.model.Order;
import com.bacoge.constructionmaterial.model.OrderItem;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.OrderRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AdminNotificationService adminNotificationService;

    /**
     * Traiter un paiement par carte de crédit
     */
    public PaymentResponseDto processCardPayment(PaymentRequestDto paymentRequest, Long userId) {
        try {
            // Créer la commande
            Order order = createOrderFromPaymentRequest(paymentRequest, userId);
            if (order == null) {
                return PaymentResponseDto.failure("Erreur lors de la création de la commande");
            }
            
            // Simuler le traitement du paiement par carte
            String transactionId = generateTransactionId();
            boolean paymentSuccess = simulateCardPayment(paymentRequest);
            
            if (paymentSuccess) {
                // Mettre à jour le statut de la commande
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                order.setStatus(Order.OrderStatus.CONFIRMED);
                order.setPaymentMethod("CARD");
                order.setTransactionId(transactionId);
                orderRepository.save(order);
                
                // Envoyer notification à l'administrateur
                notificationService.sendOrderCreatedNotification(order);
                try { adminNotificationService.createOrderNotification(order); } catch (Exception ignore) {}
                
                return PaymentResponseDto.success(transactionId, order.getId(), "card", paymentRequest.getAmount());
            } else {
                // Échec du paiement
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
                
                return PaymentResponseDto.failure("Paiement refusé par la banque");
            }
        } catch (Exception e) {
            return PaymentResponseDto.failure("Erreur technique lors du paiement");
        }
    }

    /**
     * Traiter un paiement PayPal
     */
    public PaymentResponseDto processPayPalPayment(PaymentRequestDto paymentRequest, Long userId) {
        try {
            // Créer la commande
            Order order = createOrderFromPaymentRequest(paymentRequest, userId);
            if (order == null) {
                return PaymentResponseDto.failure("Erreur lors de la création de la commande");
            }
            
            // Générer l'URL de redirection PayPal (simulation)
            String paymentId = generatePayPalPaymentId();
            String redirectUrl = generatePayPalRedirectUrl(paymentId, order.getId());
            
            // Mettre à jour la commande avec les informations PayPal
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPaymentMethod("PAYPAL");
            order.setTransactionId(paymentId);
            orderRepository.save(order);
            
            return PaymentResponseDto.paypalRedirect(redirectUrl, paymentId, order.getId(), paymentRequest.getAmount());
        } catch (Exception e) {
            return PaymentResponseDto.failure("Erreur lors de l'initialisation PayPal");
        }
    }

    /**
     * Traiter un paiement par virement bancaire
     */
    public PaymentResponseDto processBankTransferPayment(PaymentRequestDto paymentRequest, Long userId) {
        try {
            // Créer la commande
            Order order = createOrderFromPaymentRequest(paymentRequest, userId);
            if (order == null) {
                return PaymentResponseDto.failure("Erreur lors de la création de la commande");
            }
            
            String transactionId = generateTransactionId();
            
            // Mettre à jour la commande
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPaymentMethod("BANK_TRANSFER");
            order.setTransactionId(transactionId);
            orderRepository.save(order);
            
            // Générer les détails bancaires
            Map<String, String> bankDetails = generateBankDetails(order.getId());
            // Envoyer notification à l'administrateur
            notificationService.sendOrderCreatedNotification(order);
            try { adminNotificationService.createOrderNotification(order); } catch (Exception ignore) {}
            
            return PaymentResponseDto.bankTransfer(transactionId, order.getId(), paymentRequest.getAmount(), bankDetails);
        } catch (Exception e) {
            return PaymentResponseDto.failure("Erreur lors de la création de la commande");
        }
    }
    
    /**
     * Traiter un paiement à la livraison (Cash on Delivery)
     */
    public PaymentResponseDto processCashOnDeliveryPayment(PaymentRequestDto paymentRequest, Long userId) {
        try {
            // Créer la commande
            Order order = createOrderFromPaymentRequest(paymentRequest, userId);
            if (order == null) {
                return PaymentResponseDto.failure("Erreur lors de la création de la commande");
            }
            String transactionId = "COD_" + System.currentTimeMillis();
            // Mettre à jour la commande
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPaymentMethod("CASH_ON_DELIVERY");
            order.setTransactionId(transactionId);
            orderRepository.save(order);
            // Notifications client + admin
            notificationService.sendOrderCreatedNotification(order);
            try { adminNotificationService.createOrderNotification(order); } catch (Exception ignore) {}
            return PaymentResponseDto.success(transactionId, order.getId(), "cod", paymentRequest.getAmount());
        } catch (Exception e) {
            return PaymentResponseDto.failure("Erreur lors de la création de la commande (COD)");
        }
    }

    /**
     * Confirmer un paiement PayPal
     */
    public PaymentResponseDto confirmPayPalPayment(String paymentId, String payerId) {
        try {
            // Trouver la commande par transaction ID
            Optional<Order> orderOpt = orderRepository.findByTransactionId(paymentId);
            if (!orderOpt.isPresent()) {
                return PaymentResponseDto.failure("Commande non trouvée");
            }
            
            Order order = orderOpt.get();
            
            // Simuler la confirmation PayPal
            boolean confirmationSuccess = simulatePayPalConfirmation(paymentId, payerId);
            
            if (confirmationSuccess) {
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                order.setStatus(Order.OrderStatus.CONFIRMED);
                orderRepository.save(order);
                
                // Envoyer notification à l'administrateur
                notificationService.sendOrderCreatedNotification(order);
                try { adminNotificationService.createOrderNotification(order); } catch (Exception ignore) {}
                
                return PaymentResponseDto.success(paymentId, order.getId(), "paypal", order.getTotalAmount());
            } else {
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderRepository.save(order);
                
                return PaymentResponseDto.failure("Échec de la confirmation PayPal");
            }
        } catch (Exception e) {
            return PaymentResponseDto.failure("Erreur lors de la confirmation PayPal");
        }
    }

    /**
     * Obtenir le statut d'un paiement
     */
    public String getPaymentStatus(Long orderId, Long userId) {
        Optional<Order> orderOpt = orderRepository.findByIdAndUserId(orderId, userId);
        if (orderOpt.isPresent()) {
            return orderOpt.get().getPaymentStatus().toString();
        }
        throw new RuntimeException("Commande non trouvée");
    }

    /**
     * Créer une commande à partir d'une requête de paiement
     */
    private Order createOrderFromPaymentRequest(PaymentRequestDto paymentRequest, Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return null;
            }
            
            User user = userOpt.get();
            Order order = new Order();
            order.setUser(user);
            order.setCreatedAt(LocalDateTime.now());
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPaymentStatus(Order.PaymentStatus.PENDING);
            
            // Adresse de livraison
            order.setShippingAddress(String.format("%s, %s %s, %s", 
                paymentRequest.getShippingStreet(),
                paymentRequest.getShippingCity(),
                paymentRequest.getShippingPostalCode(),
                paymentRequest.getShippingCountry()));
            
            // Créer les items de commande
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();
            
            for (PaymentRequestDto.OrderItemDto itemDto : paymentRequest.getItems()) {
                Optional<Product> productOpt = productRepository.findById(itemDto.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    
                    // Utiliser le prix remisé si disponible, sinon le prix original
                    BigDecimal itemPrice = itemDto.getDiscountedPrice() != null ? itemDto.getDiscountedPrice() : product.getPrice();
                    
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setPrice(itemPrice);
                    
                    orderItems.add(orderItem);
                    totalAmount = totalAmount.add(itemPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity())));
                }
            }
            
            order.setOrderItems(orderItems);
            order.setTotalAmount(totalAmount);
            
            return orderRepository.save(order);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Simuler un paiement par carte
     */
    private boolean simulateCardPayment(PaymentRequestDto paymentRequest) {
        // Simulation : 95% de succès
        return Math.random() > 0.05;
    }

    /**
     * Simuler une confirmation PayPal
     */
    private boolean simulatePayPalConfirmation(String paymentId, String payerId) {
        // Simulation : 98% de succès
        return Math.random() > 0.02;
    }

    /**
     * Générer un ID de transaction
     */
    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Générer un ID de paiement PayPal
     */
    private String generatePayPalPaymentId() {
        return "PAY_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * Générer une URL de redirection PayPal
     */
    private String generatePayPalRedirectUrl(String paymentId, Long orderId) {
        return String.format("https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=%s&order_id=%d", 
            paymentId, orderId);
    }

    /**
     * Générer les détails bancaires pour virement
     */
    private Map<String, String> generateBankDetails(Long orderId) {
        Map<String, String> bankDetails = new HashMap<>();
        bankDetails.put("bankName", "Banque Bacoge");
        bankDetails.put("iban", "FR76 1234 5678 9012 3456 7890 123");
        bankDetails.put("bic", "BACOFRPP");
        bankDetails.put("reference", "ORDER_" + orderId);
        bankDetails.put("beneficiary", "Bacoge SARL");
        return bankDetails;
    }
}

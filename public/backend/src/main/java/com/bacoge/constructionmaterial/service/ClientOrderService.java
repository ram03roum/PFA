package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.client.CreateOrderRequest;
import com.bacoge.constructionmaterial.dto.client.OrderDisplayDto;
import com.bacoge.constructionmaterial.dto.client.OrderItemDisplayDto;
import com.bacoge.constructionmaterial.dto.client.ProductDisplayDto;
import com.bacoge.constructionmaterial.model.Order;
import com.bacoge.constructionmaterial.model.OrderItem;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.OrderRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientOrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientOrderService.class);
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AuthService authService;
    private final ClientCartService cartService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AdminNotificationService adminNotificationService;
    
    public ClientOrderService(OrderRepository orderRepository, ProductRepository productRepository, 
                             AuthService authService, ClientCartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.authService = authService;
        this.cartService = cartService;
    }
    
    public OrderDisplayDto createOrder(CreateOrderRequest request) {
        logger.info("Création d'une nouvelle commande");
        
        // Validation des paramètres
        if (request == null) {
            throw new IllegalArgumentException("La requête de création de commande ne peut pas être null");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La commande doit contenir au moins un article");
        }
        
        try {
            // Récupérer l'utilisateur connecté
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("Utilisateur non authentifié");
            }
            
            logger.info("Création de commande pour l'utilisateur ID: {}", currentUser.getId());
            
            // Valider le panier avant de créer la commande
            cartService.validateCartForOrder(currentUser.getId());
            
            Order order = new Order();
            order.setUser(currentUser);
            order.setShippingAddress(request.getShippingAddress());
            order.setBillingAddress(request.getBillingAddress());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setNotes(request.getNotes());
            
            // Créer les éléments de commande avec validation
            List<OrderItem> orderItems = request.getItems().stream()
                    .map(itemRequest -> {
                        if (itemRequest.getProductId() == null) {
                            throw new IllegalArgumentException("L'ID du produit ne peut pas être null");
                        }
                        if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                            throw new IllegalArgumentException("La quantité doit être positive");
                        }
                        
                        Product product = productRepository.findById(itemRequest.getProductId())
                                .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + itemRequest.getProductId()));
                        
                        // Vérifier la disponibilité et le stock
                        if (!product.isActive()) {
                            throw new RuntimeException("Le produit '" + product.getName() + "' n'est plus disponible");
                        }
                        
                        if (product.getStockQuantity() < itemRequest.getQuantity()) {
                            throw new RuntimeException("Stock insuffisant pour le produit '" + product.getName() + 
                                                     "'. Stock disponible: " + product.getStockQuantity() + 
                                                     ", quantité demandée: " + itemRequest.getQuantity());
                        }
                        
                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        orderItem.setProduct(product);
                        orderItem.setQuantity(itemRequest.getQuantity());
                        orderItem.setPrice(product.getPrice());
                        orderItem.calculateTotalPrice();
                        
                        // Réduire le stock
                        product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
                        productRepository.save(product);
                        
                        logger.debug("Article ajouté à la commande - Produit: {}, Quantité: {}", 
                                   product.getName(), itemRequest.getQuantity());
                        
                        return orderItem;
                    })
                    .collect(Collectors.toList());
            
            order.setOrderItems(orderItems);
            order.calculateTotals();
            
            Order savedOrder = orderRepository.save(order);
            
            // Créer une notification pour l'admin
            try {
                notificationService.sendOrderCreatedNotification(savedOrder);
                logger.info("Notification admin créée pour la commande: {}", savedOrder.getId());
                // Realtime admin notification via WebSocket
                try { adminNotificationService.createOrderNotification(savedOrder); } catch (Exception ex) { logger.warn("WS notify order create failed: {}", ex.getMessage()); }
            } catch (Exception e) {
                logger.error("Erreur lors de la création de la notification admin pour la commande: {}", savedOrder.getId(), e);
                // Ne pas faire échouer la commande si la notification échoue
            }
            
            // Vérifier les stocks faibles/ruptures après la commande
            checkStockLevelsAfterOrder(orderItems);
            
            // Vider le panier après création de la commande
            cartService.clearCart(currentUser.getId());
            
            logger.info("Commande créée avec succès - Numéro: {}, Montant total: {}", 
                       savedOrder.getOrderNumber(), savedOrder.getTotalAmount());
            
            return convertToDto(savedOrder);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la commande", e);
            throw e;
        }
    }
    
    public Page<OrderDisplayDto> getUserOrders(Pageable pageable) {
        logger.info("Récupération des commandes utilisateur");
        
        try {
            // Récupérer l'utilisateur connecté
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("Utilisateur non authentifié");
            }
            
            logger.debug("Récupération des commandes pour l'utilisateur ID: {}", currentUser.getId());
            
            // Récupérer les commandes de l'utilisateur connecté
            Page<Order> orders = orderRepository.findByUserId(currentUser.getId(), pageable);
            
            logger.info("Nombre de commandes trouvées: {} pour l'utilisateur ID: {}", 
                       orders.getTotalElements(), currentUser.getId());
            
            return orders.map(this::convertToDto);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des commandes utilisateur", e);
            throw e;
        }
    }
    
    public OrderDisplayDto getOrderById(Long id) {
        logger.info("Récupération de la commande ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("L'ID de la commande ne peut pas être null");
        }
        
        try {
            // Récupérer l'utilisateur connecté
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("Utilisateur non authentifié");
            }
            
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + id));
            
            // Vérifier que la commande appartient à l'utilisateur connecté
            if (!order.getUser().getId().equals(currentUser.getId())) {
                logger.warn("Tentative d'accès non autorisé à la commande {} par l'utilisateur {}", 
                           id, currentUser.getId());
                throw new RuntimeException("Accès non autorisé à cette commande");
            }
            
            logger.info("Commande récupérée avec succès - ID: {}, Numéro: {}", 
                       order.getId(), order.getOrderNumber());
            
            return convertToDto(order);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de la commande ID: {}", id, e);
            throw e;
        }
    }
    
    public void cancelOrder(Long id) {
        logger.info("Annulation de la commande ID: {}", id);
        
        if (id == null) {
            throw new IllegalArgumentException("L'ID de la commande ne peut pas être null");
        }
        
        try {
            // Récupérer l'utilisateur connecté
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("Utilisateur non authentifié");
            }
            
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + id));
            
            // Vérifier que la commande appartient à l'utilisateur connecté
            if (!order.getUser().getId().equals(currentUser.getId())) {
                logger.warn("Tentative d'annulation non autorisée de la commande {} par l'utilisateur {}", 
                           id, currentUser.getId());
                throw new RuntimeException("Accès non autorisé à cette commande");
            }
            
            if (order.getStatus() == Order.OrderStatus.PENDING) {
                order.setStatus(Order.OrderStatus.CANCELLED);
                
                // Restaurer le stock des produits
                for (OrderItem item : order.getOrderItems()) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                    
                    logger.debug("Stock restauré pour le produit: {} - Quantité: {}", 
                               product.getName(), item.getQuantity());
                }
                
                orderRepository.save(order);
                
                logger.info("Commande annulée avec succès - ID: {}, Numéro: {}", 
                           order.getId(), order.getOrderNumber());
                // Notify admins of status change
                try { adminNotificationService.createOrderStatusChangedNotification(order, Order.OrderStatus.CANCELLED); } catch (Exception ex) { logger.warn("WS notify order cancel failed: {}", ex.getMessage()); }
                
            } else {
                throw new RuntimeException("Impossible d'annuler cette commande. Statut actuel: " + order.getStatus());
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'annulation de la commande ID: {}", id, e);
            throw e;
        }
    }

    public void confirmOrderReceived(Long id) {
        logger.info("Confirmation de réception pour la commande ID: {}", id);
        if (id == null) {
            throw new IllegalArgumentException("L'ID de la commande ne peut pas être null");
        }
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("Utilisateur non authentifié");
            }
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + id));
            if (!order.getUser().getId().equals(currentUser.getId())) {
                logger.warn("Tentative non autorisée de confirmation de réception pour la commande {} par l'utilisateur {}", id, currentUser.getId());
                throw new RuntimeException("Accès non autorisé à cette commande");
            }
            if (order.getStatus() == Order.OrderStatus.SHIPPED) {
                order.setStatus(Order.OrderStatus.DELIVERED);
                orderRepository.save(order);
                logger.info("Commande marquée comme livrée - ID: {}", order.getId());
                // Notify admins of status change
                try { adminNotificationService.createOrderStatusChangedNotification(order, Order.OrderStatus.DELIVERED); } catch (Exception ex) { logger.warn("WS notify order delivered failed: {}", ex.getMessage()); }
            } else {
                throw new RuntimeException("Impossible de confirmer la réception. Statut actuel: " + order.getStatus());
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la confirmation de réception pour la commande ID: {}", id, e);
            throw e;
        }
    }
    
    private OrderDisplayDto convertToDto(Order order) {
        OrderDisplayDto dto = new OrderDisplayDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingCost(order.getShippingCost());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus().name());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Convertir les éléments de commande
        List<OrderItemDisplayDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());
        dto.setOrderItems(orderItemDtos);
        
        return dto;
    }
    
    private OrderItemDisplayDto convertOrderItemToDto(OrderItem orderItem) {
        OrderItemDisplayDto dto = new OrderItemDisplayDto();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setTotalPrice(orderItem.getTotalPrice());
        
        // Convertir le produit
        if (orderItem.getProduct() != null) {
            ProductDisplayDto productDto = ProductDisplayDto.fromProduct(orderItem.getProduct());
            dto.setProduct(productDto);
        }
        
        return dto;
    }
    
    public List<OrderDisplayDto> getOrderHistory() {
        logger.debug("Début de getOrderHistory");
        
        // Vérification de l'authentification
        if (SecurityContextHolder.getContext().getAuthentication() == null || 
            !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            logger.error("Tentative d'accès à l'historique des commandes sans authentification");
            throw new SecurityException("Utilisateur non authentifié");
        }
        
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            logger.error("Impossible de récupérer l'utilisateur courant");
            throw new SecurityException("Erreur d'authentification");
        }
        
        logger.debug("Recherche des commandes pour l'utilisateur ID: {}", currentUser.getId());
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(currentUser);
        logger.debug("Nombre de commandes trouvées: {}", orders.size());
        
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public byte[] generateInvoice(Long orderId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Commande non trouvée"));
        
        // Vérifier que la commande appartient à l'utilisateur actuel
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Accès non autorisé à cette commande");
        }
        
        // Vérifier que la commande est livrée
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("La facture n'est disponible que pour les commandes livrées");
        }
        
        // Générer un PDF simple (simulation)
        // Dans un vrai projet, vous utiliseriez une bibliothèque comme iText ou Apache PDFBox
        String invoiceContent = generateInvoiceContent(order);
        return invoiceContent.getBytes();
    }
    
    private String generateInvoiceContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("FACTURE\n\n");
        content.append("Commande #").append(order.getOrderNumber()).append("\n");
        content.append("Date: ").append(order.getCreatedAt()).append("\n\n");
        
        content.append("Client:\n");
        content.append(order.getUser().getFirstName()).append(" ").append(order.getUser().getLastName()).append("\n");
        content.append(order.getUser().getEmail()).append("\n\n");
        
        if (order.getBillingAddress() != null) {
            content.append("Adresse de facturation:\n");
            content.append(order.getBillingAddress()).append("\n\n");
        }
        
        content.append("Articles:\n");
        for (OrderItem item : order.getOrderItems()) {
            content.append("- ").append(item.getProduct().getName())
                   .append(" x").append(item.getQuantity())
                   .append(" = ").append(item.getTotalPrice()).append(" €\n");
        }
        
        content.append("\nSous-total: ").append(order.getSubtotal()).append(" €\n");
        content.append("TVA: ").append(order.getTaxAmount()).append(" €\n");
        content.append("Frais de livraison: ").append(order.getShippingCost()).append(" €\n");
        content.append("TOTAL: ").append(order.getTotalAmount()).append(" €\n");
        
        return content.toString();
    }
    
    /**
     * Vérifie les niveaux de stock après une commande et crée des notifications si nécessaire
     */
    private void checkStockLevelsAfterOrder(List<OrderItem> orderItems) {
        try {
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                
                // Vérifier rupture de stock
                if (product.getStockQuantity() <= 0) {
                    // TODO: Implémenter la notification de rupture de stock
                    logger.info("Stock épuisé pour le produit: {}", product.getName());
                }
                // Vérifier stock faible (moins de 10 unités)
                else if (product.getStockQuantity() <= 10) {
                    // TODO: Implémenter la notification de stock faible
                    logger.info("Stock faible pour le produit: {} - Quantité restante: {}", product.getName(), product.getStockQuantity());
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification des stocks après commande", e);
            // Ne pas faire échouer la commande si la vérification des stocks échoue
        }
    }
}
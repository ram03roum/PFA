package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.client.AddToCartRequest;
import com.bacoge.constructionmaterial.dto.client.CartDto;
import com.bacoge.constructionmaterial.dto.client.CartItemDto;
import com.bacoge.constructionmaterial.dto.client.UpdateCartItemRequest;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class GuestCartService {
    
    private static final Logger logger = LoggerFactory.getLogger(GuestCartService.class);
    private static final String CART_SESSION_KEY = "guest_cart";
    
    private final ProductRepository productRepository;
    
    public GuestCartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, GuestCartItem> getCartFromSession(HttpSession session) {
        Map<String, GuestCartItem> cart = (Map<String, GuestCartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }
    
    public CartDto getCart(HttpSession session) {
        logger.debug("DEBUG: Début de getCart dans GuestCartService");
        
        try {
            if (session == null) {
                logger.warn("Session is null, returning empty cart");
                return createEmptyCart();
            }
            
            Map<String, GuestCartItem> cartItems = getCartFromSession(session);
            logger.debug("DEBUG: Récupération des articles du panier - nombre d'articles: {}", cartItems.size());
            
            CartDto cartDto = new CartDto();
            cartDto.setId(null); // Pas d'ID pour le panier invité
            cartDto.setItems(new ArrayList<>());
            cartDto.setTotalAmount(BigDecimal.ZERO);
            cartDto.setTotalItems(0);
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalItems = 0;
            
            // Create a list to track items to remove (products that no longer exist)
            List<String> itemsToRemove = new ArrayList<>();
            
            for (Map.Entry<String, GuestCartItem> entry : cartItems.entrySet()) {
                GuestCartItem item = entry.getValue();
                String itemKey = entry.getKey();
                
                logger.debug("DEBUG: Traitement de l'article avec productId: {}", item.getProductId());
                
                try {
                    // Charger le produit avec ses images pour disposer de imageUrl côté front
                    Product product = productRepository.findByIdWithPromotions(item.getProductId()).orElse(null);
                    if (product != null && product.getPrice() != null) {
                        logger.debug("DEBUG: Produit trouvé: {}", product.getName());
                        CartItemDto itemDto = new CartItemDto();
                        itemDto.setId(Long.valueOf(item.getId()));
                        
                        // Créer un ProductDisplayDto pour le produit
                        com.bacoge.constructionmaterial.dto.client.ProductDisplayDto productDto = 
                            com.bacoge.constructionmaterial.dto.client.ProductDisplayDto.fromProduct(product);
                        itemDto.setProduct(productDto);
                        
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setUnitPrice(product.getPrice());
                        itemDto.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                        
                        cartDto.getItems().add(itemDto);
                        totalAmount = totalAmount.add(itemDto.getTotalPrice());
                        totalItems += item.getQuantity();
                    } else {
                        logger.warn("Produit non trouvé ou prix null avec l'ID: {}", item.getProductId());
                        itemsToRemove.add(itemKey);
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors du traitement du produit ID: {}", item.getProductId(), e);
                    itemsToRemove.add(itemKey);
                }
            }
            
            // Remove invalid items from cart
            for (String itemKey : itemsToRemove) {
                cartItems.remove(itemKey);
                logger.debug("Removed invalid item from cart: {}", itemKey);
            }
            
            cartDto.setTotalAmount(totalAmount);
            cartDto.setTotalItems(totalItems);
            
            logger.debug("DEBUG: Panier créé avec {} articles, montant total: {}", totalItems, totalAmount);
            return cartDto;
        } catch (Exception e) {
            logger.error("ERROR: Erreur dans getCart de GuestCartService", e);
            return createEmptyCart();
        }
    }
    
    private CartDto createEmptyCart() {
        CartDto cartDto = new CartDto();
        cartDto.setId(null);
        cartDto.setItems(new ArrayList<>());
        cartDto.setTotalAmount(BigDecimal.ZERO);
        cartDto.setTotalItems(0);
        return cartDto;
    }
    
    public CartItemDto addToCart(HttpSession session, AddToCartRequest request) {
        // Charger le produit avec images pour renvoyer un DTO complet (image principale incluse)
        Product product = productRepository.findByIdWithPromotions(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + request.getProductId()));
        
        // Vérifier le stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Stock insuffisant pour le produit: " + product.getName());
        }
        
        Map<String, GuestCartItem> cartItems = getCartFromSession(session);
        String productKey = String.valueOf(request.getProductId());
        
        GuestCartItem cartItem = cartItems.get(productKey);
        if (cartItem != null) {
            // Mettre à jour la quantité
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Stock insuffisant pour le produit: " + product.getName());
            }
            
            cartItem.setQuantity(newQuantity);
        } else {
            // Créer un nouvel article
            cartItem = new GuestCartItem();
            cartItem.setId(productKey);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItems.put(productKey, cartItem);
        }
        
        // Créer le DTO de retour
        CartItemDto itemDto = new CartItemDto();
        itemDto.setId(Long.valueOf(cartItem.getId()));
        
        // Créer un ProductDisplayDto pour le produit
        com.bacoge.constructionmaterial.dto.client.ProductDisplayDto productDto = 
            com.bacoge.constructionmaterial.dto.client.ProductDisplayDto.fromProduct(product);
        itemDto.setProduct(productDto);
        
        itemDto.setQuantity(cartItem.getQuantity());
        itemDto.setUnitPrice(product.getPrice());
        itemDto.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        
        return itemDto;
    }
    
    public CartItemDto updateCartItem(HttpSession session, String itemId, UpdateCartItemRequest request) {
        Map<String, GuestCartItem> cartItems = getCartFromSession(session);
        GuestCartItem cartItem = cartItems.get(itemId);
        
        if (cartItem == null) {
            throw new RuntimeException("Article du panier non trouvé");
        }
        
        // Charger le produit avec ses images
        Product product = productRepository.findByIdWithPromotions(cartItem.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
        
        // Vérifier le stock
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Stock insuffisant pour le produit: " + product.getName());
        }
        
        cartItem.setQuantity(request.getQuantity());
        
        // Créer le DTO de retour
        CartItemDto itemDto = new CartItemDto();
        itemDto.setId(Long.valueOf(cartItem.getId()));
        
        // Créer un ProductDisplayDto pour le produit
        com.bacoge.constructionmaterial.dto.client.ProductDisplayDto productDto = 
            com.bacoge.constructionmaterial.dto.client.ProductDisplayDto.fromProduct(product);
        itemDto.setProduct(productDto);
        
        itemDto.setQuantity(cartItem.getQuantity());
        itemDto.setUnitPrice(product.getPrice());
        itemDto.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        
        return itemDto;
    }
    
    public void removeCartItem(HttpSession session, String itemId) {
        Map<String, GuestCartItem> cartItems = getCartFromSession(session);
        cartItems.remove(itemId);
    }
    
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    /**
     * Expose les articles du panier invité pour permettre une fusion vers le panier utilisateur
     */
    public java.util.List<GuestCartItem> getGuestCartItems(HttpSession session) {
        Map<String, GuestCartItem> cartItems = getCartFromSession(session);
        return new java.util.ArrayList<>(cartItems.values());
    }
    
    public int getCartItemCount(HttpSession session) {
        Map<String, GuestCartItem> cartItems = getCartFromSession(session);
        return cartItems.values().stream()
                .mapToInt(GuestCartItem::getQuantity)
                .sum();
    }
    
    // Classe interne pour représenter un article du panier invité
    public static class GuestCartItem {
        private String id;
        private Long productId;
        private int quantity;
        
        // Constructeurs
        public GuestCartItem() {}
        
        public GuestCartItem(String id, Long productId, int quantity) {
            this.id = id;
            this.productId = productId;
            this.quantity = quantity;
        }
        
        // Getters et Setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.client.AddToCartRequest;
import com.bacoge.constructionmaterial.dto.client.CartDto;
import com.bacoge.constructionmaterial.dto.client.CartItemDto;
import com.bacoge.constructionmaterial.dto.client.UpdateCartItemRequest;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.service.AuthService;
import com.bacoge.constructionmaterial.service.ClientCartService;
import com.bacoge.constructionmaterial.service.GuestCartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/unified-cart")

public class UnifiedCartController {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedCartController.class);
    
    private final ClientCartService clientCartService;
    private final GuestCartService guestCartService;
    private final AuthService authService;
    
    public UnifiedCartController(ClientCartService clientCartService, GuestCartService guestCartService, AuthService authService) {
        this.clientCartService = clientCartService;
        this.guestCartService = guestCartService;
        this.authService = authService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
        try {
            logger.debug("getCart called - Session ID: {}", session != null ? session.getId() : "null");
            
            CartDto cart;
            boolean isUserAuthenticated = isAuthenticated();
            logger.debug("User authenticated: {}", isUserAuthenticated);
            
            if (isUserAuthenticated) {
                Long userId = getCurrentUserId();
                logger.debug("User ID: {}", userId);
                // Fusionner le panier invité si nécessaire
                try {
                    java.util.List<GuestCartService.GuestCartItem> guestItems = guestCartService.getGuestCartItems(session);
                    if (guestItems != null && !guestItems.isEmpty() && userId != null) {
                        logger.info("Merging {} guest cart item(s) into user {} cart", guestItems.size(), userId);
                        clientCartService.mergeGuestItems(userId, guestItems);
                        guestCartService.clearCart(session);
                    }
                } catch (Exception mergeEx) {
                    logger.warn("Guest cart merge skipped: {}", mergeEx.getMessage());
                }
                if (userId != null) {
                    cart = clientCartService.getCartByUserId(userId);
                } else {
                    // Fallback vers le panier invité si l'utilisateur n'est pas trouvé
                    logger.warn("User is authenticated but user ID is null, falling back to guest cart");
                    cart = guestCartService.getCart(session);
                }
            } else {
                logger.debug("Using guest cart");
                cart = guestCartService.getCart(session);
            }
            
            // Ensure cart is never null
            if (cart == null) {
                logger.warn("Cart is null, creating empty cart");
                cart = new CartDto();
                cart.setItems(new java.util.ArrayList<>());
                cart.setTotalAmount(java.math.BigDecimal.ZERO);
                cart.setTotalItems(0);
            }
            
            // Structure de réponse cohérente
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cart", cart);
            response.put("isAuthenticated", isUserAuthenticated);
            
            // Ajouter des informations utilisateur si connecté
            if (isUserAuthenticated) {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    Map<String, String> userInfo = new HashMap<>();
                    userInfo.put("firstName", currentUser.getFirstName());
                    userInfo.put("lastName", currentUser.getLastName());
                    userInfo.put("email", currentUser.getEmail());
                    response.put("user", userInfo);
                    
                    // Messages personnalisés pour les utilisateurs connectés
                    if (cart.getTotalItems() > 0) {
                        response.put("message", "Bonjour " + currentUser.getFirstName() + ", votre panier contient " + cart.getTotalItems() + " article(s)");
                    } else {
                        response.put("message", "Bonjour " + currentUser.getFirstName() + ", votre panier est vide. Découvrez nos produits !");
                    }
                }
            } else {
                // Messages pour les invités
                if (cart.getTotalItems() > 0) {
                    response.put("message", "Votre panier contient " + cart.getTotalItems() + " article(s). Connectez-vous pour sauvegarder votre panier !");
                } else {
                    response.put("message", "Votre panier est vide. Connectez-vous pour une expérience personnalisée !");
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Exception in getCart: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erreur lors du chargement du panier");
            errorResponse.put("cart", createEmptyCart());
            errorResponse.put("isAuthenticated", false);
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    private CartDto createEmptyCart() {
        CartDto emptyCart = new CartDto();
        emptyCart.setItems(new java.util.ArrayList<>());
        emptyCart.setTotalAmount(java.math.BigDecimal.ZERO);
        emptyCart.setTotalItems(0);
        return emptyCart;
    }
    
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@Valid @RequestBody AddToCartRequest request, HttpSession session) {
        try {
            CartItemDto cartItem;
            int cartItemCount;
            boolean isUserAuthenticated = isAuthenticated();
            
            if (isUserAuthenticated) {
                Long userId = getCurrentUserId();
                if (userId != null) {
                    cartItem = clientCartService.addToCart(userId, request);
                    cartItemCount = clientCartService.getCartItemCount(userId);
                } else {
                    // Fallback vers le panier invité si l'utilisateur n'est pas trouvé
                    cartItem = guestCartService.addToCart(session, request);
                    cartItemCount = guestCartService.getCartItemCount(session);
                }
            } else {
                cartItem = guestCartService.addToCart(session, request);
                cartItemCount = guestCartService.getCartItemCount(session);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartItem", cartItem);
            response.put("cartItemCount", cartItemCount);
            response.put("isAuthenticated", isUserAuthenticated);
            
            // Messages personnalisés selon le statut d'authentification
            if (isUserAuthenticated) {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    response.put("message", "Produit ajouté avec succès, " + currentUser.getFirstName() + " !");
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("firstName", currentUser.getFirstName());
                    response.put("user", userInfo);
                } else {
                    response.put("message", "Produit ajouté au panier avec succès");
                }
            } else {
                response.put("message", "Produit ajouté au panier. Connectez-vous pour sauvegarder votre panier !");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("isAuthenticated", isAuthenticated());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @PathVariable String itemId, 
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpSession session) {
        try {
            CartItemDto cartItem;
            int cartItemCount;
            
            if (isAuthenticated()) {
                Long userId = getCurrentUserId();
                cartItem = clientCartService.updateCartItem(userId, Long.valueOf(itemId), request);
                cartItemCount = clientCartService.getCartItemCount(userId);
            } else {
                cartItem = guestCartService.updateCartItem(session, itemId, request);
                cartItemCount = guestCartService.getCartItemCount(session);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Article mis à jour avec succès");
            response.put("cartItem", cartItem);
            response.put("cartItemCount", cartItemCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> removeCartItem(@PathVariable String itemId, HttpSession session) {
        try {
            int cartItemCount;
            
            if (isAuthenticated()) {
                Long userId = getCurrentUserId();
                clientCartService.removeCartItem(userId, Long.valueOf(itemId));
                cartItemCount = clientCartService.getCartItemCount(userId);
            } else {
                guestCartService.removeCartItem(session, itemId);
                cartItemCount = guestCartService.getCartItemCount(session);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Article supprimé du panier avec succès");
            response.put("cartItemCount", cartItemCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        try {
            if (isAuthenticated()) {
                Long userId = getCurrentUserId();
                clientCartService.clearCart(userId);
            } else {
                guestCartService.clearCart(session);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Panier vidé avec succès");
            response.put("cartItemCount", 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Alias pour correspondre au frontend API.cart.clear() => DELETE /api/v1/unified-cart
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCartAlias(HttpSession session) {
        return clearCart(session);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCartItemCount(HttpSession session) {
        try {
            int count;
            boolean isAuth = isAuthenticated();
            Long userId = getCurrentUserId();
            
            logger.info("Cart count request - isAuthenticated: {}, userId: {}, sessionId: {}", 
                       isAuth, userId, session.getId());
            
            if (isAuth) {
                if (userId != null) {
                    count = clientCartService.getCartItemCount(userId);
                    logger.info("Authenticated user cart count: {} for userId: {}", count, userId);
                } else {
                    // Fallback vers le panier invité si l'utilisateur n'est pas trouvé
                    count = guestCartService.getCartItemCount(session);
                    logger.warn("Authenticated user but no userId found, using guest cart. Count: {}", count);
                }
            } else {
                count = guestCartService.getCartItemCount(session);
                logger.info("Guest user cart count: {} for session: {}", count, session.getId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            
            logger.info("Returning cart count response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting cart item count", e);
            // En cas d'erreur, retourner 0
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }
    }
    
    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !authentication.getName().equals("anonymousUser");
    }
    
    private Long getCurrentUserId() {
        try {
            User currentUser = authService.getCurrentUser();
            return currentUser != null ? currentUser.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

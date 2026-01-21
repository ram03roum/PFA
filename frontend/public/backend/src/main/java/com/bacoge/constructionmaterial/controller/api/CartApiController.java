package com.bacoge.constructionmaterial.controller.api;

import com.bacoge.constructionmaterial.service.ClientCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")

public class CartApiController {
    
    private final ClientCartService clientCartService;
    
    public CartApiController(ClientCartService clientCartService) {
        this.clientCartService = clientCartService;
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("items", clientCartService.getCartItems());
            response.put("total", clientCartService.getCartTotal());
            response.put("count", clientCartService.getCartItemCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération du panier");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            
            clientCartService.addToCart(productId, quantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Produit ajouté au panier");
            response.put("count", clientCartService.getCartItemCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de l'ajout au panier");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCartCount() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("count", clientCartService.getCartItemCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération du nombre d'articles");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}

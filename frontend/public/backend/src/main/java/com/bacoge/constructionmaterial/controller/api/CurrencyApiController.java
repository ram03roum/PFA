package com.bacoge.constructionmaterial.controller.api;

import com.bacoge.constructionmaterial.service.CurrencyService;
import com.bacoge.constructionmaterial.service.ClientProductService;
import com.bacoge.constructionmaterial.dto.client.ProductDisplayDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")

public class CurrencyApiController {
    
    private final CurrencyService currencyService;
    private final ClientProductService productService;
    
    public CurrencyApiController(CurrencyService currencyService, ClientProductService productService) {
        this.currencyService = currencyService;
        this.productService = productService;
    }
    
    /**
     * Convertit les prix de plusieurs produits vers une devise spécifique
     */
    @PostMapping("/products/prices/convert")
    public ResponseEntity<Map<String, BigDecimal>> convertProductPrices(
            @RequestBody ConvertPricesRequest request) {
        
        Map<String, BigDecimal> convertedPrices = new HashMap<>();
        
        for (Long productId : request.getProductIds()) {
            try {
                ProductDisplayDto product = productService.getProductById(productId);
                convertedPrices.put(productId.toString(), product.getPrice());
            } catch (Exception e) {
                // En cas d'erreur, ignorer ce produit
                System.err.println("Erreur lors de la conversion du prix pour le produit ID " + productId + ": " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(convertedPrices);
    }
    
    /**
     * Obtient le taux de change entre deux devises
     */
    @GetMapping("/currency/rate")
    public ResponseEntity<Map<String, Object>> getExchangeRate(
            @RequestParam String from,
            @RequestParam String to) {
        
        try {
            BigDecimal rate = currencyService.getExchangeRate(from, to);
            
            Map<String, Object> response = new HashMap<>();
            response.put("from", from);
            response.put("to", to);
            response.put("rate", rate);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erreur lors de la récupération du taux de change");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Obtient la liste des devises supportées
     */
    @GetMapping("/currency/supported")
    public ResponseEntity<Map<String, String>> getSupportedCurrencies() {
        return ResponseEntity.ok(currencyService.getSupportedCurrencies());
    }
    
    /**
     * Obtient la devise par défaut
     */
    @GetMapping("/currency/default")
    public ResponseEntity<Map<String, String>> getDefaultCurrency() {
        Map<String, String> response = new HashMap<>();
        response.put("currency", currencyService.getDefaultCurrency());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Classe pour la requête de conversion de prix
     */
    public static class ConvertPricesRequest {
        private List<Long> productIds;
        private String currency;
        
        public ConvertPricesRequest() {}
        
        public List<Long> getProductIds() {
            return productIds;
        }
        
        public void setProductIds(List<Long> productIds) {
            this.productIds = productIds;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }
}

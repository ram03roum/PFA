package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.ChatRequest;
import com.bacoge.constructionmaterial.dto.ChatResponse;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
@RequiredArgsConstructor
public class ChatbotController {

    private final com.bacoge.constructionmaterial.service.OpenAIService openAIService;
    private final ProductService productService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        String userMessage = request.getMessage();
        
        // 1. RAG Amélioré : Recherche dédupliquée et limitée
        String contextInfo = "";
        try {
            // Extraction des mots-clés (> 3 lettres)
            String[] words = userMessage.split("\\s+");
            java.util.Set<Long> addedProductIds = new java.util.HashSet<>();
            java.util.List<Product> allFoundProducts = new java.util.ArrayList<>();

            for (String word : words) {
                if (word.length() > 3) {
                    List<Product> products = productService.searchProducts(word);
                    for (Product p : products) {
                        if (addedProductIds.add(p.getId())) { // add returns true if set didn't already contain the element
                            allFoundProducts.add(p);
                        }
                    }
                }
            }
            
            if (!allFoundProducts.isEmpty()) {
                // Limiter à 5 produits pertinents au total pour la performance et la clarté
                List<Product> topProducts = allFoundProducts.stream().limit(5).collect(Collectors.toList());
                
                StringBuilder contextBuilder = new StringBuilder();
                for (Product p : topProducts) {
                    contextBuilder.append(String.format("- %s : %.2f € (Dispo: %s)\n", 
                        p.getName(), 
                        p.getPrice(), 
                        p.getStockQuantity() > 0 ? "Oui" : "Non"));
                }
                contextInfo = contextBuilder.toString();
            }
        } catch (Exception e) {
            System.err.println("Erreur RAG: " + e.getMessage());
        }

        // 2. Appel à l'IA avec le contexte enrichi
        String botResponse = openAIService.getChatResponse(userMessage, contextInfo);

        return ResponseEntity.ok(new ChatResponse(botResponse));
    }
}

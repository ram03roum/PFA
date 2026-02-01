package com.bacoge.constructionmaterial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/client/test")
public class TestDataController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/add-sample-images")
    public ResponseEntity<Map<String, Object>> addSampleImages() {
        try {
            // Supprimer les anciennes images du produit 21 s'il y en a
            jdbcTemplate.update("DELETE FROM product_images WHERE product_id = ?", 21);
            
            // Ajouter des images de test pour le produit 21
            jdbcTemplate.update(
                "INSERT INTO product_images (product_id, image_url, is_main, display_order) VALUES (?, ?, ?, ?)",
                21, "/img/products/tole-main.jpg", true, 1
            );
            
            jdbcTemplate.update(
                "INSERT INTO product_images (product_id, image_url, is_main, display_order) VALUES (?, ?, ?, ?)",
                21, "/img/products/tole-detail-1.jpg", false, 2
            );
            
            jdbcTemplate.update(
                "INSERT INTO product_images (product_id, image_url, is_main, display_order) VALUES (?, ?, ?, ?)",
                21, "/img/products/tole-detail-2.jpg", false, 3
            );
            
            // Vérifier les images ajoutées
            int imageCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_images WHERE product_id = ?", 
                Integer.class, 21
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Images de test ajoutées avec succès");
            response.put("productId", 21);
            response.put("imagesAdded", imageCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de l'ajout des images: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

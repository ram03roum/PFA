package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.model.Category;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.repository.CategoryRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TestImageService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public void addTestImagesForProduct21() {
        try {
            // Vérifier si le produit 21 existe
            Optional<Product> existingProduct = productRepository.findById(21L);

            if (existingProduct.isEmpty()) {
                // Créer le produit 21 s'il n'existe pas
                createTestProduct21();
                System.out.println("✅ Produit 21 créé");
            }

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

            System.out.println("✅ Images de test ajoutées pour le produit 21");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout des images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTestProduct21() {
        try {
            // Créer une catégorie de test si elle n'existe pas
            Category testCategory;
            Optional<Category> existingCategory = categoryRepository.findByName("Test Category");
            if (existingCategory.isPresent()) {
                testCategory = existingCategory.get();
            } else {
                testCategory = new Category();
                testCategory.setName("Test Category");
                testCategory.setDescription("Catégorie de test pour les images");
                testCategory.setStatus(Category.CategoryStatus.ACTIVE);
                testCategory = categoryRepository.save(testCategory);
            }

            // Créer le produit 21
            Product product = new Product();
            product.setId(21L); // Forcer l'ID à 21
            product.setName("Tôle Ondulée Test");
            product.setDescription("Produit de test pour les images - Tôle ondulée en acier galvanisé");
            product.setPrice(new BigDecimal("45.00"));
            product.setStockQuantity(100);
            product.setCategory(testCategory);
            product.setSku("TEST-21");
            product.setWeightKg(new BigDecimal("5.2"));
            product.setDimensions("200x100x0.5 cm");
            product.setBrand("Bacoge Test");
            product.setStatus(Product.ProductStatus.ACTIVE);
            product.setMaterial("Acier galvanisé");
            product.setStyle("Industriel");
            product.setRoom("Extérieur");
            product.setColor("Gris argenté");
            product.setUnit("pièce");

            productRepository.save(product);
            System.out.println("✅ Produit de test créé avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du produit de test: " + e.getMessage());
            throw new RuntimeException("Impossible de créer le produit de test", e);
        }
    }
}

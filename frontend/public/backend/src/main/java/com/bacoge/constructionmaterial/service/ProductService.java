package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.ProductRequest;
import com.bacoge.constructionmaterial.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    // Vérification d'existence
    boolean existsById(Long productId);
    
    // Opérations CRUD
    List<Product> findAll();
    Optional<Product> findById(Long id);
    Product createProduct(ProductRequest productRequest);
    Product updateProduct(Long id, ProductRequest productRequest);
    void deleteProduct(Long id);
    
    // Gestion des images
    void addImageToProduct(Long productId, String imageUrl, boolean isMain);
    void removeImageFromProduct(Long productId, Long imageId);
    void setMainProductImage(Long productId, Long imageId);
    
    // Autres méthodes de recherche
    List<Product> searchProducts(String query);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findFeaturedProducts();
    
    // Gestion du stock
    boolean isInStock(Long productId, int quantity);
    void updateStock(Long productId, int quantityChange);
    
    // Statistiques
    long countProducts();
    long countProductsInCategory(Long categoryId);
}

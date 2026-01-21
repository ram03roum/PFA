package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    
    @Override
    boolean existsById(@org.springframework.lang.NonNull Long id);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.images " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithPromotions(@Param("id") Long id);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.promotions pr " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithPromotionsOnly(@Param("id") Long id);
    
    @Query("SELECT p FROM Product p WHERE p.sku = :sku")
    Optional<Product> findBySku(@Param("sku") String sku);
    
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.sku = :sku")
    boolean existsBySku(@Param("sku") String sku);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.id <> :productId AND p.status = 'ACTIVE'")
    List<Product> findActiveRelatedProducts(@Param("categoryId") Long categoryId, @Param("productId") Long productId, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = :status")
    Page<Product> findByStatus(@Param("status") Product.ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.style) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findByNameContainingIgnoreCaseOrStyleContainingIgnoreCase(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<Product> findByStatusOrderByCreatedAtDesc(@Param("status") Product.ProductStatus status);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId AND p.status = :status")
    List<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") Product.ProductStatus status);
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))")
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(@Param("name") String name, @Param("description") String description);
    
    @Query("SELECT p FROM Product p WHERE p.featured = true")
    List<Product> findByFeaturedTrue();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId AND p.status = :status")
    Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") Product.ProductStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
           "p.status = :status")
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndStatus(
            @Param("name") String name, @Param("description") String description, @Param("status") Product.ProductStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :minStockLevel")
    List<Product> findByStockQuantityLessThanEqual(@Param("minStockLevel") Integer minStockLevel);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.stockQuantity <= p.minStockLevel")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findProductsWithFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            @Param("status") Product.ProductStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    Page<Product> findActiveProducts(Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.images")
    Page<Product> findAllWithImages(Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
    long countByStatus(@Param("status") Product.ProductStatus status);
    
    @Query("SELECT SUM(p.stockQuantity) FROM Product p")
    Long getTotalStockQuantity();
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.status = :status ORDER BY p.stockQuantity DESC")
    List<Product> findByStatusOrderByStockQuantityDesc(@Param("status") Product.ProductStatus status);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.images " +
           "JOIN p.promotions pr " +
           "WHERE p.status = 'ACTIVE' " +
           "AND pr.status = 'ACTIVE' " +
           "AND (pr.startDate IS NULL OR pr.startDate <= :now) " +
           "AND (pr.endDate IS NULL OR pr.endDate >= :now)")
    List<Product> findProductsWithActivePromotions(@Param("now") java.time.LocalDateTime now);
    
    // Méthodes pour les statistiques du dashboard
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category ORDER BY p.createdAt DESC")
    List<Product> findTopByOrderByCreatedAtDesc(Pageable pageable);
    
    default List<Product> findTopByOrderByCreatedAtDesc(int limit) {
        return findTopByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }
    
    @Query("SELECT p.status, COUNT(p) FROM Product p GROUP BY p.status")
    List<Object[]> countProductsByStatusRaw();
    
    @Query("SELECT c.name, SUM(oi.quantity * oi.price) as totalSales, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi JOIN oi.product p JOIN p.category c JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY c.id, c.name")
    List<Object[]> getSalesByCategory();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= p.minStockLevel")
    long countLowStockProducts();
    
    @Query("SELECT p.category.name, COUNT(p) as productCount, SUM(p.stockQuantity) as totalStock " +
           "FROM Product p GROUP BY p.category.name")
    List<Object[]> getProductStatsByCategory();
    
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findNewestProducts(Pageable pageable);
    
    default List<Product> findNewestProducts(int limit) {
        return findNewestProducts(PageRequest.of(0, limit));
    }
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    List<Product> findActiveProducts();
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cart_items WHERE product_id = :productId", nativeQuery = true)
    void deleteCartItemsByProductId(@Param("productId") Long productId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM promotion_products WHERE product_id = :productId", nativeQuery = true)
    void deletePromotionProductsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.price DESC")
    List<Product> findTopRatedProducts(Pageable pageable);
    
    default List<Product> findTopRatedProducts(int limit) {
        return findTopRatedProducts(PageRequest.of(0, limit));
    }
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.price DESC")
    List<Product> findDiscountedProducts(Pageable pageable);
    
    default List<Product> findDiscountedProducts(int limit) {
        return findDiscountedProducts(PageRequest.of(0, limit));
    }
    
    // Requête optimisée pour les statistiques produits - récupère toutes les stats en une seule requête
    @Query("SELECT " +
           "COUNT(p), " +
           "SUM(CASE WHEN p.status = 'ACTIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN p.status = 'INACTIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN p.status = 'OUT_OF_STOCK' THEN 1 ELSE 0 END), " +
           "SUM(p.stockQuantity), " +
           "SUM(CASE WHEN p.stockQuantity <= p.minStockLevel THEN 1 ELSE 0 END), " +
           "AVG(p.price) " +
           "FROM Product p")
    Object[] getProductStatsBatch();
    
    // Requête pour les produits ajoutés aujourd'hui
    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdAt >= :startOfDay AND p.createdAt < :endOfDay")
    long countNewProductsToday(@Param("startOfDay") java.time.LocalDateTime startOfDay, @Param("endOfDay") java.time.LocalDateTime endOfDay);
    
    default Map<Product.ProductStatus, Long> countProductsByStatusMap() {
        List<Object[]> results = countProductsByStatusRaw();
        return results.stream().collect(Collectors.toMap(
            result -> (Product.ProductStatus) result[0],
            result -> (Long) result[1]
        ));
    }
    
    // Méthode pour les produits les plus vendus (données de test pour l'instant)
    default List<Map<String, Object>> findTopSellingProducts(int limit) {
        List<Product> products = findTopByOrderByCreatedAtDesc(limit);
        return products.stream().map(product -> {
            Map<String, Object> productData = new HashMap<>();
            productData.put("name", product.getName());
            productData.put("label", product.getName());
            productData.put("sales", (int)(Math.random() * 100) + 10); // Données de test
            productData.put("value", (int)(Math.random() * 100) + 10); // Données de test
            return productData;
        }).collect(Collectors.toList());
    }
}
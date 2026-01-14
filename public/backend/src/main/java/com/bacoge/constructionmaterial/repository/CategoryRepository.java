package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c WHERE c.name = :name")
    Optional<Category> findByName(@Param("name") String name);
    
    @Query("SELECT c FROM Category c WHERE c.status = :status")
    List<Category> findByStatus(@Param("status") Category.CategoryStatus status);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.status = 'ACTIVE'")
    Long countActiveProductsByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT c FROM Category c WHERE c.status = 'ACTIVE' ORDER BY c.name ASC")
    List<Category> findActiveCategories();
    
    @Query("SELECT c FROM Category c WHERE c.status = 'ACTIVE'")
    List<Category> findAllActiveCategories();
    
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT COUNT(c) FROM Category c WHERE c.status = :status")
    Long countByStatus(@Param("status") Category.CategoryStatus status);
    
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name")
    boolean existsByName(@Param("name") String name);
    
    // Méthode pour la distribution des ventes par catégorie (données de test)
    default List<Map<String, Object>> findCategorySalesDistribution() {
        List<Category> categories = findAllActiveCategories();
        return categories.stream().map(category -> {
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("name", category.getName());
            categoryData.put("value", (int)(Math.random() * 50) + 10); // Données de test
            return categoryData;
        }).collect(Collectors.toList());
    }
}
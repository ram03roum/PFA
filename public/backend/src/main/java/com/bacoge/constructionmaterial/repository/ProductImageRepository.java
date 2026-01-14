package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.displayOrder ASC, pi.id ASC")
    List<ProductImage> findByProductIdOrderByDisplayOrderAscIdAsc(@Param("productId") Long productId);
    
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isMain = false WHERE pi.product.id = :productId AND pi.isMain = true")
    void clearMainImageForProduct(@Param("productId") Long productId);
    
    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    // Fetch only the main image URL for quick display without loading the whole collection
    @Query("SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product.id = :productId AND pi.isMain = true")
    String findMainImageUrlByProductId(@Param("productId") Long productId);

    // Fallback: fetch first image URL by display order
    @Query("SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.displayOrder ASC, pi.id ASC")
    List<String> findAllImageUrlsByProductIdOrder(@Param("productId") Long productId);
}

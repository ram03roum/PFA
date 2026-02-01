package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r WHERE r.productId = :productId")
    List<Review> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.userId = :userId")
    List<Review> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.isApproved = true")
    Double findGlobalAverageRating();

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.isApproved = true")
    Double findAverageRatingForProduct(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isApproved = true")
    Long countApprovedByProductId(@Param("productId") Long productId);

    Page<Review> findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(Long productId, Pageable pageable);
    
    // Méthodes pour l'administration
    Page<Review> findByIsApprovedOrderByCreatedAtDesc(Boolean isApproved, Pageable pageable);
    
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    long countByIsApproved(Boolean isApproved);
}
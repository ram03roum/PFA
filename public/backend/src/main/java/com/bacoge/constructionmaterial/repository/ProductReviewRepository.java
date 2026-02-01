package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    // Trouver tous les avis d'un produit
    Page<ProductReview> findByProductIdAndIsApprovedOrderByCreatedAtDesc(Long productId, Boolean isApproved, Pageable pageable);

    // Trouver tous les avis d'un produit (approuvés uniquement)
    List<ProductReview> findByProductIdAndIsApprovedOrderByCreatedAtDesc(Long productId, Boolean isApproved);

    // Compter les avis d'un produit
    long countByProductIdAndIsApproved(Long productId, Boolean isApproved);

    // Calculer la note moyenne d'un produit
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.isApproved = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    // Trouver les avis d'un utilisateur
    List<ProductReview> findByUserIdAndIsApprovedOrderByCreatedAtDesc(Long userId, Boolean isApproved);

    // Trouver les avis en attente d'approbation
    Page<ProductReview> findByIsApprovedOrderByCreatedAtDesc(Boolean isApproved, Pageable pageable);

    // Compter les avis en attente d'approbation
    long countByIsApproved(Boolean isApproved);
}

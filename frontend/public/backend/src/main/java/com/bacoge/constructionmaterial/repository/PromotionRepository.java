package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    @Query("SELECT p FROM Promotion p WHERE p.promoCode = :promoCode")
    Optional<Promotion> findByPromoCode(@Param("promoCode") String promoCode);
    
    @Query("SELECT COUNT(p) > 0 FROM Promotion p WHERE p.promoCode = :promoCode")
    boolean existsByPromoCode(@Param("promoCode") String promoCode);
    
    @Query("SELECT p FROM Promotion p WHERE p.status = :status")
    List<Promotion> findByStatus(@Param("status") Promotion.PromotionStatus status);
    
    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' AND " +
           "(p.startDate IS NULL OR p.startDate <= :now) AND " +
           "(p.endDate IS NULL OR p.endDate >= :now)")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' AND " +
           "(p.startDate IS NULL OR p.startDate <= :now) AND " +
           "(p.endDate IS NULL OR p.endDate >= :now) AND " +
           "(p.maxUses IS NULL OR p.currentUses < p.maxUses)")
    List<Promotion> findValidPromotions(@Param("now") LocalDateTime now);
    
    @Query("SELECT p FROM Promotion p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<Promotion> findPromotionsWithFilters(
            @Param("name") String name,
            @Param("status") Promotion.PromotionStatus status
    );
    
    @Query("SELECT p FROM Promotion p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Promotion> findPromotionsWithFiltersPaginated(
            @Param("name") String name,
            @Param("status") Promotion.PromotionStatus status,
            Pageable pageable
    );
    
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.status = :status")
long countByStatus(@Param("status") Promotion.PromotionStatus status);

@Query("SELECT COUNT(p) FROM Promotion p WHERE p.status = 'ACTIVE' AND (p.startDate IS NULL OR p.startDate <= :now) AND (p.endDate IS NULL OR p.endDate >= :now)")
long countActivePromotions(@Param("now") LocalDateTime now);

@Query("SELECT COUNT(p) FROM Promotion p WHERE p.status = 'ACTIVE' AND p.endDate < :now")
long countExpiredPromotions(@Param("now") LocalDateTime now);
}
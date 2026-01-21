package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    @Query("SELECT sm FROM StockMovement sm ORDER BY sm.createdAt DESC LIMIT 10")
    List<StockMovement> findTop10ByOrderByCreatedAtDesc();
}
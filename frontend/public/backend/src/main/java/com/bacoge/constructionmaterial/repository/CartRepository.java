package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);
}
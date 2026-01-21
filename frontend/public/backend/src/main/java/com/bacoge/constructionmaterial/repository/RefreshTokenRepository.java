package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token")
    Optional<RefreshToken> findByToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteByUser_Id(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId")
    long countByUser_Id(@Param("userId") Long userId);
}

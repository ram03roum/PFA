package com.bacoge.constructionmaterial.repository;

import com.bacoge.constructionmaterial.entity.Address;
import com.bacoge.constructionmaterial.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    /**
     * Trouve toutes les adresses d'un utilisateur
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(@Param("user") User user);
    
    /**
     * Trouve une adresse par ID et utilisateur
     */
    @Query("SELECT a FROM Address a WHERE a.id = :id AND a.user = :user")
    Optional<Address> findByIdAndUser(@Param("id") Long id, @Param("user") User user);
    
    /**
     * Trouve l'adresse par défaut d'un utilisateur
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isDefault = true")
    Optional<Address> findByUserAndIsDefaultTrue(@Param("user") User user);
    
    /**
     * Compte le nombre d'adresses d'un utilisateur
     */
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user = :user")
    long countByUser(@Param("user") User user);
    
    /**
     * Supprime toutes les adresses par défaut d'un utilisateur
     * (utilisé avant de définir une nouvelle adresse par défaut)
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user = :user")
    void clearDefaultAddresses(@Param("user") User user);
    
    /**
     * Vérifie si un utilisateur a une adresse par défaut
     */
    @Query("SELECT COUNT(a) > 0 FROM Address a WHERE a.user = :user AND a.isDefault = true")
    boolean existsByUserAndIsDefaultTrue(@Param("user") User user);
    
    /**
     * Trouve les adresses par type pour un utilisateur
     */
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.type = :type")
    List<Address> findByUserAndType(@Param("user") User user, @Param("type") Address.AddressType type);
}
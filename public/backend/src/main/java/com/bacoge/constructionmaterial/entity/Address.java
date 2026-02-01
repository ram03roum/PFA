package com.bacoge.constructionmaterial.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = true)
    private String name;
    
    @Column(name = "address_name", nullable = true)
    private String addressName;
    
    @Column(nullable = false)
    private String street;
    
    @Column(nullable = false)
    private String city;
    
    @Column(name = "postal_code", nullable = false)
    private String postalCode;
    
    @Column(nullable = false)
    private String country;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType type;
    
    @Column(name = "is_default")
    private boolean isDefault = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private com.bacoge.constructionmaterial.model.User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enum pour les types d'adresse
    public enum AddressType {
        BILLING("Facturation"),
        SHIPPING("Livraison"),
        BOTH("Facturation et Livraison");
        
        private final String displayName;
        
        AddressType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructeurs
    public Address() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Address(String name, String street, String city, String postalCode, 
                  String country, AddressType type, com.bacoge.constructionmaterial.model.User user) {
        this.name = name;
        this.addressName = name; // Synchroniser avec address_name
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.type = type;
        this.user = user;
        this.isDefault = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Méthodes de cycle de vie
    @PrePersist
    protected void onCreate() {
        System.out.println("=== @PrePersist onCreate appelé ===");
        System.out.println("Avant synchronisation: name='" + this.name + "', addressName='" + this.addressName + "'");
        
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // S'assurer qu'au moins un des deux champs name/addressName est renseigné
        if (this.name == null && this.addressName == null) {
            throw new IllegalStateException("Au moins un des champs 'name' ou 'addressName' doit être renseigné");
        }
        
        // Synchroniser les champs si l'un est null
        if (this.name == null && this.addressName != null) {
            this.name = this.addressName;
            System.out.println("Synchronisation: name défini à '" + this.name + "'");
        } else if (this.addressName == null && this.name != null) {
            this.addressName = this.name;
            System.out.println("Synchronisation: addressName défini à '" + this.addressName + "'");
        }
        
        System.out.println("Après synchronisation: name='" + this.name + "', addressName='" + this.addressName + "'");
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        // Synchroniser avec addressName si addressName est null
        if (this.addressName == null) {
            this.addressName = name;
        }
    }
    
    public String getAddressName() {
        return addressName;
    }
    
    public void setAddressName(String addressName) {
        this.addressName = addressName;
        // Synchroniser avec name si name est null
        if (this.name == null) {
            this.name = addressName;
        }
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public AddressType getType() {
        return type;
    }
    
    public void setType(AddressType type) {
        this.type = type;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public com.bacoge.constructionmaterial.model.User getUser() {
        return user;
    }
    
    public void setUser(com.bacoge.constructionmaterial.model.User user) {
        this.user = user;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
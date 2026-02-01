package com.bacoge.constructionmaterial.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddressDto {
    private Long id;
    private String name;
    private String street;
    private String city;
    private String postalCode;
    private String country;
    private String type; // BILLING, SHIPPING, BOTH
    
    @JsonProperty("isDefault")
    private boolean isDefault;
    
    // Constructeurs
    public AddressDto() {}
    
    public AddressDto(Long id, String name, String street, String city, 
                     String postalCode, String country, String type, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.type = type;
        this.isDefault = isDefault;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
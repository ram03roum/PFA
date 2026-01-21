package com.bacoge.constructionmaterial.dto;

import com.bacoge.constructionmaterial.model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserResponse {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String country;
    private String city;
    private String address;
    private String role;
    private String status;
    private LocalDate birthDate;
    private String gender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    
    // Constructeurs
    public UserResponse() {}
    
    public UserResponse(User user) {
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.country = user.getCountry();
        this.city = user.getCity();
        this.address = user.getAddress();
        this.role = user.getRole().toString();
        this.status = user.getStatus().toString();
        this.birthDate = user.getBirthDate();
        this.gender = user.getGender() != null ? user.getGender().name() : null;
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.lastLogin = user.getLastLogin();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

package com.bacoge.constructionmaterial.dto;

import java.time.LocalDateTime;

/**
 * DTO pour les messages de contact
 */
public class ContactMessageDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userEmail; // Email de l'utilisateur connecté si applicable
    private boolean isAuthenticated;
    
    // Constructeur par défaut
    public ContactMessageDto() {}
    
    // Constructeur avec paramètres essentiels
    public ContactMessageDto(String name, String email, String phone, String subject, String message) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.subject = subject;
        this.message = message;
        this.status = "NOUVEAU";
        this.createdAt = LocalDateTime.now();
        this.isAuthenticated = false;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public boolean isAuthenticated() {
        return isAuthenticated;
    }
    
    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }
    
    // Pattern Builder
    public static class Builder {
        private ContactMessageDto dto;
        
        public Builder() {
            this.dto = new ContactMessageDto();
        }
        
        public Builder id(Long id) {
            dto.setId(id);
            return this;
        }
        
        public Builder name(String name) {
            dto.setName(name);
            return this;
        }
        
        public Builder email(String email) {
            dto.setEmail(email);
            return this;
        }
        
        public Builder phone(String phone) {
            dto.setPhone(phone);
            return this;
        }
        
        public Builder subject(String subject) {
            dto.setSubject(subject);
            return this;
        }
        
        public Builder message(String message) {
            dto.setMessage(message);
            return this;
        }
        
        public Builder status(String status) {
            dto.setStatus(status);
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            dto.setCreatedAt(createdAt);
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            dto.setUpdatedAt(updatedAt);
            return this;
        }
        
        public Builder userEmail(String userEmail) {
            dto.setUserEmail(userEmail);
            return this;
        }
        
        public Builder authenticated(boolean authenticated) {
            dto.setAuthenticated(authenticated);
            return this;
        }
        
        public ContactMessageDto build() {
            if (dto.getCreatedAt() == null) {
                dto.setCreatedAt(LocalDateTime.now());
            }
            if (dto.getStatus() == null) {
                dto.setStatus("NOUVEAU");
            }
            return dto;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
package com.bacoge.constructionmaterial.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewDto {
    private Long id;
    private String authorName;
    private String authorEmail;
    private int rating;
    private String title;
    private String comment;
    private LocalDateTime createdAt;
    private boolean verifiedPurchase;
    private List<String> pros;
    private List<String> cons;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    
    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isVerifiedPurchase() { return verifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { this.verifiedPurchase = verifiedPurchase; }
    
    public List<String> getPros() { return pros; }
    public void setPros(List<String> pros) { this.pros = pros; }
    
    public List<String> getCons() { return cons; }
    public void setCons(List<String> cons) { this.cons = cons; }
}

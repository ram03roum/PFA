package com.bacoge.constructionmaterial.dto.client;

public class TestimonialDto {
    private Long id;
    private String name;
    private String role;
    private String comment;
    private int rating;
    
    public TestimonialDto() {}
    
    public TestimonialDto(Long id, String name, String role, String comment, int rating) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.comment = comment;
        this.rating = rating;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
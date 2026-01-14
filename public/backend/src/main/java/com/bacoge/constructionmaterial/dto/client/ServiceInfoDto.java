package com.bacoge.constructionmaterial.dto.client;

import java.util.List;

public class ServiceInfoDto {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private boolean isMain;
    private List<String> features;
    
    public ServiceInfoDto() {}
    
    public ServiceInfoDto(Long id, String name, String description, String icon, boolean isMain) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isMain = isMain;
    }
    
    public ServiceInfoDto(Long id, String name, String description, String icon, boolean isMain, List<String> features) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isMain = isMain;
        this.features = features;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public boolean isMain() { return isMain; }
    public void setMain(boolean main) { isMain = main; }
    
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
}
package com.bacoge.constructionmaterial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {
    private Long id;
    
    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 200, message = "Le nom doit contenir entre 2 et 200 caractères")
    private String name;
    
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;
    
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal price;
    
    @NotNull(message = "Le stock est obligatoire")
    @Positive(message = "Le stock doit être positif")
    private Integer stockQuantity;
    
    private Integer minStockLevel = 10;
    private String sku;
    private BigDecimal weightKg;
    private String dimensions;
    private String brand;
    private String unit;
    private String style;
    private String room;
    private String color;
    private String material;
    private String collectionName;
    private String tags;
    private String longDescription;
    private String status;
    private Long categoryId;
    private List<MultipartFile> images;
    private List<Long> existingImageIds;
    private Integer mainImageIndex = 0;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public Integer getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(Integer minStockLevel) { this.minStockLevel = minStockLevel; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    
    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public String getLongDescription() { return longDescription; }
    public void setLongDescription(String longDescription) { this.longDescription = longDescription; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
    
    public List<Long> getExistingImageIds() { return existingImageIds; }
    public void setExistingImageIds(List<Long> existingImageIds) { this.existingImageIds = existingImageIds; }
    
    public Integer getMainImageIndex() { return mainImageIndex; }
    public void setMainImageIndex(Integer mainImageIndex) { this.mainImageIndex = mainImageIndex; }
}

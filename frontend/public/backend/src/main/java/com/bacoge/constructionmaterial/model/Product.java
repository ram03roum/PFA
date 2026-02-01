package com.bacoge.constructionmaterial.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 200, message = "Le nom doit contenir entre 2 et 200 caractères")
    private String name;
    
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;
    
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private BigDecimal price;
    
    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    private Integer stockQuantity;
    
    @Column(name = "min_stock_level")
    private Integer minStockLevel = 10;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();
    
    /**
     * Méthode de compatibilité pour récupérer l'URL de l'image principale
     * @return L'URL de l'image principale ou null si aucune image n'est définie
     */
    public String getImageUrl() {
        return images.stream()
            .filter(ProductImage::getIsMain)
            .findFirst()
            .map(ProductImage::getImageUrl)
            .orElse(null);
    }
    
    /**
     * Méthode de compatibilité pour définir l'URL de l'image principale
     * @param imageUrl URL de l'image à définir comme principale
     */
    public void setImageUrl(String imageUrl) {
        if (imageUrl == null) {
            // Si l'URL est null, on ne fait rien pour éviter de supprimer d'autres images
            return;
        }
        
        // Vérifier si l'image existe déjà
        Optional<ProductImage> existingImage = images.stream()
            .filter(img -> imageUrl.equals(img.getImageUrl()))
            .findFirst();
            
        if (existingImage.isPresent()) {
            // Mettre à jour l'image existante comme principale
            images.forEach(img -> img.setIsMain(false));
            existingImage.get().setIsMain(true);
        } else {
            // Créer une nouvelle image principale
            ProductImage newImage = new ProductImage();
            newImage.setImageUrl(imageUrl);
            newImage.setIsMain(true);
            newImage.setProduct(this);
            images.add(newImage);
        }
    }
    
    @Column(name = "sku", unique = true)
    private String sku;
    
    @Column(name = "weight_kg")
    private BigDecimal weightKg;
    
    @Column(name = "dimensions")
    private String dimensions;
    
    @Column(name = "brand")
    private String brand;
    
    @Column(name = "unit")
    private String unit;
    
    // Interior design specific attributes
    @Column(name = "style")
    private String style; // e.g., modern, bohemian, scandinavian

    @Column(name = "room")
    private String room; // e.g., living room, bedroom, kitchen

    @Column(name = "color")
    private String color; // dominant color name

    @Column(name = "material")
    private String material; // e.g., wood, metal, fabric, marble

    @Column(name = "collection_name")
    private String collectionName; // for curated collections

    @Column(name = "tags", length = 512)
    private String tags; // comma-separated tags, e.g., "minimalist,neutral,cozy"

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;
    
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_featured", columnDefinition = "boolean default false")
    private boolean featured = false;
    
    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, 
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @ManyToMany(mappedBy = "applicableProducts", fetch = FetchType.LAZY)
    private List<Promotion> promotions = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sku == null || sku.isEmpty()) {
            sku = generateSKU();
        }
    }
    
    // Méthodes utilitaires pour gérer les images
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }
    
    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }
    
    public Optional<ProductImage> getMainImage() {
        return images.stream()
            .filter(ProductImage::getIsMain)
            .findFirst();
    }
    
    public void setMainImage(ProductImage mainImage) {
        // Désélectionner l'ancienne image principale
        images.stream()
            .filter(ProductImage::getIsMain)
            .forEach(img -> img.setIsMain(false));
            
        // Définir la nouvelle image principale
        if (mainImage != null) {
            mainImage.setIsMain(true);
            if (!images.contains(mainImage)) {
                addImage(mainImage);
            }
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateSKU() {
        return "PROD-" + System.currentTimeMillis();
    }
    
    public boolean isInStock() {
        return stockQuantity > 0;
    }
    
    public boolean isLowStock() {
        return stockQuantity <= minStockLevel;
    }
    
    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }
    
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
    
    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }
    
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
    
    public String getLongDescription() { return longDescription; }
    public void setLongDescription(String longDescription) { this.longDescription = longDescription; }
    
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
    
    public List<Promotion> getPromotions() { return promotions; }
    public void setPromotions(List<Promotion> promotions) { this.promotions = promotions; }
    
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    
    public Long getCategoryId() { 
        return category != null ? category.getId() : null; 
    }
    
    /**
     * Gets the active promotion for this product, if any
     * @return the active promotion or null if none exists
     */
    public Promotion getPromotion() {
        if (promotions == null || promotions.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return promotions.stream()
            .filter(p -> p.getStatus() == Promotion.PromotionStatus.ACTIVE && 
                        (p.getStartDate() == null || !now.isBefore(p.getStartDate())) && 
                        (p.getEndDate() == null || !now.isAfter(p.getEndDate())))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Calculates the discounted price based on active promotions
     * @return The discounted price or the original price if no active promotion
     */
    public BigDecimal getDiscountedPrice() {
        Promotion activePromotion = getPromotion();
        if (activePromotion == null) {
            return price;
        }
        
        if (activePromotion.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
            BigDecimal discountAmount = price.multiply(activePromotion.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            return price.subtract(discountAmount);
        } else if (activePromotion.getDiscountType() == Promotion.DiscountType.FIXED_AMOUNT) {
            BigDecimal discountedPrice = price.subtract(activePromotion.getDiscountAmount());
            return discountedPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discountedPrice;
        }
        
        return price;
    }
    
    public enum ProductStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK
    }
}
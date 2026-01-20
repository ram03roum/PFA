package com.bacoge.constructionmaterial.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "promotions")
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom de la promotion est obligatoire")
    private String name;
    
    private String description;
    
    @NotNull(message = "Le pourcentage de réduction est obligatoire")
    @Positive(message = "Le pourcentage doit être positif")
    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;
    
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;
    
    @Enumerated(EnumType.STRING)
    private DiscountType discountType = DiscountType.PERCENTAGE;
    
    @Column(name = "promo_code", unique = true)
    private String promoCode;
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "min_order_amount")
    private BigDecimal minOrderAmount;
    
    @Column(name = "max_uses")
    private Integer maxUses;
    
    @Column(name = "current_uses")
    private Integer currentUses = 0;
    
    @Enumerated(EnumType.STRING)
    private PromotionStatus status = PromotionStatus.ACTIVE;
    
    @ManyToMany
    @JoinTable(
        name = "promotion_categories",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> applicableCategories;
    
    @ManyToMany
    @JoinTable(
        name = "promotion_products",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> applicableProducts;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Vérifie si la promotion est valide en fonction de plusieurs critères :
     * - Statut ACTIF
     - Date de début dépassée
     - Date de fin non atteinte
     - Limite d'utilisation non dépassée (si définie)
     - Montant de réduction valide
     * @return true si la promotion est valide, false sinon
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        
        // Vérification du statut
        if (status != PromotionStatus.ACTIVE) {
            return false;
        }
        
        // Vérification des dates (gérer les valeurs nulles)
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        
        // Vérification de la limite d'utilisation
        if (maxUses != null && currentUses >= maxUses) {
            return false;
        }
        
        // Vérification du montant de réduction
        if (discountType == DiscountType.PERCENTAGE && discountPercentage == null) {
            return false;
        }
        
        if (discountType == DiscountType.FIXED_AMOUNT && discountAmount == null) {
            return false;
        }
        
        // Pourcentage entre 0 et 100
        if (discountType == DiscountType.PERCENTAGE && 
            (discountPercentage.compareTo(BigDecimal.ZERO) <= 0 || 
             discountPercentage.compareTo(new BigDecimal("100")) > 0)) {
            return false;
        }
        
        return true;
    }
    
    public boolean canBeUsed() {
        return isValid() && currentUses < maxUses;
    }
    
    public void incrementUsage() {
        this.currentUses++;
    }
    
    /**
     * Calcule le prix après application de la promotion
     * @param originalPrice Le prix original du produit
     * @return Le prix après réduction, ou le prix original si la promotion n'est pas valide
     */
    public BigDecimal calculateDiscountedPrice(BigDecimal originalPrice) {
        // Vérification des paramètres
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix original ne peut pas être null ou négatif");
        }
        
        // Si la promotion n'est pas valide, retourner le prix original
        if (!isValid()) {
            return originalPrice;
        }
        
        try {
            BigDecimal discountedPrice;
            
            if (discountType == DiscountType.PERCENTAGE) {
                // Calcul de la réduction en pourcentage
                BigDecimal discountMultiplier = discountPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                BigDecimal discountAmount = originalPrice.multiply(discountMultiplier);
                discountedPrice = originalPrice.subtract(discountAmount);
            } else {
                // Réduction fixe
                discountedPrice = originalPrice.subtract(discountAmount);
            }
            
            // Le prix ne peut pas être négatif
            return discountedPrice.max(BigDecimal.ZERO);
            
        } catch (Exception e) {
            // En cas d'erreur de calcul, retourner le prix original
            return originalPrice;
        }
    }
    
    /**
     * Vérifie si la promotion est applicable à un produit spécifique
     * @param product Le produit à vérifier
     * @return true si la promotion est applicable, false sinon
     */
    public boolean isApplicableToProduct(Product product) {
        if (product == null) {
            return false;
        }
        
        // Vérifier si la promotion est valide
        if (!isValid()) {
            return false;
        }
        
        // Vérifier si le produit est dans la liste des produits éligibles
        if (applicableProducts != null && !applicableProducts.isEmpty()) {
            return applicableProducts.stream().anyMatch(p -> p.getId().equals(product.getId()));
        }
        
        // Vérifier si la catégorie du produit est éligible
        if (applicableCategories != null && !applicableCategories.isEmpty()) {
            if (product.getCategory() == null) {
                return false;
            }
            return applicableCategories.stream()
                .anyMatch(category -> category.getId().equals(product.getCategory().getId()));
        }
        
        // Si aucune restriction spécifique (pas de produits ni catégories définis), la promotion est applicable
        return true;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    
    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }
    
    public Integer getCurrentUses() { return currentUses; }
    public void setCurrentUses(Integer currentUses) { this.currentUses = currentUses; }
    
    public PromotionStatus getStatus() { return status; }
    public void setStatus(PromotionStatus status) { this.status = status; }
    
    public Set<Category> getApplicableCategories() { return applicableCategories; }
    public void setApplicableCategories(Set<Category> applicableCategories) { this.applicableCategories = applicableCategories; }
    
    public Set<Product> getApplicableProducts() { return applicableProducts; }
    public void setApplicableProducts(Set<Product> applicableProducts) { this.applicableProducts = applicableProducts; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }
    
    public enum PromotionStatus {
        ACTIVE, INACTIVE, EXPIRED
    }
}
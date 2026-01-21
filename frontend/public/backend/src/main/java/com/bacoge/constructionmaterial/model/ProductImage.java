package com.bacoge.constructionmaterial.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String imageUrl;
    
    @Column(nullable = false)
    private Boolean isMain = false;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    // Constructeur pour faciliter la création
    public ProductImage(String imageUrl, boolean isMain, Product product) {
        this.imageUrl = imageUrl;
        this.isMain = isMain;
        this.product = product;
    }
}

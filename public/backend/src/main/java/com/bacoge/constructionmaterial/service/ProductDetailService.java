package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.ProductDetailDto;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.Promotion;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductDetailService {

    private final ProductRepository productRepository;
    
    public ProductDetailService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Récupère les détails complets d'un produit par son ID
     * @param productId L'ID du produit à récupérer
     * @return Un DTO contenant les détails du produit
     */
    public ProductDetailDto getProductDetails(Long productId) {
        // Récupérer le produit depuis la base de données
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Produit non trouvé avec l'ID: " + productId);
        }
        
        Product product = productOpt.get();
        
        // Convertir le produit en DTO
        return convertToDto(product);
    }
    
    /**
     * Convertit une entité Product en ProductDetailDto
     */
    private ProductDetailDto convertToDto(Product product) {
        ProductDetailDto dto = new ProductDetailDto();
        
        // Copier les propriétés de base
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        
        // Ajouter une description courte basée sur la description complète si nécessaire
        String shortDesc = product.getDescription() != null && product.getDescription().length() > 100 ?
                         product.getDescription().substring(0, 100) : 
                         product.getDescription();
        dto.setShortDescription(shortDesc != null ? shortDesc : "");
        
        // Gérer les promotions
        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountedPrice = originalPrice;
        boolean onSale = false;
        
        // Vérifier s'il y a des promotions applicables
        if (product.getPromotions() != null && !product.getPromotions().isEmpty()) {
            // Trouver la première promotion valide pour ce produit
            Optional<Promotion> activePromotion = product.getPromotions().stream()
                .filter(Promotion::isValid)
                .filter(p -> p.isApplicableToProduct(product))
                .findFirst();
                
            if (activePromotion.isPresent()) {
                Promotion promotion = activePromotion.get();
                discountedPrice = promotion.calculateDiscountedPrice(originalPrice);
                onSale = discountedPrice.compareTo(originalPrice) < 0;
            }
        }
        
        // Mettre à jour les informations de prix dans le DTO
        dto.setOnSale(onSale);
        dto.setOriginalPrice(originalPrice);
        dto.setPrice(onSale ? discountedPrice : originalPrice);
        
        // Autres propriétés à mapper...
        
        return dto;
    }
}

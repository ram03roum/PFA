package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.model.ProductImage;
import com.bacoge.constructionmaterial.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProductImageService {

    @Value("${app.upload.dir:${user.home}/bacoge-uploads}")
    private String uploadDir;
    
    private final ProductImageRepository productImageRepository;

    public ProductImageService(ProductImageRepository productImageRepository) {
        this.productImageRepository = productImageRepository;
    }
    
    @Transactional
    public ProductImage save(ProductImage productImage) {
        return productImageRepository.save(productImage);
    }
    
    @Transactional
    public void deleteImage(Long id) {
        productImageRepository.findById(id).ifPresent(image -> {
            // Supprimer le fichier physique
            try {
                String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir, filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log l'erreur mais continuer la suppression en base
                e.printStackTrace();
            }
            // Supprimer l'entrée en base
            productImageRepository.delete(image);
        });
    }
    
    @Transactional(readOnly = true)
    public List<ProductImage> findByProductId(Long productId) {
        return productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
    }
    
    @Transactional
    public void deleteAllByProductId(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);
        images.forEach(image -> deleteImage(image.getId()));
    }
    
    @Transactional
    public void setMainImage(Long productId, Long imageId) {
        // Désélectionner toutes les images principales du produit
        productImageRepository.clearMainImageForProduct(productId);
        
        // Définir la nouvelle image principale
        productImageRepository.findById(imageId).ifPresent(image -> {
            image.setIsMain(true);
            productImageRepository.save(image);
        });
    }
}

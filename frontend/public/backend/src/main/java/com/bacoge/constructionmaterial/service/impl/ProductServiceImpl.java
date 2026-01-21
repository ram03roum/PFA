package com.bacoge.constructionmaterial.service.impl;

import com.bacoge.constructionmaterial.dto.ProductRequest;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.ProductImage;
import com.bacoge.constructionmaterial.repository.CategoryRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    @Value("${app.upload.dir:${user.home}/bacoge-uploads}")
    private String uploadDir;

    public ProductServiceImpl(ProductRepository productRepository,
                            CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long productId) {
        if (productId == null) {
            return false;
        }
        return productRepository.existsById(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product createProduct(ProductRequest productRequest) {
        Product product = new Product();
        updateProductFromRequest(product, productRequest);
        
        // Gérer la catégorie
        if (productRequest.getCategoryId() != null) {
            categoryRepository.findById(productRequest.getCategoryId())
                .ifPresent(product::setCategory);
        }
        
        // Enregistrer d'abord le produit pour obtenir un ID
        Product savedProduct = productRepository.save(product);
        
        // Gérer les images
        if (productRequest.getImages() != null && !productRequest.getImages().isEmpty()) {
            saveProductImages(savedProduct, productRequest.getImages(), productRequest.getMainImageIndex());
        }
        
        return savedProduct;
    }

    @Override
    public Product updateProduct(Long id, ProductRequest productRequest) {
        return productRepository.findById(id).map(product -> {
            updateProductFromRequest(product, productRequest);
            
            // Mettre à jour la catégorie
            if (productRequest.getCategoryId() != null) {
                categoryRepository.findById(productRequest.getCategoryId())
                    .ifPresent(product::setCategory);
            } else {
                product.setCategory(null);
            }
            
            // Mettre à jour les images si fournies
            if (productRequest.getImages() != null && !productRequest.getImages().isEmpty()) {
                // Supprimer les anciennes images non conservées
                if (productRequest.getExistingImageIds() != null) {
                    product.getImages().removeIf(image -> 
                        !productRequest.getExistingImageIds().contains(image.getId()));
                } else {
                    product.getImages().clear();
                }
                
                // Ajouter les nouvelles images
                saveProductImages(product, productRequest.getImages(), productRequest.getMainImageIndex());
            } else if (productRequest.getMainImageIndex() != null) {
                // Mettre à jour uniquement l'image principale
                if (productRequest.getMainImageIndex() < product.getImages().size()) {
                    Long imageId = product.getImages().get(productRequest.getMainImageIndex()).getId();
                    setMainProductImage(id, imageId);
                }
            }
            
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + id));
        
        // Supprimer les fichiers d'images associés
        product.getImages().forEach(image -> {
            try {
                String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get(uploadDir, filename);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log l'erreur mais continuer la suppression
                e.printStackTrace();
            }
        });
        
        productRepository.delete(product);
    }

    @Override
    public void addImageToProduct(Long productId, String imageUrl, boolean isMain) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));
        
        ProductImage image = new ProductImage();
        image.setImageUrl(imageUrl);
        image.setIsMain(isMain);
        image.setProduct(product);
        
        // Si c'est l'image principale, désélectionner les autres
        if (isMain) {
            product.getImages().forEach(img -> img.setIsMain(false));
        }
        
        product.getImages().add(image);
        productRepository.save(product);
    }

    @Override
    public void removeImageFromProduct(Long productId, Long imageId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));
        
        product.getImages().removeIf(image -> {
            if (image.getId().equals(imageId)) {
                try {
                    // Supprimer le fichier physique
                    String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
                    Path filePath = Paths.get(uploadDir, filename);
                    Files.deleteIfExists(filePath);
                    return true;
                } catch (IOException e) {
                    // Log l'erreur mais continuer la suppression en base
                    e.printStackTrace();
                    return true;
                }
            }
            return false;
        });
        
        productRepository.save(product);
    }

    @Override
    public void setMainProductImage(Long productId, Long imageId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));
        
        // Désélectionner toutes les images principales
        product.getImages().forEach(img -> img.setIsMain(false));
        
        // Définir la nouvelle image principale
        product.getImages().stream()
            .filter(img -> img.getId().equals(imageId))
            .findFirst()
            .ifPresent(img -> {
                img.setIsMain(true);
                productRepository.save(product);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInStock(Long productId, int quantity) {
        return productRepository.findById(productId)
            .map(product -> product.getStockQuantity() >= quantity)
            .orElse(false);
    }

    @Override
    public void updateStock(Long productId, int quantityChange) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + productId));
        
        int newStock = product.getStockQuantity() + quantityChange;
        if (newStock < 0) {
            throw new IllegalStateException("Stock insuffisant pour le produit: " + productId);
        }
        
        product.setStockQuantity(newStock);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProductsInCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }

    // Méthodes utilitaires privées
    
    private void updateProductFromRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setMinStockLevel(request.getMinStockLevel() != null ? request.getMinStockLevel() : 10);
        product.setSku(request.getSku());
        product.setWeightKg(request.getWeightKg());
        product.setDimensions(request.getDimensions());
        product.setBrand(request.getBrand());
        product.setUnit(request.getUnit());
        // Interior design fields
        product.setStyle(request.getStyle());
        product.setRoom(request.getRoom());
        product.setColor(request.getColor());
        product.setMaterial(request.getMaterial());
        product.setCollectionName(request.getCollectionName());
        product.setTags(request.getTags());
        product.setLongDescription(request.getLongDescription());
        if (request.getStatus() != null) {
            product.setStatus(Product.ProductStatus.valueOf(request.getStatus().toUpperCase()));
        }
    }
    
    private void saveProductImages(Product product, List<MultipartFile> files, Integer mainImageIndex) {
        if (files == null || files.isEmpty()) {
            return;
        }
        
        // S'assurer que le répertoire de destination existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Impossible de créer le répertoire d'upload", e);
            }
        }
        
        // Parcourir tous les fichiers et les enregistrer
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file != null && !file.isEmpty()) {
                try {
                    // Générer un nom de fichier unique
                    String originalFilename = file.getOriginalFilename();
                    String fileExtension = "";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    String newFilename = UUID.randomUUID().toString() + fileExtension;
                    
                    // Enregistrer le fichier
                    Path filePath = uploadPath.resolve(newFilename);
                    Files.copy(file.getInputStream(), filePath);
                    
                    // Créer l'entité ProductImage
                    ProductImage image = new ProductImage();
                    image.setImageUrl("/uploads/" + newFilename);
                    image.setDisplayOrder(i);
                    image.setProduct(product);
                    
                    // Définir si c'est l'image principale
                    boolean isMain = (mainImageIndex != null && i == mainImageIndex) || 
                                   (mainImageIndex == null && i == 0);
                    image.setIsMain(isMain);
                    
                    // Si c'est l'image principale, désélectionner les autres
                    if (isMain) {
                        product.getImages().forEach(img -> img.setIsMain(false));
                    }
                    
                    product.getImages().add(image);
                    
                } catch (IOException e) {
                    // Log l'erreur mais continuer avec les autres fichiers
                    e.printStackTrace();
                }
            }
        }
    }
}

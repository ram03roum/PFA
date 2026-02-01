package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.client.ProductDisplayDto;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.repository.ProductImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientProductService {
    
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    
    public ClientProductService(ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Page<ProductDisplayDto> getActiveProducts(
            String name,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String style,
            String room,
            String color,
            String material,
            String collectionName,
            String tags,
            String sortBy,
            String sortDir,
            Pageable pageable,
            String currency) {
        // Utiliser Specifications JPA pour filtrage côté DB
        Specification<Product> spec = Specification.where((root, query, cb) -> cb.conjunction());

        // Statut ACTIVE
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), Product.ProductStatus.ACTIVE));

        // Recherche texte (name/description)
        if (name != null && !name.trim().isEmpty()) {
            String q = "%" + name.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), q),
                    cb.like(cb.lower(root.get("description")), q)
            ));
        }

        // Catégorie
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        // Prix min/max
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        // Filtres design d'intérieur
        if (style != null && !style.isBlank()) {
            String v = "%" + style.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("style")), v));
        }
        if (room != null && !room.isBlank()) {
            String v = "%" + room.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("room")), v));
        }
        if (color != null && !color.isBlank()) {
            String v = "%" + color.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("color")), v));
        }
        if (material != null && !material.isBlank()) {
            String v = "%" + material.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("material")), v));
        }
        if (collectionName != null && !collectionName.isBlank()) {
            String v = "%" + collectionName.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("collectionName")), v));
        }
        if (tags != null && !tags.isBlank()) {
            String v = "%" + tags.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("tags")), v));
        }

        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(this::toDisplayDto);
    }
    
    public ProductDisplayDto getProductById(Long id) {
        // Get product without any joins to avoid MultipleBagFetchException
        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        
        Product product = productOpt.get();
        
        // Create DTO with only basic fields - no lazy collections
        ProductDisplayDto dto = new ProductDisplayDto();
        dto.setId(product.getId());
        dto.setName(product.getName() != null ? product.getName() : "Produit sans nom");
        dto.setDescription(product.getDescription() != null ? product.getDescription() : "Aucune description");
        dto.setPrice(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
        dto.setBrand(product.getBrand());
        dto.setSku(product.getSku());
        dto.setStockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
        dto.setDimensions(product.getDimensions());
        dto.setWeightKg(BigDecimal.valueOf(1.0));
        dto.setCurrency("EUR");
        
        // Safe defaults without accessing lazy collections
        dto.setCategory("Matériaux de construction");
        // Enrich images from repository to avoid lazy-loading issues
        List<String> urls = productImageRepository.findAllImageUrlsByProductIdOrder(product.getId());
        String mainUrl = productImageRepository.findMainImageUrlByProductId(product.getId());
        if ((mainUrl == null || mainUrl.isBlank()) && urls != null && !urls.isEmpty()) {
            mainUrl = urls.get(0);
        }
        if (urls != null && !urls.isEmpty()) {
            dto.setImageUrls(urls);
            dto.setImageUrl(mainUrl != null ? mainUrl : urls.get(0));
        } else {
            dto.setImageUrls(List.of("/images/placeholder-product.jpg"));
            dto.setImageUrl("/branding/product-placeholder.svg");
        }
        dto.setAverageRating(4.0);
        dto.setReviewCount(0);
        dto.setDiscountedPrice(dto.getPrice());
        dto.setInStock(dto.getStockQuantity() > 0);
        dto.setLowStock(dto.getStockQuantity() < 10);
        dto.setStatus("ACTIVE");
        dto.setCreatedAt(product.getCreatedAt());
        
        return dto;
    }
    
    public List<ProductDisplayDto> getRecommendedProducts(int limit) {
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .limit(limit)
                .collect(Collectors.toList());
        return products.stream()
                .map(this::toDisplayDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductDisplayDto> getPopularProducts(int limit) {
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .limit(limit)
                .collect(Collectors.toList());
        return products.stream()
                .map(this::toDisplayDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductDisplayDto> getNewArrivals() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .filter(product -> product.getCreatedAt() != null && product.getCreatedAt().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());
        return products.stream()
                .map(this::toDisplayDto)
                .collect(Collectors.toList());
    }
    
    public Page<ProductDisplayDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        Page<Product> filteredProducts = products.map(product -> {
            if (product.getCategoryId() != null && product.getCategoryId().equals(categoryId) && 
                product.getStatus() == Product.ProductStatus.ACTIVE) {
                return product;
            }
            return null;
        });
        return filteredProducts.map(this::toDisplayDto);
    }
    
    public Page<ProductDisplayDto> searchProducts(String query, Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        Page<Product> filteredProducts = products.map(product -> {
            if (product.getStatus() == Product.ProductStatus.ACTIVE && 
                (query == null || product.getName().toLowerCase().contains(query.toLowerCase()) ||
                 product.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                return product;
            }
            return null;
        });
        return filteredProducts.map(this::toDisplayDto);
    }
    
    public List<ProductDisplayDto> getFeaturedProducts() {
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .limit(8)
                .collect(Collectors.toList());
        return products.stream()
                .map(this::toDisplayDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductDisplayDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products = productRepository.findAll().stream()
                .filter(product -> product.getStatus() == Product.ProductStatus.ACTIVE)
                .filter(product -> (minPrice == null || product.getPrice().compareTo(minPrice) >= 0))
                .filter(product -> (maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0))
                .collect(Collectors.toList());
        return products.stream()
                .map(this::toDisplayDto)
                .collect(Collectors.toList());
    }

    public List<ProductDisplayDto> getRelatedProducts(Long productId) {
        Product product = productRepository.findByIdWithCategory(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (product.getCategory() == null) {
            return List.of();
        }
        List<Product> related = productRepository.findActiveRelatedProducts(
                product.getCategory().getId(), productId, PageRequest.of(0, 8));
        return related.stream()
                .map(this::toDisplayDto)
                .collect(Collectors.toList());
    }

    // Build a ProductDisplayDto safely and enrich its imageUrl from repository (avoid lazy image collection)
    private ProductDisplayDto toDisplayDto(Product product) {
        ProductDisplayDto dto = ProductDisplayDto.fromProduct(product);
        try {
            String mainUrl = productImageRepository.findMainImageUrlByProductId(product.getId());
            if (mainUrl == null || mainUrl.isBlank()) {
                var urls = productImageRepository.findAllImageUrlsByProductIdOrder(product.getId());
                if (urls != null && !urls.isEmpty()) mainUrl = urls.get(0);
            }
            if (mainUrl != null && !mainUrl.isBlank()) {
                dto.setImageUrl(mainUrl);
            }
        } catch (Exception ignored) {}
        return dto;
    }
}
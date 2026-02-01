package com.bacoge.constructionmaterial.service.admin;

import com.bacoge.constructionmaterial.dto.admin.CreatePromotionRequest;
import com.bacoge.constructionmaterial.dto.admin.PromotionDto;
import com.bacoge.constructionmaterial.model.Promotion;
import com.bacoge.constructionmaterial.repository.PromotionRepository;
import com.bacoge.constructionmaterial.repository.CategoryRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.Category;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Set;
import java.util.HashSet;

@Service
public class AdminPromotionService {
    
    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    public AdminPromotionService(PromotionRepository promotionRepository,
                                 CategoryRepository categoryRepository,
                                 ProductRepository productRepository) {
        this.promotionRepository = promotionRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }
    
    public List<PromotionDto> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Page<PromotionDto> getAllPromotions(String name, String status, Pageable pageable) {
        // In a complete implementation, this would use repository methods with filtering
        List<Promotion> allPromotions = promotionRepository.findAll();
        
        // Apply filters if provided
        Stream<Promotion> filteredStream = allPromotions.stream();
        if (name != null && !name.isEmpty()) {
            filteredStream = filteredStream.filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()));
        }
        if (status != null && !status.isEmpty()) {
            filteredStream = filteredStream.filter(p -> p.getStatus().name().equalsIgnoreCase(status));
        }
        
        List<Promotion> filteredPromotions = filteredStream.collect(Collectors.toList());
        List<PromotionDto> promotionDtos = filteredPromotions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Create a Page from the filtered results
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), promotionDtos.size());
        List<PromotionDto> pageContent = promotionDtos.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, promotionDtos.size());
    }
    
    public List<com.bacoge.constructionmaterial.dto.client.PromotionDto> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findActivePromotions(now).stream()
                .map(promotion -> {
                    com.bacoge.constructionmaterial.dto.client.PromotionDto dto = new com.bacoge.constructionmaterial.dto.client.PromotionDto();
                    dto.setId(promotion.getId());
                    dto.setName(promotion.getName());
                    dto.setDescription(promotion.getDescription());
                    dto.setDiscountPercentage(promotion.getDiscountPercentage());
                    dto.setDiscountAmount(promotion.getDiscountAmount());
                    dto.setDiscountType(promotion.getDiscountType() != null ? promotion.getDiscountType().name() : null);
                    dto.setPromoCode(promotion.getPromoCode());
                    dto.setStartDate(promotion.getStartDate());
                    dto.setEndDate(promotion.getEndDate());
                    dto.setMinOrderAmount(promotion.getMinOrderAmount());
                    dto.setMaxUses(promotion.getMaxUses());
                    dto.setCurrentUses(promotion.getCurrentUses());
                    dto.setStatus(promotion.getStatus() != null ? promotion.getStatus().name() : null);
                    dto.setCreatedAt(promotion.getCreatedAt());
                    dto.setUpdatedAt(promotion.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public List<PromotionDto> getValidPromotions() {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findValidPromotions(now).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PromotionDto getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        return convertToDto(promotion);
    }
    
    public PromotionDto getPromotionByCode(String promoCode) {
        Promotion promotion = promotionRepository.findAll().stream()
                .filter(p -> p.getPromoCode().equals(promoCode))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Promotion not found with code: " + promoCode));
        return convertToDto(promotion);
    }
    
    public PromotionDto createPromotion(CreatePromotionRequest request) {
        // Validation minimaliste
        if (request.getPromoCode() != null && promotionRepository.existsByPromoCode(request.getPromoCode())) {
            throw new RuntimeException("Un code promo avec cette valeur existe déjà");
        }

        Promotion promotion = new Promotion();
        applyRequestToEntity(promotion, request);

        Promotion savedPromotion = promotionRepository.save(promotion);
        return convertToDto(savedPromotion);
    }
    
    public PromotionDto updatePromotion(Long id, CreatePromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        applyRequestToEntity(promotion, request);
        Promotion savedPromotion = promotionRepository.save(promotion);
        return convertToDto(savedPromotion);
    }
    
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion not found with id: " + id);
        }
        promotionRepository.deleteById(id);
    }
    
    public PromotionDto updatePromotionStatus(Long id, Promotion.PromotionStatus status) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        
        promotion.setStatus(status);
        Promotion savedPromotion = promotionRepository.save(promotion);
        return convertToDto(savedPromotion);
    }
    
    public List<PromotionDto> searchPromotions(String name, Promotion.PromotionStatus status) {
        return promotionRepository.findAll().stream()
                .filter(promotion -> name == null || promotion.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(promotion -> status == null || promotion.getStatus() == status)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public long getTotalPromotions() {
        return promotionRepository.count();
    }
    
    public long getActivePromotionsCount() {
        return getActivePromotions().size();
    }
    
    public long getExpiredPromotionsCount() {
        return promotionRepository.findAll().stream()
                .filter(promotion -> promotion.getEndDate().isBefore(LocalDateTime.now()))
                .count();
    }
    
    public long getPromotionsCountByStatus(Promotion.PromotionStatus status) {
        return promotionRepository.findAll().stream()
                .filter(promotion -> promotion.getStatus() == status)
                .count();
    }
    
    private PromotionDto convertToDto(Promotion promotion) {
        PromotionDto dto = new PromotionDto();
        dto.setId(promotion.getId());
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setPromoCode(promotion.getPromoCode());
        dto.setDiscountPercentage(promotion.getDiscountPercentage());
        dto.setDiscountAmount(promotion.getDiscountAmount());
        dto.setDiscountType(promotion.getDiscountType() != null ? promotion.getDiscountType().name() : null);
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setMinOrderAmount(promotion.getMinOrderAmount());
        dto.setMaxUses(promotion.getMaxUses());
        dto.setCurrentUses(promotion.getCurrentUses());
        dto.setStatus(promotion.getStatus() != null ? promotion.getStatus().name() : null);
        dto.setCreatedAt(promotion.getCreatedAt());
        dto.setUpdatedAt(promotion.getUpdatedAt());
        return dto;
    }

    private void applyRequestToEntity(Promotion promotion, CreatePromotionRequest request) {
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setPromoCode(request.getPromoCode());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setMinOrderAmount(request.getMinOrderAmount());
        promotion.setMaxUses(request.getMaxUses());

        // Discount
        Promotion.DiscountType type = null;
        if (request.getDiscountType() != null) {
            try {
                type = Promotion.DiscountType.valueOf(request.getDiscountType().toUpperCase());
            } catch (Exception ignored) {}
        }
        if (type == null) {
            type = Promotion.DiscountType.PERCENTAGE;
        }
        promotion.setDiscountType(type);
        if (type == Promotion.DiscountType.PERCENTAGE) {
            promotion.setDiscountPercentage(request.getDiscountPercentage());
            promotion.setDiscountAmount(null);
        } else {
            promotion.setDiscountAmount(request.getDiscountAmount());
            promotion.setDiscountPercentage(null);
        }

        // Status
        Promotion.PromotionStatus status = Promotion.PromotionStatus.ACTIVE;
        if (request.getStatus() != null) {
            try {
                status = Promotion.PromotionStatus.valueOf(request.getStatus().toUpperCase());
            } catch (Exception ignored) {}
        }
        promotion.setStatus(status);

        // Applicable categories
        if (request.getApplicableCategoryIds() != null && !request.getApplicableCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getApplicableCategoryIds()));
            promotion.setApplicableCategories(categories);
        } else {
            promotion.setApplicableCategories(null);
        }

        // Applicable products
        if (request.getApplicableProductIds() != null && !request.getApplicableProductIds().isEmpty()) {
            Set<Product> products = new HashSet<>(productRepository.findAllById(request.getApplicableProductIds()));
            promotion.setApplicableProducts(products);
        } else {
            promotion.setApplicableProducts(null);
        }
    }
}

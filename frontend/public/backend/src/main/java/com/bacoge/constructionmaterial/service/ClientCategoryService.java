package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.client.CategoryDisplayDto;
import com.bacoge.constructionmaterial.model.Category;
import com.bacoge.constructionmaterial.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientCategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public ClientCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAll(); // Simplified - return all categories as active
    }
    
    // Méthode pour compter les produits dans une catégorie
    private int countProductsInCategory(Category category) {
        if (category.getProducts() != null) {
            return category.getProducts().size();
        }
        return 0;
    }
    
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    public List<Category> getFeaturedCategories() {
        // Return all categories for now - in a complete implementation, 
        // this would filter by featured flag
        return categoryRepository.findAll();
    }
    
    public List<Category> getAllCategoriesWithPromoCount() {
        // Return all categories for now - in a complete implementation,
        // this would include promotion counts
        return categoryRepository.findAll();
    }

    public List<CategoryDisplayDto> getAllActiveCategoryDisplays() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    Long count = categoryRepository.countActiveProductsByCategoryId(category.getId());
                    int productCount = count != null ? count.intValue() : 0;
                    CategoryDisplayDto dto = new CategoryDisplayDto();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    dto.setDescription(category.getDescription());
                    dto.setImageUrl(category.getImageUrl());
                    dto.setStatus("ACTIVE");
                    dto.setCreatedAt(category.getCreatedAt());
                    dto.setProductCount(productCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
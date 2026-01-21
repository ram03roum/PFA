package com.bacoge.constructionmaterial.service.admin;

import com.bacoge.constructionmaterial.dto.admin.CategoryDto;
import com.bacoge.constructionmaterial.dto.admin.CreateCategoryRequest;
import com.bacoge.constructionmaterial.model.Category;
import com.bacoge.constructionmaterial.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public AdminCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    CategoryDto dto = CategoryDto.fromEntity(category);
                    Long count = categoryRepository.countActiveProductsByCategoryId(category.getId());
                    dto.setProductsCount(count != null ? count : 0L);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getStatus() == Category.CategoryStatus.ACTIVE)
                .map(category -> {
                    CategoryDto dto = CategoryDto.fromEntity(category);
                    Long count = categoryRepository.countActiveProductsByCategoryId(category.getId());
                    dto.setProductsCount(count != null ? count : 0L);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        CategoryDto dto = CategoryDto.fromEntity(category);
        // Build complete image URL if image exists
        if (dto.getImageUrl() != null && !dto.getImageUrl().startsWith("http")) {
            String baseUrl = "http://localhost:8080"; // TODO: Get from configuration
            dto.setImageUrl(baseUrl + dto.getImageUrl());
        }
        return dto;
    }
    
    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        // Gérer le statut - conversion entre les enums
        if (request.getStatus() != null) {
            category.setStatus(Category.CategoryStatus.valueOf(request.getStatus().name()));
        } else {
            category.setStatus(Category.CategoryStatus.ACTIVE);
        }
        
        // Gérer l'image URL
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            category.setImageUrl(request.getImageUrl());
        }
        
        Category savedCategory = categoryRepository.save(category);
        CategoryDto dto = CategoryDto.fromEntity(savedCategory);
        return dto;
    }
    
    @Transactional
    public CategoryDto updateCategory(Long id, CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        // Handle image URL update
        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            category.setImageUrl(request.getImageUrl());
        }
        
        Category savedCategory = categoryRepository.save(category);
        CategoryDto dto = CategoryDto.fromEntity(savedCategory);
        return dto;
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
    
    @Transactional
    public CategoryDto updateCategoryStatus(Long id, Category.CategoryStatus status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setStatus(status);
        Category savedCategory = categoryRepository.save(category);
        CategoryDto dto = CategoryDto.fromEntity(savedCategory);
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategoriesByName(String name) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName().toLowerCase().contains(name.toLowerCase()))
                .map(category -> {
                    CategoryDto dto = CategoryDto.fromEntity(category);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    public long getTotalCategories() {
        return categoryRepository.count();
    }
    
    public long getActiveCategoriesCount() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getStatus() == Category.CategoryStatus.ACTIVE)
                .count();
    }
}

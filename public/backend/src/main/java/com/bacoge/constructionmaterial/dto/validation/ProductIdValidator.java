package com.bacoge.constructionmaterial.dto.validation;

import com.bacoge.constructionmaterial.service.ProductService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductIdValidator implements ConstraintValidator<ValidProductId, Long> {

    @Autowired
    private ProductService productService;

    @Override
    public void initialize(ValidProductId constraintAnnotation) {
        // Initialization if needed
    }

    @Override
    public boolean isValid(Long productId, ConstraintValidatorContext context) {
        if (productId == null) {
            return false;
        }
        return productService.existsById(productId);
    }
}

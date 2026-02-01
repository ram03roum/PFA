package com.bacoge.constructionmaterial.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ProductIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProductId {
    String message() default "Invalid product ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

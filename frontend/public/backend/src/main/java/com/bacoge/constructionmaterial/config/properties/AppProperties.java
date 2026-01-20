package com.bacoge.constructionmaterial.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Central configuration properties for the application.
 * This class serves as a container for all custom application properties.
 * 
 * @author Bacoge Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@Validated
// @Import({SecurityConfig.class}) - Removed as SecurityConfig class not found
public class AppProperties {
    
    /**
     * Application name.
     * Default: Loaded from spring.application.name
     */
    private String name;
    
    /**
     * Application version.
     * Default: Loaded from build information or application.properties
     */
    private String version;
    
    /**
     * Application description.
     */
    private String description;
    
    /**
     * Default currency code (ISO 4217).
     * Default: BIF (Burundian Franc)
     */
    private String currency = "BIF";
    
    /**
     * Default tax rate as a percentage.
     * Default: 18.0
     */
    private double taxRate = 18.0;
    
    /**
     * Maximum allowed discount percentage.
     * Default: 50.0
     */
    private double maxDiscountPercentage = 50.0;
    
    @PostConstruct
    public void init() {
        System.out.println("Application properties loaded: " + this);
    }
}

package com.bacoge.constructionmaterial.config;

import com.bacoge.constructionmaterial.config.properties.AppProperties;
import com.bacoge.constructionmaterial.config.properties.JwtProperties;
import com.bacoge.constructionmaterial.config.properties.MailProperties;
import com.bacoge.constructionmaterial.config.properties.SecurityProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable and scan for configuration properties.
 * This ensures all @ConfigurationProperties-annotated classes are properly
 * registered as Spring beans and can be injected where needed.
 */
@Configuration
@ConfigurationPropertiesScan("com.bacoge.constructionmaterial.config.properties")
@EnableConfigurationProperties({
    AppProperties.class,
    JwtProperties.class,
    SecurityProperties.class,
    MailProperties.class
})
public class PropertiesConfig {
    // This class serves as a central place to enable and scan for configuration properties
    // All @ConfigurationProperties-annotated classes in the com.bacoge.constructionmaterial.config.properties package
    // will be automatically registered as Spring beans
}

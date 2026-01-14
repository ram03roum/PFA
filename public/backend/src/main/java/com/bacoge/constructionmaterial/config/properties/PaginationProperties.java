package com.bacoge.constructionmaterial.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for pagination settings.
 * Maps properties with prefix 'app.pagination' from application.properties/yml.
 */
@Component
@ConfigurationProperties(prefix = "app.pagination")
public class PaginationProperties {
    /**
     * Default number of items per page when no size is specified.
     * Default: 20 items per page
     */
    private int defaultSize = 20;
    
    /**
     * Maximum number of items allowed per page.
     * Default: 100 items
     */
    private int maxSize = 100;

    /**
     * @return the default number of items per page
     */
    public int getDefaultSize() {
        return defaultSize;
    }

    /**
     * @param defaultSize the default number of items per page
     */
    public void setDefaultSize(int defaultSize) {
        this.defaultSize = defaultSize;
    }

    /**
     * @return the maximum number of items allowed per page
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @param maxSize the maximum number of items allowed per page
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}

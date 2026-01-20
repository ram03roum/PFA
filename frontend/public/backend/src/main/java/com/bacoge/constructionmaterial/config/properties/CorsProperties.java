package com.bacoge.constructionmaterial.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for CORS (Cross-Origin Resource Sharing) settings.
 * Maps properties with prefix 'app.cors' from application.properties/yml.
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    /**
     * List of allowed origins (domain names) that can access the API.
     * Use "*" to allow all origins (not recommended for production).
     * Default: ["http://localhost:3000", "http://localhost:8080"]
     */
    private List<String> allowedOrigins = Arrays.asList("http://localhost:3000", "http://localhost:8080");
    
    /**
     * List of allowed HTTP methods.
     * Default: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"]
     */
    private List<String> allowedMethods = Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    
    /**
     * List of allowed HTTP headers.
     * Default: ["*"], which allows all headers
     */
    private List<String> allowedHeaders = Arrays.asList("*");
    
    /**
     * List of response headers that are exposed to the client.
     * Default: ["Authorization", "Content-Type", "X-Requested-With", "Accept", "X-XSRF-TOKEN"]
     */
    private List<String> exposedHeaders = Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "X-XSRF-TOKEN");
    
    /**
     * Whether user credentials (cookies, HTTP authentication) are supported.
     * Default: true
     */
    private boolean allowCredentials = true;
    
    /**
     * How long (in seconds) the results of a preflight request can be cached.
     * Default: 3600 seconds (1 hour)
     */
    private long maxAge = 3600L;

    // Getters and Setters with documentation
    
    /**
     * @return the list of allowed origins
     */
    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * @param allowedOrigins the list of allowed origins to set
     */
    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * @return the list of allowed HTTP methods
     */
    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    /**
     * @param allowedMethods the list of allowed HTTP methods to set
     */
    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    /**
     * @return the list of allowed HTTP headers
     */
    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    /**
     * @param allowedHeaders the list of allowed HTTP headers to set
     */
    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    /**
     * @return the list of exposed response headers
     */
    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    /**
     * @param exposedHeaders the list of exposed response headers to set
     */
    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    /**
     * @return whether credentials are allowed
     */
    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    /**
     * @param allowCredentials whether to allow credentials
     */
    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    /**
     * @return the maximum age for preflight requests in seconds
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * @param maxAge the maximum age for preflight requests in seconds to set
     */
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}

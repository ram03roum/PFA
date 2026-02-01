package com.bacoge.constructionmaterial.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for image upload and processing settings.
 * Maps properties with prefix 'app.images' from application.properties/yml.
 */
@Component
@ConfigurationProperties(prefix = "app.images")
public class ImagesProperties {
    /**
     * Directory where uploaded images will be stored.
     * Default: "./uploads/images/"
     */
    private String uploadDir = "./uploads/images/";
    
    /**
     * Maximum allowed size for uploaded images in bytes.
     * Default: 5MB (5 * 1024 * 1024 bytes)
     */
    private long maxSize = 5 * 1024 * 1024; // 5MB
    
    /**
     * List of allowed file extensions for uploaded images.
     */
    private String[] allowedExtensions = {"jpg", "jpeg", "png", "gif", "webp"};
    
    /**
     * Thumbnail generation settings.
     */
    private final Thumbnail thumbnail = new Thumbnail();

    /**
     * Thumbnail configuration properties.
     */
    public static class Thumbnail {
        /**
         * Width of generated thumbnails in pixels.
         * Default: 200px
         */
        private int width = 200;
        
        /**
         * Height of generated thumbnails in pixels.
         * Default: 200px
         */
        private int height = 200;

        public int getWidth() { 
            return width; 
        }
        
        public void setWidth(int width) { 
            this.width = width; 
        }
        
        public int getHeight() { 
            return height; 
        }
        
        public void setHeight(int height) { 
            this.height = height; 
        }
    }

    // Getters and Setters with documentation
    
    public String getUploadDir() { 
        return uploadDir; 
    }
    
    public void setUploadDir(String uploadDir) { 
        this.uploadDir = uploadDir; 
    }
    
    public long getMaxSize() { 
        return maxSize; 
    }
    
    public void setMaxSize(long maxSize) { 
        this.maxSize = maxSize; 
    }
    
    public String[] getAllowedExtensions() { 
        return allowedExtensions; 
    }
    
    public void setAllowedExtensions(String[] allowedExtensions) { 
        this.allowedExtensions = allowedExtensions; 
    }
    
    public Thumbnail getThumbnail() { 
        return thumbnail; 
    }
    
    // No setter for thumbnail as it's final and initialized in-place
}

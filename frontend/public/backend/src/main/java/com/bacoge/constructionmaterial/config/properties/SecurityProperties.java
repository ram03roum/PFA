package com.bacoge.constructionmaterial.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Configuration properties for security settings.
 * Maps properties with prefix 'app.security' from application.properties/yml.
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    
    /**
     * Password policy configuration.
     */
    private final Password password = new Password();
    
    /**
     * Login attempt and lockout configuration.
     */
    private final Login login = new Login();
    
    /**
     * List of allowed origins for CORS.
     */
    private List<String> allowedOrigins;
    
    /**
     * List of allowed HTTP methods for CORS.
     */
    private List<String> allowedMethods;
    
    /**
     * List of allowed headers for CORS.
     */
    private List<String> allowedHeaders;
    
    /**
     * List of exposed headers for CORS.
     */
    private List<String> exposedHeaders;
    
    /**
     * Whether to allow credentials in CORS requests.
     */
    private boolean allowCredentials = true;
    
    /**
     * Maximum age in seconds for CORS preflight requests.
     */
    private long maxAge = 3600;

    /**
     * Password policy configuration.
     */
    @Data
    public static class Password {
        /**
         * Minimum password length.
         */
        @Min(value = 6, message = "Minimum password length must be at least 6")
        private int minLength = 8;
        
        /**
         * Whether password must contain at least one uppercase letter.
         */
        private boolean requireUppercase = true;
        
        /**
         * Whether password must contain at least one lowercase letter.
         */
        private boolean requireLowercase = true;
        
        /**
         * Whether password must contain at least one digit.
         * Default: true
         */
        private boolean requireDigit = true;
        
        /**
         * Whether password must contain at least one special character.
         * Default: true
         */
        private boolean requireSpecialChar = true;

        public int getMinLength() { 
            return minLength; 
        }
        
        public void setMinLength(int minLength) { 
            this.minLength = minLength; 
        }
        
        public boolean isRequireUppercase() { 
            return requireUppercase; 
        }
        
        public void setRequireUppercase(boolean requireUppercase) { 
            this.requireUppercase = requireUppercase; 
        }
        
        public boolean isRequireLowercase() { 
            return requireLowercase; 
        }
        
        public void setRequireLowercase(boolean requireLowercase) { 
            this.requireLowercase = requireLowercase; 
        }
        
        public boolean isRequireDigit() { 
            return requireDigit; 
        }
        
        public void setRequireDigit(boolean requireDigit) { 
            this.requireDigit = requireDigit; 
        }
        
        public boolean isRequireSpecialChar() { 
            return requireSpecialChar; 
        }
        
        public void setRequireSpecialChar(boolean requireSpecialChar) { 
            this.requireSpecialChar = requireSpecialChar; 
        }
    }

    /**
     * Login attempt and account lockout settings.
     */
    public static class Login {
        /**
         * Maximum number of failed login attempts before account is locked.
         * Default: 5 attempts
         */
        private int maxAttempts = 5;
        
        /**
         * Duration of account lockout after exceeding max attempts.
         * Default: 30 minutes
         */
        private Duration lockoutDuration = Duration.ofMinutes(30);

        public int getMaxAttempts() { 
            return maxAttempts; 
        }
        
        public void setMaxAttempts(int maxAttempts) { 
            this.maxAttempts = maxAttempts; 
        }
        
        public Duration getLockoutDuration() { 
            return lockoutDuration; 
        }
        
        public void setLockoutDuration(Duration lockoutDuration) { 
            this.lockoutDuration = lockoutDuration; 
        }
    }

    // Getters (no setters as the fields are final and initialized in-place)
    
    public Password getPassword() { 
        return password; 
    }
    
    public Login getLogin() { 
        return login; 
    }
}

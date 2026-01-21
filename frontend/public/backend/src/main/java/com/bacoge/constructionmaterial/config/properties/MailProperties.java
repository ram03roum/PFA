package com.bacoge.constructionmaterial.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for email settings.
 * Maps properties with prefix 'app.mail' from application.properties/yml.
 */
@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {
    /**
     * SMTP server host.
     * Example: smtp.gmail.com
     */
    private String host;
    
    /**
     * SMTP server port.
     * Default: 587 (TLS)
     */
    private int port = 587;
    
    /**
     * Username for SMTP authentication.
     * Typically the email address used to send emails.
     */
    private String username;
    
    /**
     * Password for SMTP authentication.
     * For Gmail, this would be an App Password if 2FA is enabled.
     */
    private String password;
    
    /**
     * Protocol to use for mail sending.
     * Default: smtp
     */
    private String protocol = "smtp";
    
    /**
     * Whether to use SMTP authentication.
     * Default: true
     */
    private boolean smtpAuth = true;
    
    /**
     * Whether to enable STARTTLS for secure connection.
     * Default: true
     */
    private boolean starttlsEnable = true;
    
    /**
     * Default 'from' email address.
     * Example: no-reply@bacoge.com
     */
    private String from;
    
    /**
     * Default 'from' name displayed to recipients.
     * Example: "Bacoge App"
     */
    private String fromName;
    
    /**
     * Enable JavaMail debug output.
     * Default: false
     */
    private boolean debug = false;

    // Getters and Setters with documentation
    
    public String getHost() { 
        return host; 
    }
    
    public void setHost(String host) { 
        this.host = host; 
    }
    
    public int getPort() { 
        return port; 
    }
    
    public void setPort(int port) { 
        this.port = port; 
    }
    
    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public String getProtocol() { 
        return protocol; 
    }
    
    public void setProtocol(String protocol) { 
        this.protocol = protocol; 
    }
    
    public boolean isSmtpAuth() { 
        return smtpAuth; 
    }
    
    public void setSmtpAuth(boolean smtpAuth) { 
        this.smtpAuth = smtpAuth; 
    }
    
    public boolean isStarttlsEnable() { 
        return starttlsEnable; 
    }
    
    public void setStarttlsEnable(boolean starttlsEnable) { 
        this.starttlsEnable = starttlsEnable; 
    }
    
    public String getFrom() { 
        return from; 
    }
    
    public void setFrom(String from) { 
        this.from = from; 
    }
    
    public String getFromName() { 
        return fromName; 
    }
    
    public void setFromName(String fromName) { 
        this.fromName = fromName; 
    }
    
    public boolean isDebug() { 
        return debug; 
    }
    
    public void setDebug(boolean debug) { 
        this.debug = debug; 
    }
}

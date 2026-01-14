package com.bacoge.constructionmaterial.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for handling JWT token operations with support for different token types
 */

@Service
public class JwtService {
    
    // Token type constants
    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String ADMIN_ACCESS = "ADMIN_ACCESS";
    public static final String USER_ACCESS = "USER_ACCESS";
    
    @Value("${app.jwt.secret:MyVerySecureJwtSecretKeyForHS512Algorithm123456789012345678901234}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration-ms:86400000}")
    private Long jwtExpiration;
    
    @Value("${app.jwt.admin-expiration-ms:28800000}")
    private Long adminJwtExpiration;
    
    private Key getSigningKey() {
        try {
            if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
                // Fallback to a secure default if not configured
                jwtSecret = "MyVerySecureJwtSecretKeyForHS512Algorithm123456789012345678901234";
            }
            return Keys.hmacShaKeyFor(jwtSecret.getBytes());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JWT signing key. Please check the JWT secret configuration.", e);
        }
    }
    
    /**
     * Generate a token with default type (USER_ACCESS)
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails, USER_ACCESS);
    }
    
    /**
     * Generate a token with specific type (ADMIN_ACCESS or USER_ACCESS)
     */
    public String generateToken(Authentication authentication, String tokenType) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails, tokenType);
    }
    
    /**
     * Generate a token with default type (USER_ACCESS)
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, USER_ACCESS);
    }
    
    /**
     * Generate a token with specific type (ADMIN_ACCESS or USER_ACCESS)
     */
    public String generateToken(UserDetails userDetails, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        
        // Add user information to claims
        if (userDetails instanceof com.bacoge.constructionmaterial.model.User) {
            com.bacoge.constructionmaterial.model.User user = (com.bacoge.constructionmaterial.model.User) userDetails;
            claims.put("role", user.getRole().name());
            claims.put("firstName", user.getFirstName());
            claims.put("lastName", user.getLastName());
            claims.put("email", user.getEmail());
        }
        
        // Add token type to claims
        claims.put(TOKEN_TYPE_CLAIM, tokenType);
        
        return createToken(claims, userDetails.getUsername(), tokenType);
    }
    
    public String generateToken(UserDetails userDetails, Map<String, Object> claims, String tokenType) {
        if (claims == null) {
            claims = new HashMap<>();
        }
        claims.put(TOKEN_TYPE_CLAIM, tokenType);
        return createToken(claims, userDetails.getUsername(), tokenType);
    }
    
    private String createToken(Map<String, Object> claims, String subject, String tokenType) {
        long expiration = ADMIN_ACCESS.equals(tokenType) ? adminJwtExpiration : jwtExpiration;
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public Boolean validateToken(String token) {
        return validateToken(token, null);
    }
    
    public Boolean validateToken(String token, String expectedTokenType) {
        try {
            Claims claims = validateTokenAndGetClaims(token, expectedTokenType);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Vérifie si un token JWT est valide (non expiré et correctement signé)
     * 
     * @param token Le token JWT à valider
     * @return true si le token est valide, false sinon
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Rafraîchit un token JWT en créant un nouveau token avec les mêmes claims
     * mais une nouvelle date d'expiration
     * 
     * @param token Le token JWT à rafraîchir
     * @return Un nouveau token JWT avec une nouvelle date d'expiration
     */
    public String refreshToken(String token) {
        try {
            // Vérifier d'abord si le token est valide
            if (!isTokenValid(token)) {
                return null;
            }
            
            // Extraire les claims existants
            Claims claims = extractAllClaims(token);
            
            // Créer un nouveau token avec les mêmes claims mais une nouvelle date d'expiration
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            return null;
        }
    }
    
    public Claims validateTokenAndGetClaims(String token) {
        return validateTokenAndGetClaims(token, null);
    }
    
    public Claims validateTokenAndGetClaims(String token, String expectedTokenType) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
                
        // If a specific token type is expected, validate it
        if (expectedTokenType != null) {
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!expectedTokenType.equals(tokenType)) {
                throw new SecurityException("Invalid token type");
            }
        }
        
        return claims;
    }
    
    /**
     * Invalide un token JWT en l'ajoutant à une liste noire
     * Note: Dans une implémentation réelle, vous voudriez peut-être utiliser un cache distribué comme Redis
     * pour stocker les tokens invalides
     * 
     * @param token Le token JWT à invalider
     */
    public void invalidateToken(String token) {
        // Dans une implémentation de base, nous ne faisons rien car les tokens JWT sont auto-contenus
        // et ne peuvent pas être invalidés sans vérification côté serveur
        // Pour une implémentation plus robuste, vous pourriez :
        // 1. Stocker le token dans une base de données avec une date d'expiration
        // 2. Vérifier dans cette base de données lors de la validation du token
        // 3. Nettoyer périodiquement les tokens expirés
        
        // Pour l'instant, nous nous contentons de logger l'invalidation
        // Une implémentation complète nécessiterait une solution de stockage partagé
        // pour les environnements distribués
        
        // Exemple d'implémentation avec une blacklist en mémoire (non persistante)
        // tokenBlacklist.add(token);
        
        // Pour l'instant, nous ne fais que logger l'action
        // Dans une application de production, vous voudriez implémenter une solution plus robuste
    }
}
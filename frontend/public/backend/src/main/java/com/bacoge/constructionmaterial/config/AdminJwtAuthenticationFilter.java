package com.bacoge.constructionmaterial.config;

import com.bacoge.constructionmaterial.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    // Chemins à ignorer pour éviter les traitements inutiles
    private static final List<String> STATIC_RESOURCE_PATTERNS = Arrays.asList(
        "/admin/css/", "/admin/js/", "/admin/images/", "/admin/fonts/", "/admin/assets/",
        "/css/", "/js/", "/images/", "/fonts/", "/favicon.ico", "/@vite/", "/static/"
    );

    public AdminJwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        System.out.println("AdminJwtAuthenticationFilter - Checking if should filter: " + path);
        
        // Ne pas filtrer les requêtes qui ne sont pas pour l'administration
        if (!path.startsWith("/admin/") && !path.startsWith("/api/admin/")) {
            System.out.println("AdminJwtAuthenticationFilter - Not an admin path, skipping filter");
            return true;
        }
        
        // Ne pas filtrer la page de connexion admin et ses ressources
        if (path.equals("/admin/login") || 
            path.startsWith("/admin/login/") ||
            path.equals("/admin") ||
            path.equals("/admin/")) {
            System.out.println("AdminJwtAuthenticationFilter - Admin login page or related, skipping filter");
            return true;
        }
        
        // Ne pas filtrer les ressources statiques
        if (STATIC_RESOURCE_PATTERNS.stream().anyMatch(path::contains)) {
            System.out.println("AdminJwtAuthenticationFilter - Static resource, skipping filter");
            return true;
        }
        
        // Ne pas filtrer les requêtes OPTIONS (nécessaires pour CORS)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("AdminJwtAuthenticationFilter - OPTIONS request, skipping filter");
            return true;
        }
        
        // Ne pas filtrer les chemins de l'API d'authentification admin
        if (path.startsWith("/api/admin/auth/")) {
            System.out.println("AdminJwtAuthenticationFilter - Admin auth API, skipping filter");
            return true;
        }
        
        System.out.println("AdminJwtAuthenticationFilter - Should filter this request: " + path);
        // Filtrer toutes les autres requêtes d'administration
        return false;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request, 
        @NonNull HttpServletResponse response, 
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String jwt = extractAdminJwtToken(request);
        
        if (jwt == null) {
            System.out.println("DEBUG: Aucun token JWT admin trouvé pour: " + request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        
        // Éviter la re-authentification si déjà authentifié dans cette requête
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final Claims claims = jwtService.validateTokenAndGetClaims(jwt, "ADMIN_ACCESS");
            final String username = claims.getSubject();
            
            if (username != null) {
                UserDetails adminDetails = userDetailsService.loadUserByUsername(username);
                
                // Vérifier que l'utilisateur a bien un rôle ADMIN
                if (adminDetails.getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    System.out.println("WARN: Tentative d'accès admin avec un compte non-admin: " + username);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    adminDetails, null, adminDetails.getAuthorities()
                );
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("DEBUG: Authentification admin réussie pour: " + username);
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            clearAdminJwtCookie(response);
        } catch (Exception e) {
            System.out.println("WARN: Erreur d'authentification admin: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractAdminJwtToken(HttpServletRequest request) {
        // Chercher dans le cookie dédié aux admins
        String cookieToken = extractJwtTokenFromCookie(request, "admin_jwt_token");
        if (cookieToken != null) return cookieToken;
        
        // Fallback sur le header Authorization pour les appels API
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty() && !"undefined".equalsIgnoreCase(token) && !"null".equalsIgnoreCase(token)) {
                return token;
            }
        }
        
        return null;
    }

    private String extractJwtTokenFromCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.isBlank()) return value;
                }
            }
        }
        return null;
    }
    
    private void clearAdminJwtCookie(HttpServletResponse response) {
        Cookie expiredCookie = new Cookie("admin_jwt_token", "");
        expiredCookie.setMaxAge(0);
        expiredCookie.setPath("/");
        expiredCookie.setHttpOnly(true);
        response.addCookie(expiredCookie);
    }
}

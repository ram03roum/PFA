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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    // Chemins à ignorer pour éviter les traitements inutiles
    private static final List<String> STATIC_RESOURCE_PATTERNS = Arrays.asList(
        "/css/", "/js/", "/images/", "/fonts/", "/favicon.ico", "/@vite/", "/static/"
    );

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        System.out.println("JwtAuthenticationFilter - Checking path: " + path);
        
        // Ignorer les ressources statiques pour améliorer les performances
        if (STATIC_RESOURCE_PATTERNS.stream().anyMatch(path::contains)) {
            System.out.println("JwtAuthenticationFilter - Excluding static resource: " + path);
            return true;
        }
        
        // Ne pas exclure /api/auth/status afin que le cookie jwt_token puisse authentifier cette requête
        // On peut néanmoins ignorer explicitement les endpoints de login/register
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            System.out.println("JwtAuthenticationFilter - Skipping auth login/register path: " + path);
            return true;
        }
        
        System.out.println("JwtAuthenticationFilter - Should filter path: " + path);
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String jwt = extractJwtToken(request);
        
        if (jwt == null) {
            // Seulement logger pour les endpoints API importants
            String uri = request.getRequestURI();
            if (uri.startsWith("/api/") && !uri.equals("/api/auth/status")) {
                System.out.println("DEBUG: Aucun token JWT pour: " + uri);
            }
            if (uri.startsWith("/admin/")) {
                System.out.println("DEBUG: Aucun token JWT pour page admin: " + uri);
            }
            filterChain.doFilter(request, response);
            return;
        }
        
        // Éviter la re-authentification si déjà authentifié dans cette requête
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final Claims claims = jwtService.validateTokenAndGetClaims(jwt);
            final String username = claims.getSubject();
            
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
                // Logger seulement les authentifications réussies importantes
                if (request.getRequestURI().startsWith("/api/admin/")) {
                    System.out.println("DEBUG: Authentification admin réussie pour: " + username);
                }
                if (request.getRequestURI().startsWith("/admin/")) {
                    System.out.println("DEBUG: Contexte authentifié pour page admin: " + username);
                }
            }
        } catch (io.jsonwebtoken.MalformedJwtException mjme) {
            // Si le header est mal formé, tenter une récupération via cookie
            String cookieToken = extractJwtTokenFromCookie(request);
            if (cookieToken != null) {
                try {
                    final Claims claims = jwtService.validateTokenAndGetClaims(cookieToken);
                    final String username = claims.getSubject();
                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        if (request.getRequestURI().startsWith("/admin/")) {
                            System.out.println("DEBUG: Récupération via cookie réussie pour page admin: " + username);
                        }
                    }
                } catch (Exception ignored) {
                    System.out.println("WARN: Token JWT invalide détecté après fallback cookie: " + ignored.getClass().getSimpleName());
                }
            } else {
                System.out.println("WARN: Token JWT invalide détecté: MalformedJwtException");
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token expiré - nettoyer le cookie
            clearExpiredJwtCookie(request, response);
        } catch (io.jsonwebtoken.security.SecurityException e) {
            // Token invalide - logger seulement en cas d'erreur critique
            System.out.println("WARN: Token JWT invalide détecté: " + e.getClass().getSimpleName());
        } catch (Exception e) {
            // Autres erreurs - logger pour debug
            System.out.println("ERROR: Erreur validation JWT: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractJwtToken(HttpServletRequest request) {
        // Chercher dans l'en-tête Authorization en priorité
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (token.isEmpty() || "undefined".equalsIgnoreCase(token) || "null".equalsIgnoreCase(token)) {
                // Fallback vers cookie si le header est vide/incorrect
                String cookieToken = extractJwtTokenFromCookie(request);
                if (cookieToken != null) return cookieToken;
                return null;
            }
            return token;
        }
        
        // Chercher dans les cookies en fallback
        String cookieToken = extractJwtTokenFromCookie(request);
        if (cookieToken != null) return cookieToken;
        
        return null;
    }

    private String extractJwtTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                String name = cookie.getName();
                if ("jwt_token".equals(name) || "admin_jwt_token".equals(name)) {
                    String value = cookie.getValue();
                    if (value != null && !value.isBlank()) return value;
                }
            }
        }
        return null;
    }
    
    private void clearExpiredJwtCookie(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    Cookie expiredCookie = new Cookie("jwt_token", "");
                    expiredCookie.setMaxAge(0);
                    expiredCookie.setPath("/");
                    expiredCookie.setHttpOnly(true);
                    response.addCookie(expiredCookie);
                    break;
                }
            }
        }
    }
}
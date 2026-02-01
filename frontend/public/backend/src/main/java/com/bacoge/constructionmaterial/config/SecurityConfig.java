package com.bacoge.constructionmaterial.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final Environment environment;
    private final UserDetailsService userDetailsService;
    
    @Value("${app.cors.allowed-origins:http://localhost:8080,http://127.0.0.1:8080}")
    private List<String> allowedOrigins;
    
    @Value("${app.security.jwt.cookie-name:jwt_token}")
    private String jwtCookieName;

    // Configuration centralisée des chemins
    private static final class SecurityPaths {
        
        // API d'authentification publique
        static final String[] AUTH_PATHS = {
            "/api/auth/**",
            "/api/admin/auth/**",
            "/client/auth/**"
        };
        
        // Documentation et outils de développement
        static final String[] DEV_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui/**", 
            "/swagger-ui.html",
            "/h2-console/**",
            "/actuator/**"
        };
        
        // Ressources statiques
        static final String[] STATIC_RESOURCES = {
            "/css/**", "/js/**", "/images/**", "/assets/**",
            "/favicon.ico", "/uploads/**", "/fontawesome/**", 
            "/webfonts/**", "/webjars/**", "/static/**"
        };
        
        // Pages publiques client
        static final String[] PUBLIC_PAGES = {
            "/", "/home", "/catalog/**", "/products/**", "/product/**",
            "/contact", "/about", "/services", "/promotions", "/cart", 
            "/checkout", "/login", "/register", "/error", "/access-denied",
            "/product-detail/**"
        };
        
        // API publiques
        static final String[] PUBLIC_API = {
            "/api/products/**", "/api/v1/products/**", "/api/categories/**",
            "/api/promotions/**", "/client/**", "/api/v1/client/**",
            "/api/cart/**", "/api/v1/cart/**", "/api/v1/unified-cart/**",
            "/api/services/**", "/api/reviews/product/**", "/api/csrf", "/api/contact/**",
            "/api/chat/**"
        };
        
        // Administration - pages de connexion
        static final String[] ADMIN_PATHS = {
            "/admin/login", "/admin/login/**", "/admin/css/**", 
            "/admin/js/**", "/admin/images/**", "/admin/fonts/**", 
            "/admin/assets/**", "/admin/error", "/admin/access-denied"
        };
        
        // Administration - zones protégées
        static final String[] ADMIN_PROTECTED = {
            "/admin/products/**", "/admin/categories/**", "/admin/orders/**",
            "/admin/promotions/**", "/admin/users/**", "/admin/notifications/**",
            "/admin/messages/**", "/admin/dashboard/**", "/admin/reviews/**", "/admin/api/**"
        };
        
        // API d'administration
        static final String[] ADMIN_API = {
            "/api/admin/**"
        };
        
        // Zones utilisateur authentifié
        static final String[] USER_PROTECTED = {
            "/profile", "/preferences", "/order-tracking",
            "/api/addresses/**", "/api/notifications/**", "/api/messages/**"
        };
    }

    @Bean
    public HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository() {
        HttpSessionCsrfTokenRepository repo = new HttpSessionCsrfTokenRepository();
        // Aligner avec l'en-tête utilisé côté frontend
        repo.setHeaderName("X-XSRF-TOKEN");
        return repo;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain");
        
        return http
            // Enable CSRF with proper configuration
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/admin-login",
                    "/logout",
                    "/api/user/**",
                    "/api/addresses/**",
                    "/api/payment/**",
                    "/h2-console/**",
                    "/api/admin/**",
                    "/api/v1/unified-cart/**",
                    "/api/notifications/**",
                    "/api/messages/**",
                    "/api/contact/**",
                    "/api/chat/**",
                    "/admin/api/messages/**",
                    "/client/orders/**"
                )
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .anonymous(anonymous -> anonymous.disable())
            .requestCache(requestCache -> requestCache.disable())
            
            // Configuration CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Gestion des sessions
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?expired")
            )
            
            // Configuration des en-têtes
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://cdn.tailwindcss.com https://cdnjs.cloudflare.com; " +
                        "style-src 'self' 'unsafe-inline' https:; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data: https:; " +
                        "connect-src 'self' https://cdn.jsdelivr.net https://cdn.tailwindcss.com https://cdnjs.cloudflare.com https: data: ws: wss:;")
                )
            )
            
            // Configuration des autorisations
            .authorizeHttpRequests(this::configureAuthorizations)
            
            // Configuration de l'authentification
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/spring-security/login")
                .successHandler(this::handleLoginSuccess)
                .failureHandler(this::handleLoginFailure)
                .permitAll()
            )
            
            // Configuration de la déconnexion
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .logoutSuccessHandler(this::handleLogoutSuccess)
                .invalidateHttpSession(true)
                // Clear both client and admin JWT cookies
                .deleteCookies("JSESSIONID", jwtCookieName, "admin_jwt_token")
                .clearAuthentication(true)
                .permitAll()
            )
            
            // Gestion des exceptions
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(this::handleAuthenticationException)
                .accessDeniedHandler(this::handleAccessDeniedException)
            )
            
            // Filtre JWT
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Provider d'authentification
            .authenticationProvider(authenticationProvider())
            
            .build();
    }

    private void configureAuthorizations(
        org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    ) {
        log.debug("Configuring authorization rules");
        
        // Chemins publics
        authz.requestMatchers(SecurityPaths.AUTH_PATHS).permitAll()
             .requestMatchers(SecurityPaths.STATIC_RESOURCES).permitAll()
             .requestMatchers(SecurityPaths.PUBLIC_PAGES).permitAll()
             .requestMatchers(SecurityPaths.PUBLIC_API).permitAll()
             .requestMatchers(SecurityPaths.ADMIN_PATHS).permitAll()
             // Autoriser les endpoints WebSocket (SockJS handshake) pour permettre la connexion du client
             .requestMatchers("/ws/**").permitAll()
             .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
        
        // Environnement de développement
        if (isDevelopmentEnvironment()) {
            authz.requestMatchers(SecurityPaths.DEV_PATHS).permitAll();
        }
        
        // Zones d'administration
        authz.requestMatchers(SecurityPaths.ADMIN_PROTECTED).hasRole("ADMIN")
             .requestMatchers(SecurityPaths.ADMIN_API).hasRole("ADMIN");
        
        // Zones utilisateur authentifié
        authz.requestMatchers(SecurityPaths.USER_PROTECTED).authenticated();
        
        // Tout le reste nécessite une authentification
        authz.anyRequest().authenticated();
    }

    private boolean isDevelopmentEnvironment() {
        return Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(profile -> profile.contains("dev") || profile.contains("local"));
    }

    private void handleLoginSuccess(
        jakarta.servlet.http.HttpServletRequest request,
        jakarta.servlet.http.HttpServletResponse response,
        org.springframework.security.core.Authentication authentication
    ) throws IOException {
        log.debug("Login successful for user: {}", authentication.getName());
        
        // Check if user has ADMIN role
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        // Always return JSON response for API requests
        if (isApiRequest(request) || "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            // Create a more detailed user object
            String userJson = String.format(
                "{\"id\":\"%s\", \"username\":\"%s\", \"roles\":%s}",
                authentication.getName(),
                authentication.getName(),
                authentication.getAuthorities().stream()
                    .map(auth -> "\"" + auth.getAuthority() + "\"")
                    .collect(java.util.stream.Collectors.joining(",", "[", "]"))
            );
            
            writeJsonResponse(response, 200, 
                String.format(
                    "{\"success\":true, \"message\":\"Connexion réussie\", \"user\":%s, \"redirect\":\"%s\"}",
                    userJson,
                    isAdmin ? "/admin/dashboard" : "/"
                )
            );
        } else {
            // For non-API requests, handle redirects
            String targetUrl = isAdmin ? "/admin/dashboard" : "/";
            
            // Check for redirect parameter
            String redirectParam = request.getParameter("redirect");
            if (redirectParam != null && !redirectParam.isEmpty()) {
                // Basic security check to prevent open redirects
                if (redirectParam.startsWith("/") || redirectParam.startsWith(request.getContextPath() + "/")) {
                    targetUrl = redirectParam;
                }
            }
            
            // Add a script to handle redirect in case the form was submitted via AJAX
            String htmlResponse = String.format(
                "<!DOCTYPE html><html><head><title>Redirection</title>" +
                "<script>window.location.href='%s';</script>" +
                "</head><body>" +
                "<p>Redirection en cours... <a href='%s'>Cliquez ici</a> si vous n'êtes pas redirigé automatiquement.</p>" +
                "</body></html>",
                targetUrl, targetUrl
            );
            
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(htmlResponse);
        }
    }

    private void handleLoginFailure(
        jakarta.servlet.http.HttpServletRequest request,
        jakarta.servlet.http.HttpServletResponse response,
        org.springframework.security.core.AuthenticationException exception
    ) throws IOException {
        log.warn("Login failed: {}", exception.getMessage());
        
        if (isApiRequest(request)) {
            writeJsonResponse(response, 401, 
                "{\"success\": false, \"message\": \"Échec de l'authentification\", \"error\": \"" + 
                exception.getMessage() + "\"}");
        } else {
            // Check if this was an admin login attempt
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/admin/login")) {
                response.sendRedirect("/admin/login?error=true");
            } else {
                response.sendRedirect("/login?error=true");
            }
        }
    }

        private void handleLogoutSuccess(
        jakarta.servlet.http.HttpServletRequest request,
        jakarta.servlet.http.HttpServletResponse response,
        org.springframework.security.core.Authentication authentication
    ) throws IOException {
        log.debug("Logout successful");

        String targetUrl = "/login?logout=true";
        if (authentication != null && authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            targetUrl = "/admin/login?logout=true";
        }

        if (isApiRequest(request)) {
            writeJsonResponse(response, 200, 
                "{\"success\": true, \"message\": \"Déconnexion réussie\"}");
        } else {
            response.sendRedirect(targetUrl);
        }
    }

    private void handleAuthenticationException(
        jakarta.servlet.http.HttpServletRequest request,
        jakarta.servlet.http.HttpServletResponse response,
        org.springframework.security.core.AuthenticationException authException
    ) throws IOException {
        String requestUri = request.getRequestURI();
        log.warn("Authentication required for: {}", requestUri);
        
        if (isApiRequest(request)) {
            writeJsonResponse(response, 401, 
                "{\"status\": 401, \"error\": \"Non autorisé\", \"message\": \"" + 
                authException.getMessage() + "\"}");
        } else {
            String loginUrl = requestUri.startsWith("/admin") ? 
                "/admin/login?error=unauthorized" : "/login?error=unauthorized";
            response.sendRedirect(loginUrl);
        }
    }

    private void handleAccessDeniedException(
        jakarta.servlet.http.HttpServletRequest request,
        jakarta.servlet.http.HttpServletResponse response,
        org.springframework.security.access.AccessDeniedException accessDeniedException
    ) throws IOException {
        String requestUri = request.getRequestURI();
        log.warn("=== ACCÈS REFUSÉ ===");
        log.warn("URI demandée: {}", requestUri);
        log.warn("Méthode: {}", request.getMethod());
        log.warn("Utilisateur authentifié: {}", request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Non");
        log.warn("Rôles de l'utilisateur: {}", request.getUserPrincipal() != null ? "À vérifier" : "Utilisateur non authentifié");
        log.warn("Message d'erreur: {}", accessDeniedException.getMessage());
        log.warn("=====================");
        
        if (isApiRequest(request)) {
            writeJsonResponse(response, 403, 
                "{\"status\": 403, \"error\": \"Accès refusé\", \"message\": \"" + 
                accessDeniedException.getMessage() + "\"}");
        } else {
            String deniedUrl = requestUri.startsWith("/admin") ? 
                "/admin/access-denied" : "/access-denied";
            response.sendRedirect(deniedUrl);
        }
    }

    private boolean isApiRequest(jakarta.servlet.http.HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String contentType = request.getHeader("Content-Type");
        String xRequestedWith = request.getHeader("X-Requested-With");
        
        // Check if it's an AJAX request
        boolean isAjax = "XMLHttpRequest".equals(xRequestedWith);
        
        // Check content type and accept headers
        boolean hasJsonHeaders = (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) ||
                                (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE));
        
        // Only treat as API request if it's explicitly an AJAX call or has JSON headers
        // Don't treat form submissions to /api/auth/login as API requests
        return isAjax || hasJsonHeaders;
    }

    private void writeJsonResponse(
        jakarta.servlet.http.HttpServletResponse response, 
        int status, 
        String json
    ) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(json);
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Force de hachage renforcée
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.debug("Configuring CORS with allowed origins: {}", allowedOrigins);
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Inclure systématiquement les origines de dev 5173 en plus des valeurs configurées
        List<String> originPatterns = new java.util.ArrayList<>();
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            originPatterns.addAll(allowedOrigins);
        }
        if (!originPatterns.contains("http://localhost:5173")) originPatterns.add("http://localhost:5173");
        if (!originPatterns.contains("http://127.0.0.1:5173")) originPatterns.add("http://127.0.0.1:5173");
        if (!originPatterns.contains("http://localhost:8080")) originPatterns.add("http://localhost:8080");
        if (!originPatterns.contains("http://127.0.0.1:8080")) originPatterns.add("http://127.0.0.1:8080");
        configuration.setAllowedOriginPatterns(originPatterns);
        
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // En-têtes autorisés
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // En-têtes exposés
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "X-Auth-Token", "Content-Disposition", "X-CSRF-TOKEN", "X-XSRF-TOKEN"
        ));
        
        // Important: allowCredentials doit être true pour supporter les cookies d'authentification
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
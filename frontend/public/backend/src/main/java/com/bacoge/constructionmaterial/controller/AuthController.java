package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.RegisterRequest;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.service.AuthService;
import com.bacoge.constructionmaterial.service.AdminNotificationService;
import com.bacoge.constructionmaterial.service.JwtService;
import com.bacoge.constructionmaterial.service.NotificationService;
import com.bacoge.constructionmaterial.service.UserRegistrationService;
import com.bacoge.constructionmaterial.dto.payload.request.TokenRefreshRequest;
import com.bacoge.constructionmaterial.dto.payload.response.TokenRefreshResponse;
import com.bacoge.constructionmaterial.model.RefreshToken;
import com.bacoge.constructionmaterial.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur unifié pour la gestion de l'authentification
 * Gère les opérations de connexion, déconnexion, inscription et validation de token
 */
@Tag(
    name = "Authentification", 
    description = "API pour la gestion de l'authentification des utilisateurs"
)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRegistrationService userRegistrationService;
    private final RefreshTokenService refreshTokenService;
    private final NotificationService notificationService;
    private final AdminNotificationService adminNotificationService;

    public AuthController(AuthService authService, JwtService jwtService, UserRegistrationService userRegistrationService, RefreshTokenService refreshTokenService, NotificationService notificationService, AdminNotificationService adminNotificationService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userRegistrationService = userRegistrationService;
        this.refreshTokenService = refreshTokenService;
        this.notificationService = notificationService;
        this.adminNotificationService = adminNotificationService;
    }
    
    /**
     * Authentifie un utilisateur et retourne un token JWT
     * 
     * @param request Les informations d'identification de l'utilisateur
     * @return Un token JWT si l'authentification réussit
     */
    @Operation(
        summary = "Authentification d'un utilisateur",
        description = "Authentifie un utilisateur avec son email et son mot de passe et retourne un token JWT",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Authentification réussie",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Données de requête invalides",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Identifiants invalides",
                content = @Content
            )
        }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        logger.info("Tentative de connexion pour l'email: {}", request.getEmail());
        try {
            Authentication authentication = authService.authenticateUser(request.getEmail().trim(), request.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return createErrorResponse("Échec de l'authentification", HttpStatus.UNAUTHORIZED);
            }

            String accessToken = jwtService.generateToken(authentication);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(currentUser.getId());

            // Déposer le JWT dans un cookie HttpOnly côté CLIENT pour que les pages serveur /profile, /preferences, ... s'authentifient via JwtAuthenticationFilter
            addClientJwtCookie(httpResponse, accessToken);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken.getToken());
            response.put("message", "Connexion réussie");
            response.put("user", createUserResponse(currentUser));

            logger.info("Connexion réussie pour l'utilisateur: {}", currentUser.getEmail());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.warn("Échec de connexion: {}", e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Erreur lors de la connexion: {}", e.getMessage(), e);
            return createErrorResponse("Erreur interne du serveur", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Rafraîchit un token JWT expiré
     * 
     * @param refreshToken Le refresh token à utiliser pour obtenir un nouveau token d'accès
     * @return Un nouveau token JWT et un nouveau refresh token
     */
    @Operation(
        summary = "Rafraîchissement d'un token JWT",
        description = "Génère un nouveau token d'accès à partir d'un refresh token valide",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Nouveau token généré avec succès",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Refresh token manquant ou invalide",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Refresh token expiré ou révoqué",
                content = @Content
            )
        }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    String token = jwtService.generateToken(authentication);
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }
    
    /**
     * Valide un token JWT et renvoie les informations de l'utilisateur
     * 
     * @param token Le token JWT à valider
     * @return Les informations de l'utilisateur si le token est valide
     */
    @Operation(
        summary = "Validation d'un token JWT",
        description = "Vérifie la validité d'un token JWT et retourne les informations de l'utilisateur associé",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Token valide, retourne les informations de l'utilisateur",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Token invalide ou expiré",
                content = @Content
            )
        }
    )
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader(AUTH_HEADER) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
                return ResponseEntity.ok(Map.of("valid", false));
            }
            
            String token = authHeader.substring(TOKEN_PREFIX.length());
            boolean isValid = jwtService.isTokenValid(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    response.put("user", createUserResponse(currentUser));
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.warn("Erreur lors de la validation du token: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }
    
    /**
     * Vérifie le statut d'authentification de l'utilisateur actuel
     * 
     * @param request La requête HTTP pour récupérer les informations de session
     * @return Le statut d'authentification et les informations utilisateur
     */
    @Operation(
        summary = "Vérification du statut d'authentification",
        description = "Vérifie si l'utilisateur est authentifié et retourne ses informations",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Statut d'authentification retourné avec succès",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
                )
            )
        }
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(HttpServletRequest request) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Vérifier si l'utilisateur est authentifié via le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = authentication != null && 
                                    authentication.isAuthenticated() && 
                                    !"anonymousUser".equals(authentication.getName());
            
            response.put("authenticated", isAuthenticated);
            response.put("isAdmin", false);
            response.put("username", null);
            
            if (isAuthenticated) {
                Object principal = authentication.getPrincipal();
                // Parfois le principal est un User du domaine, parfois un UserDetails Spring
                if (principal instanceof User) {
                    User user = (User) principal;
                    response.put("username", user.getUsername());
                    response.put("isAdmin", user.getRole() == User.UserRole.ADMIN);
                    response.put("user", createUserResponse(user));
                } else if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    response.put("username", userDetails.getUsername());
                    boolean hasAdminRole = userDetails.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
                    response.put("isAdmin", hasAdminRole);
                    // Essayer de récupérer l'utilisateur domaine pour enrichir la réponse
                    User current = authService.getCurrentUser();
                    if (current != null) {
                        response.put("user", createUserResponse(current));
                    }
                } else {
                    // Fallback: déterminer le rôle à partir des autorités de l'Authentication
                    boolean hasAdminRole = authentication.getAuthorities() != null && authentication.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
                    response.put("isAdmin", hasAdminRole);
                    response.put("username", authentication.getName());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du statut d'authentification: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            response.put("isAdmin", false);
            response.put("username", null);
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Enregistre un nouvel utilisateur
     *
     * @param request Les informations d'inscription de l'utilisateur
     * @return Les détails de l'utilisateur créé
     */
    @Operation(
        summary = "Inscription d'un nouvel utilisateur",
        description = "Crée un nouveau compte utilisateur avec les informations fournies",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Utilisateur créé avec succès",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = User.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Données de requête invalides ou email déjà utilisé",
                content = @Content
            )
        }
    )
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Tentative d'inscription pour l'email: {}", request.getEmail());
        
        try {
            // Validation des entrées
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return createErrorResponse("L'email est obligatoire", HttpStatus.BAD_REQUEST);
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return createErrorResponse("Le mot de passe est obligatoire", HttpStatus.BAD_REQUEST);
            }
            
            // Enregistrement de l'utilisateur
            User user = userRegistrationService.registerUser(request);
            
            // Envoyer une notification d'inscription
            try {
                notificationService.sendUserRegisteredNotification(user);
                logger.info("Notification d'inscription envoyée pour l'utilisateur: {}", user.getEmail());
                try { adminNotificationService.createUserRegistrationNotification(user); } catch (Exception ex) { logger.warn("WS notify user registration failed: {}", ex.getMessage()); }
            } catch (Exception e) {
                logger.warn("Erreur lors de l'envoi de la notification d'inscription: {}", e.getMessage());
                // Ne pas faire échouer l'inscription si la notification échoue
            }
            
            // Génération du token JWT
            String token = jwtService.generateToken(user);
            
            // Construction de la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            response.put("token", token);
            response.put("user", createUserResponse(user));
            
            logger.info("Inscription réussie pour l'utilisateur: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Erreur de validation lors de l'inscription: {}", e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Erreur lors de l'inscription: {}", e.getMessage(), e);
            return createErrorResponse("Erreur lors de l'inscription. Veuillez réessayer.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ===== MÉTHODES UTILITAIRES =====
    
    /**
     * Crée une réponse d'utilisateur standardisée
     */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("fullName", user.getFullName());
        userMap.put("role", user.getRole().toString());
        return userMap;
    }
    
    /**
     * Crée une réponse d'erreur standardisée
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String errorMessage, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorMessage);
        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Ajoute un cookie HttpOnly contenant le JWT pour l'authentification des pages serveur
     */
    private void addClientJwtCookie(HttpServletResponse response, String jwt) {
        ResponseCookie cookie = ResponseCookie
            .from("jwt_token", jwt)
            .httpOnly(true)
            .secure(false) // Mettre à true en production (HTTPS)
            .path("/")
            .maxAge(24 * 60 * 60)
            .sameSite("Lax") // ports différents mais même site (localhost) -> Lax suffit en dev HTTP
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void addAdminJwtCookie(HttpServletResponse response, String jwt) {
        ResponseCookie cookie = ResponseCookie
            .from("admin_jwt_token", jwt)
            .httpOnly(true)
            .secure(false) // Mettre à true en production (HTTPS)
            .path("/")
            .maxAge(24 * 60 * 60)
            .sameSite("Lax")
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    
    // ===== CLASSES INTERNES =====
    
    /**
     * Requête de connexion
     */
    public static class LoginRequest {
        private String email;
        private String password;
        
        // Getters et setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    

    
    @PostMapping("/admin-login")
    @Operation(
        summary = "Authentification administrateur",
        description = "Authentifie un administrateur avec son email et son mot de passe",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations d'identification de l'administrateur",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class, example = "{\n  \"email\": \"admin@bacoge.fr\",\n  \"password\": \"votre_mot_de_passe\"\n}")
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Authentification administrateur réussie",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\n  \"success\": true,\n  \"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"message\": \"Connexion administrateur réussie\",\n  \"user\": {\n    \"id\": 1,\n    \"email\": \"admin@bacoge.fr\",\n    \"firstName\": \"Admin\",\n    \"lastName\": \"System\"\n  }\n}")
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Données de requête invalides",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\n  \"success\": false,\n  \"message\": \"L'email et le mot de passe sont requis\"\n}")
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Identifiants invalides ou utilisateur non administrateur",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\n  \"success\": false,\n  \"message\": \"Email ou mot de passe incorrect\"\n}")
                )
            )
        }
    )
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse httpResponse) {
        try {
            // Vérification des champs obligatoires
            if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return createErrorResponse("L'email et le mot de passe sont requis", HttpStatus.BAD_REQUEST);
            }
            
            // Authentification de l'administrateur
            Authentication authentication = authService.authenticateAdmin(
                loginRequest.getEmail().trim(), 
                loginRequest.getPassword()
            );
            
            // Obtention de l'utilisateur actuel
            User currentUser = (User) authentication.getPrincipal();
            
            // Création du token d'accès avec type ADMIN_ACCESS
            String accessToken = jwtService.generateToken(authentication, JwtService.ADMIN_ACCESS);
            
            // Création du refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(currentUser.getId());

            // Déposer le JWT dans un cookie HttpOnly ADMIN pour l'accès aux pages /admin/** côté serveur
            addAdminJwtCookie(httpResponse, accessToken);

            // Construction de la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken.getToken());
            response.put("message", "Connexion administrateur réussie");
            response.put("user", createUserResponse(currentUser));

            return ResponseEntity.ok(response);
        
        } catch (Exception e) {
            logger.error("Erreur lors de la connexion admin: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    
    /**
     * Déconnecte l'utilisateur et invalide la session
     * 
     * @param request La requête HTTP
     * @param response La réponse HTTP
     * @return Un message de confirmation de déconnexion
     */
    @Operation(
        summary = "Déconnexion d'un utilisateur",
        description = "Déconnecte l'utilisateur et invalide la session en cours",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Déconnexion réussie",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class, example = "{\n  \"success\": true,\n  \"message\": \"Déconnexion réussie\"\n}")
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Erreur interne du serveur",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\n  \"success\": false,\n  \"error\": \"Erreur lors de la déconnexion\"\n}")
                )
            )
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.info("Déconnexion d'un utilisateur");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && !(authentication.getPrincipal() instanceof String)) {
                 User currentUser = (User) authentication.getPrincipal();
                 refreshTokenService.deleteByUserId(currentUser.getId());
                 logger.info("Refresh token supprimé pour l'utilisateur: {}", currentUser.getEmail());
            }

            // Invalider la session HTTP
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // Nettoyer le contexte de sécurité
            SecurityContextHolder.clearContext();

            // Effacer cookies côté client (jwt, admin jwt, session)
            try {
                ResponseCookie clearClientJwt = ResponseCookie
                    .from("jwt_token", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
                response.addHeader(HttpHeaders.SET_COOKIE, clearClientJwt.toString());

                ResponseCookie clearAdminJwt = ResponseCookie
                    .from("admin_jwt_token", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
                response.addHeader(HttpHeaders.SET_COOKIE, clearAdminJwt.toString());

                ResponseCookie clearSession = ResponseCookie
                    .from("JSESSIONID", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
                response.addHeader(HttpHeaders.SET_COOKIE, clearSession.toString());
            } catch (Exception ex) {
                logger.warn("Échec d'effacement des cookies lors de la déconnexion: {}", ex.getMessage());
            }

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Déconnexion réussie");

            logger.info("Déconnexion réussie");
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            logger.error("Erreur lors de la déconnexion: {}", e.getMessage(), e);
            return createErrorResponse("Erreur lors de la déconnexion", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    

}

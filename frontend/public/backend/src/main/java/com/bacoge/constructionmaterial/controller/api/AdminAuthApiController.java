package com.bacoge.constructionmaterial.controller.api;

import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.model.User.UserRole;
import com.bacoge.constructionmaterial.repository.UserRepository;
import com.bacoge.constructionmaterial.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AdminAuthApiController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateAdmin(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(name = "remember", required = false) Boolean remember) {
        
        System.out.println("Tentative de connexion admin pour l'email: " + email);
        
        try {
            // Vérifier d'abord si l'utilisateur existe
            Optional<User> userOpt = userRepository.findByEmail(email);
            System.out.println("Utilisateur trouvé: " + userOpt.isPresent());
            
            if (userOpt.isEmpty()) {
                System.out.println("Aucun utilisateur trouvé avec l'email: " + email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Identifiants invalides\"}");
            }
            
            // Vérifier si l'utilisateur est admin
            User user = userOpt.get();
            System.out.println("Rôle de l'utilisateur: " + user.getRole());
            
            if (user.getRole() != UserRole.ADMIN) {
                System.out.println("L'utilisateur n'est pas un administrateur");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("{\"error\": \"Accès refusé: droits insuffisants\"}");
            }
            
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Générer un token JWT pour l'admin
            String token = jwtService.generateToken(authentication, JwtService.ADMIN_ACCESS);
            
            // Créer un cookie HTTP-only pour le token
            long maxAge = Boolean.TRUE.equals(remember) ? (30L * 24 * 60 * 60) : (8L * 60 * 60); // 30j ou 8h
            ResponseCookie cookie = ResponseCookie.from("admin_jwt_token", token)
                    .httpOnly(true)
                    .secure(false) // Mettez à true en production avec HTTPS
                    .path("/")
                    .maxAge(maxAge)
                    .sameSite("Lax")
                    .build();
            
            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", userOpt.get());
            response.put("role", "ADMIN");
            
            // Retourner la réponse avec le cookie
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Authentication failed: " + e.getMessage() + "\"}");
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutAdmin() {
        // Créer un cookie d'expiration
        ResponseCookie cookie = ResponseCookie.from("admin_jwt_token", "")
                .httpOnly(true)
                .secure(false) // Mettez à true en production avec HTTPS
                .path("/")
                .maxAge(0) // Expire immédiatement
                .sameSite("Lax")
                .build();
        
        // Invalider la session
        SecurityContextHolder.clearContext();
        
        // Retourner la réponse avec le cookie d'expiration
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("{\"message\": \"Logout successful\"}");
    }
    
    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAdminAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            // L'utilisateur est authentifié
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", true);
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities());
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"authenticated\": false, \"error\": \"Not authenticated\"}");
    }
}

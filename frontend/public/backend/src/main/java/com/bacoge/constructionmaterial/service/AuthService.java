package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Remove @Autowired for AuthenticationManager
    // @Autowired
    // private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    
    
    public Authentication authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            logger.warn("Utilisateur non trouvé pour l'email: {}", email);
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
        
        logger.info("Utilisateur trouvé: {} avec statut: {}", user.getEmail(), user.getStatus());
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Mot de passe incorrect pour l'utilisateur: {}", email);
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            logger.warn("Compte désactivé pour l'utilisateur: {}", email);
            throw new RuntimeException("Compte utilisateur désactivé");
        }
        
        // Mettre à jour la dernière connexion
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Créer l'authentication manuellement
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
    
    public Authentication authenticateAdmin(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("Compte utilisateur désactivé");
        }
        
        // Vérifier que l'utilisateur a le rôle ADMIN
        if (user.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("Accès refusé: privilèges administrateur requis");
        }
        
        // Mettre à jour la dernière connexion
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Créer l'authentication manuellement pour définir le contexte de sécurité
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        return authentication;
    }
    
    public boolean isTokenValid(String token) {
        try {
            return jwtService.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    public User getCurrentUser() {
        logger.debug("DEBUG: Début de getCurrentUser");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("DEBUG: Authentication: {}", authentication != null ? authentication.getClass().getSimpleName() : "null");
        
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            logger.debug("DEBUG: Email extrait de l'authentication: {}", email);
            
            if ("anonymousUser".equals(email)) {
                logger.debug("DEBUG: Utilisateur anonyme détecté");
                return null;
            }
            
            User user = userRepository.findByEmail(email).orElse(null);
            logger.debug("DEBUG: Utilisateur trouvé dans la base: {}", user != null ? user.getEmail() : "null");
            return user;
        }
        
        logger.debug("DEBUG: Aucune authentication ou utilisateur non authentifié");
        return null;
    }
}
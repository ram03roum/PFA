package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.ChangePasswordRequest;
import com.bacoge.constructionmaterial.dto.UpdateProfileRequest;
import com.bacoge.constructionmaterial.dto.UserResponse;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.UserRepository;
import com.bacoge.constructionmaterial.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class UserProfileService {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Récupère le profil de l'utilisateur connecté
     */
    public UserResponse getCurrentUserProfile() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }
        
        logger.info("Récupération du profil pour l'utilisateur: {}", currentUser.getEmail());
        return new UserResponse(currentUser);
    }

    /**
     * Met à jour le profil de l'utilisateur connecté
     */
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        logger.info("Mise à jour du profil pour l'utilisateur: {}", currentUser.getEmail());

        // Vérifier si l'email a changé et s'il n'est pas déjà utilisé
        if (!currentUser.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre compte");
            }
        }

        // Mettre à jour les informations
        currentUser.setFirstName(request.getFirstName());
        currentUser.setLastName(request.getLastName());
        currentUser.setEmail(request.getEmail());
        currentUser.setPhoneNumber(request.getPhoneNumber());
        currentUser.setCountry(request.getCountry());
        currentUser.setCity(request.getCity());
        currentUser.setAddress(request.getAddress());
        currentUser.setBirthDate(request.getBirthDate());
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            currentUser.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        }

        User updatedUser = userRepository.save(currentUser);
        
        logger.info("Profil mis à jour avec succès pour l'utilisateur: {}", updatedUser.getEmail());
        
        // Envoyer un email de notification de modification de profil
        try {
            emailService.sendProfileUpdateNotification(updatedUser.getEmail(), updatedUser.getFirstName());
        } catch (Exception e) {
            logger.warn("Impossible d'envoyer l'email de notification de mise à jour du profil: {}", e.getMessage());
        }

        return new UserResponse(updatedUser);
    }

    /**
     * Change le mot de passe de l'utilisateur connecté
     */
    public void changePassword(ChangePasswordRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        logger.info("Changement de mot de passe pour l'utilisateur: {}", currentUser.getEmail());

        // Vérifier le mot de passe actuel
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect");
        }

        // Vérifier que les nouveaux mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("Les nouveaux mots de passe ne correspondent pas");
        }

        // Vérifier que le nouveau mot de passe est différent de l'ancien
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        // Mettre à jour le mot de passe
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        logger.info("Mot de passe changé avec succès pour l'utilisateur: {}", currentUser.getEmail());

        // Envoyer un email de notification de changement de mot de passe
        try {
            emailService.sendPasswordChangeNotification(currentUser.getEmail(), currentUser.getFirstName());
        } catch (Exception e) {
            logger.warn("Impossible d'envoyer l'email de notification de changement de mot de passe: {}", e.getMessage());
        }
    }

    /**
     * Désactive le compte de l'utilisateur connecté
     */
    public void deactivateAccount() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        logger.info("Désactivation du compte pour l'utilisateur: {}", currentUser.getEmail());

        currentUser.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(currentUser);

        logger.info("Compte désactivé avec succès pour l'utilisateur: {}", currentUser.getEmail());

        // Envoyer un email de confirmation de désactivation
        try {
            emailService.sendAccountDeactivationNotification(currentUser.getEmail(), currentUser.getFirstName());
        } catch (Exception e) {
            logger.warn("Impossible d'envoyer l'email de notification de désactivation: {}", e.getMessage());
        }
    }

    /**
     * Réactive le compte de l'utilisateur
     */
    public void reactivateAccount(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        User user = userOpt.get();
        logger.info("Réactivation du compte pour l'utilisateur: {}", user.getEmail());

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        logger.info("Compte réactivé avec succès pour l'utilisateur: {}", user.getEmail());
    }

    /**
     * Supprime définitivement le compte de l'utilisateur connecté
     * ATTENTION: Cette action est irréversible
     */
    public void deleteAccount() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        logger.warn("SUPPRESSION DÉFINITIVE du compte pour l'utilisateur: {}", currentUser.getEmail());

        String userEmail = currentUser.getEmail();
        String userName = currentUser.getFirstName();
        
        // Créer une notification pour l'admin avant la suppression
        try {
            notificationService.sendUserDeletedNotification(currentUser);
            logger.info("Notification de suppression envoyée pour l'utilisateur: {}", currentUser.getEmail());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification de suppression: {}", currentUser.getEmail(), e);
            // Ne pas faire échouer la suppression si la notification échoue
        }

        // Supprimer l'utilisateur de la base de données
        userRepository.delete(currentUser);

        logger.warn("Compte supprimé définitivement pour l'utilisateur: {}", userEmail);

        // Envoyer un email de confirmation de suppression
        try {
            emailService.sendAccountDeletionConfirmation(userEmail, userName);
        } catch (Exception e) {
            logger.warn("Impossible d'envoyer l'email de confirmation de suppression: {}", e.getMessage());
        }

        // Nettoyer le contexte de sécurité
        SecurityContextHolder.clearContext();
    }

    /**
     * Vérifie si un email est disponible (non utilisé par un autre compte)
     */
    public boolean isEmailAvailable(String email) {
        User currentUser = getCurrentUser();
        
        // Si c'est l'email actuel de l'utilisateur, il est disponible
        if (currentUser != null && currentUser.getEmail().equals(email)) {
            return true;
        }
        
        return !userRepository.existsByEmail(email);
    }

    /**
     * Met à jour la date de dernière connexion
     */
    public void updateLastLogin(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Dernière connexion mise à jour pour l'utilisateur: {}", email);
        }
    }

    /**
     * Récupère les préférences de l'utilisateur connecté
     */
    public Map<String, Object> getUserPreferences() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        logger.info("Récupération des préférences pour l'utilisateur: {}", currentUser.getEmail());

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("language", "fr"); // Langue par défaut
        preferences.put("currency", "EUR"); // Devise par défaut
        preferences.put("notifications", Map.of(
            "email", true,
            "sms", false,
            "push", true,
            "marketing", false
        ));
        preferences.put("privacy", Map.of(
            "profileVisibility", "private",
            "showEmail", false,
            "showPhone", false
        ));
        preferences.put("theme", "light");
        preferences.put("timezone", "Europe/Paris");

        return preferences;
    }

    /**
     * Met à jour les préférences de l'utilisateur connecté
     */
    public Map<String, Object> updateUserPreferences(Map<String, Object> preferences) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        logger.info("Mise à jour des préférences pour l'utilisateur: {}", currentUser.getEmail());

        // Dans une implémentation réelle, vous stockeriez ces préférences en base de données
        // Pour l'instant, on retourne les préférences mises à jour
        Map<String, Object> updatedPreferences = new HashMap<>(preferences);
        
        // Validation des préférences
        if (updatedPreferences.containsKey("language")) {
            String language = (String) updatedPreferences.get("language");
            if (!List.of("fr", "en", "es", "ar").contains(language)) {
                updatedPreferences.put("language", "fr");
            }
        }
        
        if (updatedPreferences.containsKey("currency")) {
            String currency = (String) updatedPreferences.get("currency");
            if (!List.of("EUR", "USD", "TND").contains(currency)) {
                updatedPreferences.put("currency", "EUR");
            }
        }

        logger.info("Préférences mises à jour avec succès pour l'utilisateur: {}", currentUser.getEmail());
        return updatedPreferences;
    }

    /**
     * Récupère l'historique de connexion de l'utilisateur connecté
     */
    public Map<String, Object> getLoginHistory() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        logger.info("Récupération de l'historique de connexion pour l'utilisateur: {}", currentUser.getEmail());

        // Simuler un historique de connexion
        List<Map<String, Object>> loginHistory = new ArrayList<>();
        
        // Dernière connexion (actuelle)
        Map<String, Object> currentLogin = new HashMap<>();
        currentLogin.put("date", LocalDateTime.now().toString());
        currentLogin.put("ipAddress", "192.168.1.100");
        currentLogin.put("device", "Chrome sur Windows");
        currentLogin.put("location", "Tunis, Tunisie");
        currentLogin.put("status", "Actuelle");
        loginHistory.add(currentLogin);
        
        // Connexions précédentes simulées
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> login = new HashMap<>();
            login.put("date", LocalDateTime.now().minusDays(i).toString());
            login.put("ipAddress", "192.168.1." + (100 + i));
            login.put("device", "Chrome sur Windows");
            login.put("location", "Tunis, Tunisie");
            login.put("status", "Terminée");
            loginHistory.add(login);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("loginHistory", loginHistory);
        result.put("totalLogins", loginHistory.size());
        result.put("lastLogin", currentUser.getLastLogin() != null ? currentUser.getLastLogin().toString() : null);

        return result;
    }

    /**
     * Active/désactive l'authentification à deux facteurs
     */
    public Map<String, Object> toggle2FA(boolean enable) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        logger.info("Modification de l'authentification à deux facteurs pour l'utilisateur: {} - Activer: {}", 
                   currentUser.getEmail(), enable);

        // Dans une implémentation réelle, vous stockeriez l'état 2FA en base de données
        Map<String, Object> twoFactorAuth = new HashMap<>();
        twoFactorAuth.put("enabled", enable);
        twoFactorAuth.put("method", enable ? "app" : null);
        twoFactorAuth.put("backupCodes", enable ? generateBackupCodes() : null);
        twoFactorAuth.put("setupDate", enable ? LocalDateTime.now().toString() : null);

        if (enable) {
            // Envoyer un email de notification d'activation 2FA
            try {
                emailService.send2FAActivationNotification(currentUser.getEmail(), currentUser.getFirstName());
            } catch (Exception e) {
                logger.warn("Impossible d'envoyer l'email de notification 2FA: {}", e.getMessage());
            }
        }

        logger.info("Authentification à deux facteurs {} pour l'utilisateur: {}", 
                   enable ? "activée" : "désactivée", currentUser.getEmail());

        return twoFactorAuth;
    }

    /**
     * Génère des codes de sauvegarde pour l'authentification à deux facteurs
     */
    private List<String> generateBackupCodes() {
        List<String> backupCodes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            backupCodes.add(String.format("%04d-%04d", 
                (int)(Math.random() * 10000), 
                (int)(Math.random() * 10000)));
        }
        return backupCodes;
    }

    /**
     * Récupère l'utilisateur connecté depuis le contexte de sécurité
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}

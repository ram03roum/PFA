package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.ChangePasswordRequest;
import com.bacoge.constructionmaterial.dto.UpdateProfileRequest;
import com.bacoge.constructionmaterial.dto.UserResponse;
import com.bacoge.constructionmaterial.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
public class UserProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserProfileService userProfileService;

    /**
     * Récupère le profil de l'utilisateur connecté
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        try {
            logger.info("Récupération du profil utilisateur");
            UserResponse userProfile = userProfileService.getCurrentUserProfile();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userProfile);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du profil: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Met à jour le profil de l'utilisateur connecté
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            logger.info("Mise à jour du profil utilisateur");
            UserResponse updatedUser = userProfileService.updateProfile(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profil mis à jour avec succès");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Erreur de validation lors de la mise à jour du profil: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du profil: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Change le mot de passe de l'utilisateur connecté
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            logger.info("Changement de mot de passe utilisateur");
            userProfileService.changePassword(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mot de passe changé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Erreur de validation lors du changement de mot de passe: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Erreur lors du changement de mot de passe: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Vérifie si un email est disponible
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
        try {
            logger.info("Vérification de la disponibilité de l'email: {}", email);
            boolean isAvailable = userProfileService.isEmailAvailable(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", isAvailable);
            response.put("message", isAvailable ? "Email disponible" : "Email déjà utilisé");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de l'email: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Désactive le compte de l'utilisateur connecté
     */
    @PutMapping("/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateAccount() {
        try {
            logger.info("Désactivation du compte utilisateur");
            userProfileService.deactivateAccount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Compte désactivé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la désactivation du compte: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Supprime définitivement le compte de l'utilisateur connecté
     * ATTENTION: Cette action est irréversible
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestParam String confirmation) {
        try {
            // Vérifier la confirmation
            if (!"DELETE_MY_ACCOUNT".equals(confirmation)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Confirmation requise. Veuillez saisir 'DELETE_MY_ACCOUNT' pour confirmer.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            logger.warn("SUPPRESSION DÉFINITIVE du compte utilisateur");
            userProfileService.deleteAccount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Compte supprimé définitivement");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du compte: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Erreur interne du serveur");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Réactive un compte désactivé (pour les administrateurs)
     */
    @PutMapping("/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reactivateAccount(@RequestParam String email) {
        try {
            logger.info("Réactivation du compte pour l'email: {}", email);
            userProfileService.reactivateAccount(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Compte réactivé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la réactivation du compte: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Récupère les préférences de l'utilisateur connecté
     */
    @GetMapping("/preferences")
    public ResponseEntity<Map<String, Object>> getUserPreferences() {
        try {
            logger.info("Récupération des préférences utilisateur");
            Map<String, Object> preferences = userProfileService.getUserPreferences();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("preferences", preferences);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des préférences: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Met à jour les préférences de l'utilisateur connecté
     */
    @PutMapping("/preferences")
    public ResponseEntity<Map<String, Object>> updateUserPreferences(@RequestBody Map<String, Object> preferences) {
        try {
            logger.info("Mise à jour des préférences utilisateur");
            Map<String, Object> updatedPreferences = userProfileService.updateUserPreferences(preferences);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Préférences mises à jour avec succès");
            response.put("preferences", updatedPreferences);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des préférences: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Récupère l'historique de connexion de l'utilisateur connecté
     */
    @GetMapping("/login-history")
    public ResponseEntity<Map<String, Object>> getLoginHistory() {
        try {
            logger.info("Récupération de l'historique de connexion");
            Map<String, Object> loginHistory = userProfileService.getLoginHistory();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("loginHistory", loginHistory);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'historique: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Récupère le statut de l'authentification à deux facteurs
     */
    @GetMapping("/2fa/status")
    public ResponseEntity<Map<String, Object>> get2FAStatus() {
        try {
            logger.info("Récupération du statut 2FA");
            
            // Pour l'instant, retourner un statut par défaut (désactivé)
            // Dans une implémentation réelle, cela viendrait de la base de données
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("enabled", false);
            response.put("method", null);
            
            logger.info("Statut 2FA récupéré avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du statut 2FA: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Active/désactive l'authentification à deux facteurs
     */
    @PutMapping("/2fa")
    public ResponseEntity<Map<String, Object>> toggle2FA(@RequestBody Map<String, Boolean> request) {
        try {
            Boolean enable = request.get("enabled");
            if (enable == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Le paramètre 'enabled' est requis");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            logger.info("Modification de l'authentification à deux facteurs: {}", enable);
            Map<String, Object> result = userProfileService.toggle2FA(enable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", enable ? "Authentification à deux facteurs activée" : "Authentification à deux facteurs désactivée");
            response.put("twoFactorAuth", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la modification de l'authentification à deux facteurs: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

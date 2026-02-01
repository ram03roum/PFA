package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.AddressDto;
import com.bacoge.constructionmaterial.service.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")

@PreAuthorize("hasAnyRole('USER','CLIENT','ADMIN')")
public class AddressController {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);
    private final AddressService addressService;
    
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }
    
    @GetMapping
    public ResponseEntity<?> getUserAddresses(Authentication authentication) {
        try {
            logger.info("=== DÉBUT getUserAddresses ===");
            logger.info("Authentification reçue: {}", authentication);
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Tentative d'accès non authentifié aux adresses");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentification requise");
            }
            
            String userEmail = authentication.getName();
            logger.info("Récupération des adresses pour l'utilisateur: {}", userEmail);
            
            // Vérifier les rôles de l'utilisateur
            if (authentication.getAuthorities() != null) {
                logger.info("Rôles de l'utilisateur: {}", 
                    authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.joining(", ")));
            }
            
            if (userEmail == null || userEmail.isEmpty()) {
                logger.error("Email utilisateur non trouvé dans l'authentification");
                return ResponseEntity.badRequest().body("Impossible d'identifier l'utilisateur");
            }
            
            List<AddressDto> addresses = addressService.getUserAddresses(userEmail);
            logger.info("{} adresses trouvées pour l'utilisateur: {}", addresses.size(), userEmail);
            
            return ResponseEntity.ok(addresses);
            
        } catch (Exception e) {
            logger.error("ERREUR lors de la récupération des adresses", e);
            Map<String, String> errorResponse = new HashMap<>();
            String errorMessage = "Erreur lors de la récupération des adresses: " + e.getMessage();
            logger.error(errorMessage);
            errorResponse.put("message", errorMessage);
            errorResponse.put("error", e.toString());
            errorResponse.put("stacktrace", java.util.Arrays.toString(e.getStackTrace()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAddress(
            @RequestBody AddressDto addressDto, 
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            logger.info("=== DEBUG SAUVEGARDE ADRESSE ===");
            logger.info("User email: {}", userEmail);
            logger.info("AddressDto reçu: name='{}', street='{}', city='{}', postalCode='{}', country='{}', type='{}', isDefault={}", 
                addressDto.getName(), addressDto.getStreet(), addressDto.getCity(), 
                addressDto.getPostalCode(), addressDto.getCountry(), addressDto.getType(), addressDto.isDefault());
            
            AddressDto createdAddress = addressService.createAddress(userEmail, addressDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("address", createdAddress);
            response.put("message", "Adresse créée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'adresse: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressDto addressDto,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            AddressDto updatedAddress = addressService.updateAddress(userEmail, id, addressDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("address", updatedAddress);
            response.put("message", "Adresse mise à jour avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            addressService.deleteAddress(userEmail, id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Adresse supprimée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping("/{id}/default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            AddressDto defaultAddress = addressService.setDefaultAddress(userEmail, id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("address", defaultAddress);
            response.put("message", "Adresse définie comme adresse par défaut");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

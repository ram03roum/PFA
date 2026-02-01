package com.bacoge.constructionmaterial.controller.api;

import com.bacoge.constructionmaterial.dto.PaymentRequestDto;
import com.bacoge.constructionmaterial.dto.PaymentResponseDto;
import com.bacoge.constructionmaterial.service.PaymentService;
import com.bacoge.constructionmaterial.repository.UserRepository;
import com.bacoge.constructionmaterial.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentApiController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Traiter un paiement par carte de crédit
     */
    @PostMapping("/card")
    public ResponseEntity<?> processCardPayment(@RequestBody PaymentRequestDto paymentRequest, HttpServletRequest request, Authentication authentication) {
        try {
            // Validation des données de paiement
            if (!isValidCardPayment(paymentRequest)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Données de paiement invalides"
                ));
            }

            // Traitement sécurisé du paiement
            PaymentResponseDto response = paymentService.processCardPayment(paymentRequest, getEffectiveUserId(authentication, request));
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transactionId", response.getTransactionId(),
                    "orderId", response.getOrderId(),
                    "message", "Paiement traité avec succès"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of(
                    "success", false,
                    "message", response.getErrorMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Erreur lors du traitement du paiement"
            ));
        }
    }

    /**
     * Traiter un paiement PayPal
     */
    @PostMapping("/paypal")
    public ResponseEntity<?> processPayPalPayment(@RequestBody PaymentRequestDto paymentRequest, HttpServletRequest request, Authentication authentication) {
        try {
            PaymentResponseDto response = paymentService.processPayPalPayment(paymentRequest, getEffectiveUserId(authentication, request));
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paypalUrl", response.getPaypalRedirectUrl(),
                    "orderId", response.getOrderId(),
                    "message", "Redirection vers PayPal"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of(
                    "success", false,
                    "message", response.getErrorMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Erreur lors de l'initialisation PayPal"
            ));
        }
    }

    /**
     * Traiter un paiement par virement bancaire
     */
    @PostMapping("/bank-transfer")
    public ResponseEntity<?> processBankTransferPayment(@RequestBody PaymentRequestDto paymentRequest, HttpServletRequest request, Authentication authentication) {
        try {
            PaymentResponseDto response = paymentService.processBankTransferPayment(paymentRequest, getEffectiveUserId(authentication, request));
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", response.getOrderId(),
                    "bankDetails", response.getBankDetails(),
                    "message", "Commande créée - En attente de virement"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", response.getErrorMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Erreur lors de la création de la commande"
            ));
        }
    }

    /**
     * Traiter un paiement à la livraison (Cash on Delivery)
     */
    @PostMapping("/cod")
    public ResponseEntity<?> processCashOnDelivery(@RequestBody PaymentRequestDto paymentRequest, HttpServletRequest request, Authentication authentication) {
        try {
            PaymentResponseDto response = paymentService.processCashOnDeliveryPayment(paymentRequest, getEffectiveUserId(authentication, request));
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", response.getOrderId(),
                    "message", "Commande créée - Paiement à la livraison"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", response.getErrorMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Erreur lors de la création de la commande (COD)"
            ));
        }
    }

    /**
     * Confirmer un paiement PayPal (callback)
     */
    @PostMapping("/paypal/confirm")
    public ResponseEntity<?> confirmPayPalPayment(@RequestParam String paymentId, @RequestParam String payerId) {
        try {
            PaymentResponseDto response = paymentService.confirmPayPalPayment(paymentId, payerId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orderId", response.getOrderId(),
                    "message", "Paiement PayPal confirmé"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(Map.of(
                    "success", false,
                    "message", response.getErrorMessage()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Erreur lors de la confirmation PayPal"
            ));
        }
    }

    /**
     * Vérifier le statut d'un paiement
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long orderId, HttpServletRequest request, Authentication authentication) {
        try {
            String paymentStatus = paymentService.getPaymentStatus(orderId, getEffectiveUserId(authentication, request));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "orderId", orderId,
                "status", paymentStatus
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Commande non trouvée"
            ));
        }
    }

    /**
     * Validation des données de paiement par carte
     */
    private boolean isValidCardPayment(PaymentRequestDto paymentRequest) {
        if (paymentRequest.getCardNumber() == null || paymentRequest.getCardNumber().length() < 13) {
            return false;
        }
        if (paymentRequest.getExpiryDate() == null || !paymentRequest.getExpiryDate().matches("\\d{2}/\\d{2}")) {
            return false;
        }
        if (paymentRequest.getCvv() == null || paymentRequest.getCvv().length() < 3) {
            return false;
        }
        if (paymentRequest.getCardHolderName() == null || paymentRequest.getCardHolderName().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Extraire l'ID utilisateur depuis la requête
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        // Cette méthode devrait extraire l'ID utilisateur du token JWT
        // Pour l'instant, on utilise une valeur par défaut
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // TODO: Décoder le JWT et extraire l'ID utilisateur
            return 1L; // Valeur temporaire
        }
        return null;
    }

    /**
     * Résout l'identité utilisateur effective à partir de l'Authentication (JWT cookie) ou de l'en-tête Authorization.
     */
    private Long getEffectiveUserId(Authentication authentication, HttpServletRequest request) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String usernameOrEmail = authentication.getName();
                User user = userRepository.findByEmail(usernameOrEmail).orElse(null);
                if (user != null) return user.getId();
            }
        } catch (Exception ignored) {}
        return getUserIdFromRequest(request);
    }
}

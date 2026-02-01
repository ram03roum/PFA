package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.admin.CreateReviewRequest;
import com.bacoge.constructionmaterial.dto.admin.ProductReviewDto;
import com.bacoge.constructionmaterial.service.admin.AdminReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final AdminReviewService reviewService;

    /**
     * Créer un nouvel avis
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(@Validated @RequestBody CreateReviewRequest request) {
        try {
            log.info("Création d'un avis pour le produit ID: {}", request.getProductId());

            ProductReviewDto review = reviewService.createReview(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("review", review);
            response.put("message", "Avis créé avec succès");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création de l'avis: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupérer tous les avis d'un produit
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductReviewDto> reviews = reviewService.getReviewsByProductId(productId, pageable);

            // Récupérer les statistiques
            AdminReviewService.ReviewStats stats = reviewService.getReviewStats(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews.getContent());
            response.put("currentPage", reviews.getNumber());
            response.put("totalItems", reviews.getTotalElements());
            response.put("totalPages", reviews.getTotalPages());
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des avis: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des avis: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Récupérer les statistiques d'un produit
     */
    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<AdminReviewService.ReviewStats> getProductReviewStats(@PathVariable Long productId) {
        try {
            AdminReviewService.ReviewStats stats = reviewService.getReviewStats(productId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Approuver un avis (Admin uniquement)
     */
    @PutMapping("/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveReview(@PathVariable Long reviewId) {
        try {
            log.info("Approbation de l'avis ID: {}", reviewId);

            ProductReviewDto review = reviewService.approveReview(reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("review", review);
            response.put("message", "Avis approuvé avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de l'approbation de l'avis: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Rejeter un avis (Admin uniquement)
     */
    @PutMapping("/{reviewId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectReview(@PathVariable Long reviewId) {
        try {
            log.info("Rejet de l'avis ID: {}", reviewId);

            ProductReviewDto review = reviewService.rejectReview(reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("review", review);
            response.put("message", "Avis rejeté avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du rejet de l'avis: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Supprimer un avis (Admin uniquement)
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long reviewId) {
        try {
            log.info("Suppression de l'avis ID: {}", reviewId);

            reviewService.deleteReview(reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avis supprimé avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'avis: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Récupérer les avis en attente d'approbation (Admin uniquement)
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductReviewDto> reviews = reviewService.getPendingReviews(pageable);
            long pendingCount = reviewService.getPendingReviewsCount();

            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviews.getContent());
            response.put("currentPage", reviews.getNumber());
            response.put("totalItems", reviews.getTotalElements());
            response.put("totalPages", reviews.getTotalPages());
            response.put("pendingCount", pendingCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des avis en attente: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la récupération des avis en attente");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

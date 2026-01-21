package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.client.ReviewDto;
import com.bacoge.constructionmaterial.service.ReviewService;
import com.bacoge.constructionmaterial.service.AuthService;
import com.bacoge.constructionmaterial.service.AdminNotificationService;
import com.bacoge.constructionmaterial.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/client/reviews")

public class ClientReviewController {
    
    private final ReviewService reviewService;
    private final AuthService authService;
    private final AdminNotificationService adminNotificationService;
    
    public ClientReviewController(ReviewService reviewService, AuthService authService, AdminNotificationService adminNotificationService) {
        this.reviewService = reviewService;
        this.authService = authService;
        this.adminNotificationService = adminNotificationService;
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewDto> reviews = reviewService.getProductReviews(productId, pageable);
        Double averageRating = reviewService.getAverageRating(productId);
        Integer totalReviews = reviewService.getReviewCount(productId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviews.getContent());
        response.put("currentPage", reviews.getNumber());
        response.put("totalPages", reviews.getTotalPages());
        response.put("totalReviews", totalReviews);
        response.put("averageRating", averageRating);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> createReview(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> request) {
        
        try {
            Long userId = null;
            if (request != null && request.get("userId") != null) {
                try { userId = Long.valueOf(request.get("userId").toString()); } catch (Exception ignored) {}
            }
            if (userId == null) {
                User current = authService.getCurrentUser();
                if (current == null) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Utilisateur non authentifié");
                    return ResponseEntity.status(401).body(error);
                }
                userId = current.getId();
            }
            Integer rating = request != null && request.get("rating") != null ? Integer.valueOf(request.get("rating").toString()) : null;
            String comment = request != null && request.get("comment") != null ? request.get("comment").toString() : null;
            
            ReviewDto review = reviewService.createReview(productId, userId, rating, comment);
            
            // Broadcast admin notification (NEW_REVIEW)
            try { adminNotificationService.createNewReviewNotification(productId, userId, rating, comment); } catch (Exception ignored) {}
            
            Map<String, Object> response = new HashMap<>();
            response.put("review", review);
            response.put("message", "Avis ajouté avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam Long userId) {
        
        try {
            reviewService.deleteReview(reviewId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avis supprimé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getUserReviews(@PathVariable Long userId) {
        List<ReviewDto> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }
}

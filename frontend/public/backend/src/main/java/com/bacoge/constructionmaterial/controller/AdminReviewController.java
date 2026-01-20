package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.client.ReviewDto;
import com.bacoge.constructionmaterial.entity.Review;
import com.bacoge.constructionmaterial.repository.ReviewRepository;
import com.bacoge.constructionmaterial.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {
    
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    
    public AdminReviewController(ReviewService reviewService, ReviewRepository reviewRepository) {
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
    }
    
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean approved) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews;
        
        if (approved != null) {
            reviews = reviewRepository.findByIsApprovedOrderByCreatedAtDesc(approved, pageable);
        } else {
            reviews = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        List<ReviewDto> reviewDtos = reviews.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewDtos);
        response.put("currentPage", reviews.getNumber());
        response.put("totalPages", reviews.getTotalPages());
        response.put("totalElements", reviews.getTotalElements());
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<Map<String, Object>> approveReview(@PathVariable Long reviewId) {
        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Avis non trouvé"));
            
            review.setIsApproved(true);
            review.setUpdatedAt(java.time.LocalDateTime.now());
            reviewRepository.save(review);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avis approuvé avec succès");
            response.put("review", convertToDto(review));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<Map<String, Object>> rejectReview(@PathVariable Long reviewId) {
        try {
            Review review = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Avis non trouvé"));
            
            review.setIsApproved(false);
            review.setUpdatedAt(java.time.LocalDateTime.now());
            reviewRepository.save(review);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avis rejeté avec succès");
            response.put("review", convertToDto(review));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewRepository.deleteById(reviewId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avis supprimé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats() {
        try {
            long totalReviews = reviewRepository.count();
            long approvedReviews = reviewRepository.countByIsApproved(true);
            long pendingReviews = reviewRepository.countByIsApproved(false);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalReviews", totalReviews);
            stats.put("approvedReviews", approvedReviews);
            stats.put("pendingReviews", pendingReviews);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProductId());
        dto.setUserId(review.getUserId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setIsApproved(review.getIsApproved());
        dto.setIsVerified(review.getIsVerified());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}
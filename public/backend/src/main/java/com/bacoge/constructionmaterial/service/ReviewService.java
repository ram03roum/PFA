package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.entity.Review;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.dto.client.ReviewDto;
import com.bacoge.constructionmaterial.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    
    public ReviewService(ReviewRepository reviewRepository, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
    }
    
    public Double getAverageRating(Long productId) {
        if (productId == null) return 0.0;
        Double avg = reviewRepository.findAverageRatingForProduct(productId);
        return avg != null ? avg : 0.0;
    }
    
    public Integer getReviewCount(Long productId) {
        if (productId == null) return 0;
        Long c = reviewRepository.countApprovedByProductId(productId);
        return c != null ? c.intValue() : 0;
    }
    
    public Page<ReviewDto> getProductReviews(Long productId, Pageable pageable) {
        if (productId == null) {
            return Page.empty(pageable);
        }
        Page<Review> reviews = reviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId, pageable);
        return reviews.map(this::convertToDto);
    }
    
    public ReviewDto createReview(Long productId, Long userId, Integer rating, String comment) {
        if (productId == null || userId == null || rating == null) {
            throw new IllegalArgumentException("Product ID, User ID, and rating are required");
        }
        
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        Review review = new Review();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setComment(comment);
        review.setIsApproved(false);
        review.setIsVerified(false);
        
        Review savedReview = reviewRepository.save(review);
        return convertToDto(savedReview);
    }
    
    public void deleteReview(Long reviewId, Long userId) {
        if (reviewId == null || userId == null) {
            throw new IllegalArgumentException("Review ID and User ID are required");
        }
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Check if the user owns this review
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this review");
        }
        
        reviewRepository.deleteById(reviewId);
    }
    
    public List<ReviewDto> getUserReviews(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // In a complete implementation, we would filter reviews by userId
        return reviewRepository.findAll().stream()
                .filter(review -> review.getUserId().equals(userId))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ReviewDto> getAllApprovedReviews() {
        return reviewRepository.findAll().stream()
                .filter(review -> review.getIsApproved())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<ReviewDto> getTopRatedReviews(int limit) {
        return reviewRepository.findAll().stream()
                .filter(review -> review.getIsApproved())
                .filter(review -> review.getRating() >= 4)
                .limit(limit)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProductId());
        dto.setUserId(review.getUserId());
        
        // Get user information
        User user = userService.findById(review.getUserId());
        if (user != null) {
            dto.setUserName(user.getFirstName() + " " + user.getLastName());
        } else {
            dto.setUserName("Utilisateur Anonyme");
        }
        
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setIsVerified(review.getIsVerified());
        dto.setIsApproved(review.getIsApproved());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}
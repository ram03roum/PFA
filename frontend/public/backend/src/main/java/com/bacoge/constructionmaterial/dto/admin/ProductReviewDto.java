package com.bacoge.constructionmaterial.dto.admin;

import com.bacoge.constructionmaterial.model.ProductReview;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDto {

    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private String userEmail;
    private Integer rating;
    private String comment;
    private String title;
    private Boolean isVerifiedPurchase;
    private Boolean isApproved;
    private Integer helpfulVotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductReviewDto fromEntity(ProductReview review) {
        ProductReviewDto dto = new ProductReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
        dto.setUserEmail(review.getUser().getEmail());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setTitle(review.getTitle());
        dto.setIsVerifiedPurchase(review.getIsVerifiedPurchase());
        dto.setIsApproved(review.getIsApproved());
        dto.setHelpfulVotes(review.getHelpfulVotes());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}

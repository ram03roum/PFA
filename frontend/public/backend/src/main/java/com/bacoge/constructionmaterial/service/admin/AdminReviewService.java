package com.bacoge.constructionmaterial.service.admin;

import com.bacoge.constructionmaterial.dto.admin.CreateReviewRequest;
import com.bacoge.constructionmaterial.dto.admin.ProductReviewDto;
import com.bacoge.constructionmaterial.model.Product;
import com.bacoge.constructionmaterial.model.ProductReview;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.ProductReviewRepository;
import com.bacoge.constructionmaterial.repository.ProductRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Créer un nouvel avis
     */
    public ProductReviewDto createReview(CreateReviewRequest request) {
        // Vérifier que le produit existe
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + request.getProductId()));

        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + request.getUserId()));

        // Vérifier qu'un utilisateur ne peut pas laisser plusieurs avis pour le même produit
        boolean alreadyReviewed = reviewRepository.findByProductIdAndIsApprovedOrderByCreatedAtDesc(
                request.getProductId(), true).stream()
                .anyMatch(review -> review.getUser().getId().equals(request.getUserId()));

        if (alreadyReviewed) {
            throw new RuntimeException("Vous avez déjà laissé un avis pour ce produit");
        }

        // Créer l'avis
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setTitle(request.getTitle());
        review.setIsVerifiedPurchase(false); // Par défaut, pas d'achat vérifié
        review.setIsApproved(true); // Approuvé automatiquement pour l'instant
        review.setHelpfulVotes(0);

        ProductReview savedReview = reviewRepository.save(review);
        return ProductReviewDto.fromEntity(savedReview);
    }

    /**
     * Récupérer tous les avis d'un produit
     */
    @Transactional(readOnly = true)
    public List<ProductReviewDto> getReviewsByProductId(Long productId) {
        List<ProductReview> reviews = reviewRepository.findByProductIdAndIsApprovedOrderByCreatedAtDesc(
                productId, true);
        return reviews.stream()
                .map(ProductReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les avis d'un produit avec pagination
     */
    @Transactional(readOnly = true)
    public Page<ProductReviewDto> getReviewsByProductId(Long productId, Pageable pageable) {
        Page<ProductReview> reviews = reviewRepository.findByProductIdAndIsApprovedOrderByCreatedAtDesc(
                productId, true, pageable);
        return reviews.map(ProductReviewDto::fromEntity);
    }

    /**
     * Récupérer les statistiques d'un produit
     */
    @Transactional(readOnly = true)
    public ReviewStats getReviewStats(Long productId) {
        long totalReviews = reviewRepository.countByProductIdAndIsApproved(productId, true);
        Double averageRating = reviewRepository.findAverageRatingByProductId(productId);

        // Compter les avis par note
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i; // Make i effectively final
            long count = reviewRepository.findByProductIdAndIsApprovedOrderByCreatedAtDesc(productId, true)
                    .stream()
                    .filter(review -> review.getRating().equals(rating))
                    .count();
            ratingDistribution.put(i, count);
        }

        return new ReviewStats(totalReviews, averageRating, ratingDistribution);
    }

    /**
     * Approuver un avis
     */
    public ProductReviewDto approveReview(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé avec l'ID: " + reviewId));

        review.setIsApproved(true);
        ProductReview savedReview = reviewRepository.save(review);
        return ProductReviewDto.fromEntity(savedReview);
    }

    /**
     * Rejeter un avis
     */
    public ProductReviewDto rejectReview(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé avec l'ID: " + reviewId));

        review.setIsApproved(false);
        ProductReview savedReview = reviewRepository.save(review);
        return ProductReviewDto.fromEntity(savedReview);
    }

    /**
     * Supprimer un avis
     */
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Avis non trouvé avec l'ID: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    /**
     * Récupérer tous les avis en attente d'approbation
     */
    @Transactional(readOnly = true)
    public Page<ProductReviewDto> getPendingReviews(Pageable pageable) {
        Page<ProductReview> reviews = reviewRepository.findByIsApprovedOrderByCreatedAtDesc(false, pageable);
        return reviews.map(ProductReviewDto::fromEntity);
    }

    /**
     * Compter les avis en attente d'approbation
     */
    @Transactional(readOnly = true)
    public long getPendingReviewsCount() {
        return reviewRepository.countByIsApproved(false);
    }

    // Classe interne pour les statistiques
    public static class ReviewStats {
        private final long totalReviews;
        private final Double averageRating;
        private final Map<Integer, Long> ratingDistribution;

        public ReviewStats(long totalReviews, Double averageRating, Map<Integer, Long> ratingDistribution) {
            this.totalReviews = totalReviews;
            this.averageRating = averageRating;
            this.ratingDistribution = ratingDistribution;
        }

        public long getTotalReviews() { return totalReviews; }
        public Double getAverageRating() { return averageRating; }
        public Map<Integer, Long> getRatingDistribution() { return ratingDistribution; }
    }
}

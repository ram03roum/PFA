package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.client.ReviewDto;
import com.bacoge.constructionmaterial.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")

public class ServiceTestimonialController {

    @Autowired
    private ReviewService reviewService;

    /**
     * Récupère les témoignages pour les services
     * Retourne tous les avis approuvés et vérifiés
     */
    @GetMapping("/reviews/testimonials")
    public ResponseEntity<List<ReviewDto>> getServiceTestimonials() {
        try {
            // Récupérer tous les avis approuvés pour affichage en témoignages
            List<ReviewDto> testimonials = reviewService.getAllApprovedReviews();
            return ResponseEntity.ok(testimonials);
        } catch (Exception e) {
            // En cas d'erreur, retourner une liste vide
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Récupère les témoignages pour la page d'accueil
     * Retourne les meilleurs avis (note >= 4) limités à 6
     */
    @GetMapping("/reviews/testimonials/home")
    public ResponseEntity<List<ReviewDto>> getHomeTestimonials() {
        try {
            List<ReviewDto> testimonials = reviewService.getTopRatedReviews(6);
            return ResponseEntity.ok(testimonials);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}

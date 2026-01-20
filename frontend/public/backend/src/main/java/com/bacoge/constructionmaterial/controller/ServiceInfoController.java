package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.dto.client.ServiceInfoDto;
import com.bacoge.constructionmaterial.dto.client.TestimonialDto;
import com.bacoge.constructionmaterial.service.ServiceInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des informations de services
 */
@RestController
@RequestMapping("/api/services")

@Tag(name = "Services", description = "API pour la gestion des services et témoignages")
public class ServiceInfoController {

    private final ServiceInfoService serviceInfoService;

    public ServiceInfoController(ServiceInfoService serviceInfoService) {
        this.serviceInfoService = serviceInfoService;
    }

    /**
     * Récupère tous les services (principaux + additionnels)
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les services", description = "Retourne la liste complète des services disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des services récupérée avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<List<ServiceInfoDto>> getAllServices() {
        try {
            List<ServiceInfoDto> services = serviceInfoService.getAllServices();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les services principaux uniquement
     */
    @GetMapping("/main")
    @Operation(summary = "Récupérer les services principaux", description = "Retourne la liste des services principaux")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Services principaux récupérés avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<List<ServiceInfoDto>> getMainServices() {
        try {
            List<ServiceInfoDto> services = serviceInfoService.getMainServices();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les services additionnels uniquement
     */
    @GetMapping("/additional")
    @Operation(summary = "Récupérer les services additionnels", description = "Retourne la liste des services additionnels")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Services additionnels récupérés avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<List<ServiceInfoDto>> getAdditionalServices() {
        try {
            List<ServiceInfoDto> services = serviceInfoService.getAdditionalServices();
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère un service par son ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un service par ID", description = "Retourne les détails d'un service spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service trouvé"),
        @ApiResponse(responseCode = "404", description = "Service non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<ServiceInfoDto> getServiceById(@PathVariable Long id) {
        try {
            ServiceInfoDto service = serviceInfoService.getServiceById(id);
            if (service != null) {
                return ResponseEntity.ok(service);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère tous les témoignages clients
     */
    @GetMapping("/testimonials")
    @Operation(summary = "Récupérer les témoignages", description = "Retourne la liste des témoignages clients")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Témoignages récupérés avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<List<TestimonialDto>> getTestimonials() {
        try {
            List<TestimonialDto> testimonials = serviceInfoService.getTestimonials();
            return ResponseEntity.ok(testimonials);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les statistiques des services
     */
    @GetMapping("/stats")
    @Operation(summary = "Récupérer les statistiques des services", description = "Retourne les statistiques générales des services")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<ServiceInfoService.ServiceStatsDto> getServiceStats() {
        try {
            ServiceInfoService.ServiceStatsDto stats = serviceInfoService.getServiceStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

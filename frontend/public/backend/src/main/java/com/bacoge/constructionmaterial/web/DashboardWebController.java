package com.bacoge.constructionmaterial.web;

import com.bacoge.constructionmaterial.service.DashboardService;
import com.bacoge.constructionmaterial.service.admin.AdminPromotionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardWebController {

    private final DashboardService dashboardService;
    private final AdminPromotionService adminPromotionService;

    public DashboardWebController(DashboardService dashboardService, AdminPromotionService adminPromotionService) {
        this.dashboardService = dashboardService;
        this.adminPromotionService = adminPromotionService;
    }

    @GetMapping
    public String dashboard(Model model) {
        try {
            // Récupérer les statistiques du tableau de bord
            var stats = dashboardService.getDashboardStats();
            var recentActivity = dashboardService.getRecentActivity();
            
            // Ajouter directement l'objet stats au modèle pour simplifier l'accès dans le template
            model.addAttribute("pageTitle", "Tableau de bord");
            model.addAttribute("activeMenu", "dashboard");
            model.addAttribute("stats", stats);
            model.addAttribute("recentActivity", recentActivity);
            
            // Données supplémentaires pour les graphiques
            model.addAttribute("salesTrends", stats.getSalesTrends() != null ? stats.getSalesTrends() : Map.of());
            model.addAttribute("topProducts", stats.getTopProducts() != null ? stats.getTopProducts() : List.of());
            model.addAttribute("salesByCategory", stats.getSalesByCategory() != null ? stats.getSalesByCategory() : Map.of());
            
            // Statistiques des promotions
            model.addAttribute("totalPromotions", adminPromotionService.getTotalPromotions());
            model.addAttribute("activePromotions", adminPromotionService.getActivePromotionsCount());
            model.addAttribute("expiredPromotions", adminPromotionService.getExpiredPromotionsCount());
            
            return "admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement du tableau de bord");
            return "admin/error";
        }
    }
    
    @GetMapping("/**")
    public String handleSubPaths() {
        return "redirect:/admin/dashboard";
    }
}

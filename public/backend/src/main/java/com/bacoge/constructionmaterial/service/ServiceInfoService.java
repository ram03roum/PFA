package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.dto.client.ServiceInfoDto;
import com.bacoge.constructionmaterial.dto.client.TestimonialDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ServiceInfoService {
    
    public List<ServiceInfoDto> getAllServices() {
        List<ServiceInfoDto> services = new ArrayList<>();
        services.addAll(getMainServices());
        services.addAll(getAdditionalServices());
        return services;
    }
    
    public List<ServiceInfoDto> getMainServices() {
        List<ServiceInfoDto> services = new ArrayList<>();
        
        List<String> livraisonFeatures = Arrays.asList(
            "Livraison sous 24h",
            "Suivi en temps réel",
            "Livraison gratuite dès 100€"
        );
        services.add(new ServiceInfoDto(1L, "Livraison Express", "Livraison sous 24h pour tous vos besoins urgents", "fas fa-truck", true, livraisonFeatures));
        
        List<String> conseilFeatures = Arrays.asList(
            "Experts certifiés",
            "Devis gratuit",
            "Accompagnement personnalisé"
        );
        services.add(new ServiceInfoDto(2L, "Conseil Expert", "Nos experts vous accompagnent dans vos projets", "fas fa-user-tie", true, conseilFeatures));
        
        List<String> garantieFeatures = Arrays.asList(
            "Conformité aux normes",
            "Garantie 2 ans",
            "Certification qualité"
        );
        services.add(new ServiceInfoDto(3L, "Garantie Qualité", "Tous nos produits sont garantis conformes aux normes", "fas fa-shield-alt", true, garantieFeatures));
        
        return services;
    }
    
    public List<ServiceInfoDto> getAdditionalServices() {
        List<ServiceInfoDto> services = new ArrayList<>();
        
        services.add(new ServiceInfoDto(4L, "Installation", "Service d'installation professionnel", "fas fa-tools", false));
        services.add(new ServiceInfoDto(5L, "Maintenance", "Entretien et maintenance de vos équipements", "fas fa-wrench", false));
        
        return services;
    }
    
    public List<TestimonialDto> getTestimonials() {
        List<TestimonialDto> testimonials = new ArrayList<>();
        
        testimonials.add(new TestimonialDto(1L, "Marie Dubois", "Architecte", 
            "Service impeccable et produits de qualité. L'équipe B&Acoge nous a accompagnés tout au long de notre projet de rénovation.", 5));
        testimonials.add(new TestimonialDto(2L, "Pierre Martin", "Entrepreneur", 
            "Livraison rapide et matériaux conformes à nos attentes. Je recommande vivement B&Acoge pour tous vos projets.", 5));
        
        return testimonials;
    }
    
    public ServiceInfoDto getServiceById(Long id) {
        // Pour l'instant, retourner null - à implémenter avec une vraie base de données
        return null;
    }
    
    public static class ServiceStatsDto {
        private int yearsExperience;
        private int satisfiedCustomers;
        private String deliveryTime;
        private double qualityRating;
        
        public ServiceStatsDto() {}
        
        public ServiceStatsDto(int yearsExperience, int satisfiedCustomers, String deliveryTime, double qualityRating) {
            this.yearsExperience = yearsExperience;
            this.satisfiedCustomers = satisfiedCustomers;
            this.deliveryTime = deliveryTime;
            this.qualityRating = qualityRating;
        }
        
        // Getters and Setters
        public int getYearsExperience() { return yearsExperience; }
        public void setYearsExperience(int yearsExperience) { this.yearsExperience = yearsExperience; }
        
        public int getSatisfiedCustomers() { return satisfiedCustomers; }
        public void setSatisfiedCustomers(int satisfiedCustomers) { this.satisfiedCustomers = satisfiedCustomers; }
        
        public String getDeliveryTime() { return deliveryTime; }
        public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
        
        public double getQualityRating() { return qualityRating; }
        public void setQualityRating(double qualityRating) { this.qualityRating = qualityRating; }
    }
    
    public ServiceStatsDto getServiceStats() {
        return new ServiceStatsDto(20, 5000, "24h", 4.9);
    }
}
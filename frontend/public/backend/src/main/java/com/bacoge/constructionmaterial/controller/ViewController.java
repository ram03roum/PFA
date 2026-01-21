package com.bacoge.constructionmaterial.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Contrôleur pour les redirections vers les pages statiques.
 * Note: La plupart des fonctionnalités sont gérées par WebController.
 */
@Controller
public class ViewController {

    // Redirection pour la page d'accueil statique
    @GetMapping("/static/home")
    public String staticHome() {
        return "forward:/index.html";
    }

    // Redirection pour les pages d'erreur
    @GetMapping("/error/404")
    public String error404() {
        return "forward:/error/404.html";
    }

    @GetMapping("/error/500")
    public String error500() {
        return "forward:/error/500.html";
    }

    // Redirections pour les pages statiques qui n'ont pas de logique métier
    @GetMapping("/static/about")
    public String about() {
        return "forward:/about/index.html";
    }

    @GetMapping("/static/contact")
    public String contact() {
        return "forward:/contact/index.html";
    }
    
    // Redirection pour les ressources statiques non gérées par défaut
    @GetMapping("/static/resources/{path:[a-zA-Z0-9-]+}")
    public String staticResources(@PathVariable String path) {
        return "forward:/" + path + "/index.html";
    }
}

package com.bacoge.constructionmaterial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.bacoge.constructionmaterial"})
@EnableJpaRepositories(basePackages = {"com.bacoge.constructionmaterial.repository"})
@EntityScan(basePackages = {"com.bacoge.constructionmaterial.model", "com.bacoge.constructionmaterial.entity"})
public class BacogeApplication {
    public static void main(String[] args) {
        // Démarrer l'application Spring Boot
        SpringApplication.run(BacogeApplication.class, args);
    }
}
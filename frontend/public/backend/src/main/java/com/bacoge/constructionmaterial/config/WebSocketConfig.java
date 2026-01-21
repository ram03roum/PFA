package com.bacoge.constructionmaterial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour les notifications en temps réel
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active un broker de messages simple en mémoire
        config.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour les messages envoyés depuis le client vers le serveur
        config.setApplicationDestinationPrefixes("/app");
        // Préfixe pour les messages utilisateur spécifiques
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée WebSocket avec support SockJS pour la compatibilité
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Point d'entrée WebSocket natif
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
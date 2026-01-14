package com.bacoge.constructionmaterial.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    // Default to gpt-3.5-turbo until fine-tuning is done
    @Value("${openai.model:gpt-3.5-turbo}")
    private String modelId;

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    public String getChatResponse(String userMessage, String contextInfo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelId);
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", userMessage);
        
        // Add system prompt to maintain persona
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        
        String systemContent = "Tu es l'assistant de 'Atmo Design'. Tu dois être concis, précis et direct (max 3 phrases).";
        if (contextInfo != null && !contextInfo.isEmpty()) {
            systemContent += "\n\nVoici les produits disponibles en stock (utilise UNIQUEMENT ceux-ci si pertinent) :\n" + contextInfo;
            systemContent += "\nSi le client demande un prix, donne le prix exact de la liste.";
        } else {
            systemContent += "\nSi tu ne connais pas la réponse ou le produit, dis simplement que tu ne sais pas.";
        }
        
        systemMessage.put("content", systemContent);

        requestBody.put("messages", List.of(systemMessage, message));
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> messageObj = (Map<String, Object>) choices.get(0).get("message");
                    return (String) messageObj.get("content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Désolé, je rencontre des difficultés techniques pour le moment (" + e.getMessage() + ").";
        }
        
        return "Désolé, je n'ai pas pu générer de réponse.";
    }
}

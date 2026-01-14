package com.bacoge.constructionmaterial.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ContactService {
    // In-memory message store to make admin views functional without a DB entity
    private final Map<Long, Map<String, Object>> messageStore = new ConcurrentHashMap<>();
    
    public Map<String, Object> getContactStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMessages", 0);
        stats.put("unreadMessages", 0);
        stats.put("responseTime", "24h");
        stats.put("satisfactionRate", 95.0);
        return stats;
    }
    
    public Map<String, Object> submitContactMessage(Map<String, Object> messageData) {
        if (messageData == null) {
            throw new IllegalArgumentException("Contact message cannot be null");
        }
        
        // Set default values
        Long id = System.currentTimeMillis();
        messageData.put("id", id);
        messageData.put("status", "NEW");
        messageData.put("createdAt", LocalDateTime.now().toString());
        messageData.putIfAbsent("name", "");
        messageData.putIfAbsent("email", "");
        messageData.putIfAbsent("subject", "");
        messageData.putIfAbsent("message", "");
        
        // Persist in-memory for now so admin pages can retrieve it
        messageStore.put(id, new HashMap<>(messageData));
        System.out.println("Contact message submitted: " + messageData.get("name") + " (#" + id + ")");
        
        return messageData;
    }
    
    public List<Map<String, Object>> getAllContactMessages() {
        return new ArrayList<>(messageStore.values());
    }
    
    public Map<String, Object> getContactMessageById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Message ID cannot be null");
        }
        Map<String, Object> existing = messageStore.get(id);
        if (existing == null) {
            throw new RuntimeException("Contact message not found with id: " + id);
        }
        return new HashMap<>(existing);
    }
    
    public Map<String, Object> updateMessageStatus(Long id, String status) {
        if (id == null || status == null) {
            throw new IllegalArgumentException("Message ID and status cannot be null");
        }
        Map<String, Object> existing = messageStore.get(id);
        if (existing == null) {
            throw new RuntimeException("Contact message not found with id: " + id);
        }
        existing.put("status", status);
        existing.put("updatedAt", LocalDateTime.now().toString());
        messageStore.put(id, existing);
        System.out.println("Contact message status updated: ID " + id + " -> " + status);
        return new HashMap<>(existing);
    }
    
    public List<Map<String, Object>> getMessagesByStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        String s = status.trim().toUpperCase();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> m : messageStore.values()) {
            Object st = m.get("status");
            if (st != null && s.equals(String.valueOf(st).toUpperCase())) {
                result.add(new HashMap<>(m));
            }
        }
        return result;
    }
    
    public static class ContactStatsDto {
        private Long totalMessages;
        private Long unreadMessages;
        private String averageResponseTime;
        private Double satisfactionRate;
        
        // Constructors
        public ContactStatsDto() {}
        
        public ContactStatsDto(Long totalMessages, Long unreadMessages, String averageResponseTime, Double satisfactionRate) {
            this.totalMessages = totalMessages;
            this.unreadMessages = unreadMessages;
            this.averageResponseTime = averageResponseTime;
            this.satisfactionRate = satisfactionRate;
        }
        
        // Getters and setters
        public Long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(Long totalMessages) { this.totalMessages = totalMessages; }
        
        public Long getUnreadMessages() { return unreadMessages; }
        public void setUnreadMessages(Long unreadMessages) { this.unreadMessages = unreadMessages; }
        
        public String getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(String averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public Double getSatisfactionRate() { return satisfactionRate; }
        public void setSatisfactionRate(Double satisfactionRate) { this.satisfactionRate = satisfactionRate; }
    }
    
    public ContactStatsDto getContactStatistics() {
        return new ContactStatsDto(0L, 0L, "24h", 95.0);
    }
    
}
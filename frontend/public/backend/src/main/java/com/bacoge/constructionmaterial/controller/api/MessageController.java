package com.bacoge.constructionmaterial.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/_stub_messages") // Deprecated stub; real endpoints exist under /api/messages and /admin/api/messages
public class MessageController {

    @GetMapping("/conversations")
    public ResponseEntity<Map<String, Object>> listConversations() {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", "Client");
        user.put("lastName", "Test");
        user.put("email", "client@example.com");

        Map<String, Object> conv = new HashMap<>();
        conv.put("id", 1);
        conv.put("user", user);
        conv.put("subject", "Support commande");
        conv.put("lastMessage", "Bonjour, j'ai une question…");
        conv.put("lastMessageDate", Instant.now().toString());
        conv.put("readByAdmin", false);
        conv.put("hasUnreadMessages", true);

        Map<String, Object> res = new HashMap<>();
        res.put("conversations", Collections.singletonList(conv));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<Map<String, Object>> getConversation(@PathVariable("id") String id) {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("id", 1);
        m1.put("senderType", "USER");
        m1.put("content", "Bonjour, j'ai une question sur ma commande");
        m1.put("createdAt", Instant.now().minusSeconds(3600).toString());

        Map<String, Object> m2 = new HashMap<>();
        m2.put("id", 2);
        m2.put("senderType", "ADMIN");
        m2.put("content", "Bonjour ! Merci pour votre message, je vous aide tout de suite.");
        m2.put("createdAt", Instant.now().minusSeconds(1800).toString());
        m2.put("isReadByUser", true);

        Map<String, Object> res = new HashMap<>();
        res.put("messages", Arrays.asList(m1, m2));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/conversations/{id}/read")
    public ResponseEntity<Map<String, Object>> markConvRead(@PathVariable("id") String id) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("id", id);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", body.getOrDefault("content", ""));
        res.put("conversationId", body.get("conversationId"));
        res.put("senderType", body.getOrDefault("senderType", "ADMIN"));
        return ResponseEntity.ok(res);
    }
}

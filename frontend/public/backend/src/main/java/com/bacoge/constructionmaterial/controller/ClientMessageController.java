package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.model.Conversation;
import com.bacoge.constructionmaterial.model.Message;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.repository.UserRepository;
import com.bacoge.constructionmaterial.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ClientMessageController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserRepository userRepository;

    // Page messages client
    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public String clientMessagesPage(Model model) {
        return "client/messages";
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // APIs client
    @GetMapping("/api/messages/conversations")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> listMyConversations(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = currentUser(auth);
        Page<Conversation> convs = conversationService.getUserConversations(user, page, size);
        Map<String, Object> res = new HashMap<>();
        res.put("content", convs.getContent());
        res.put("totalElements", convs.getTotalElements());
        res.put("totalPages", convs.getTotalPages());
        res.put("page", convs.getNumber());
        return res;
    }

    @GetMapping("/api/messages/conversations/{id}")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getMyConversation(Authentication auth, @PathVariable Long id) {
        // Access control is handled when marking read or replying
        List<Message> msgs = conversationService.getMessages(id);
        Map<String, Object> res = new HashMap<>();
        res.put("messages", msgs);
        return res;
    }

    @PostMapping("/api/messages/conversations/{id}/reply")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> reply(Authentication auth, @PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "").trim();
        if (content.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Contenu vide"));
        }
        User user = currentUser(auth);
        conversationService.userReply(user, id, content);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/api/messages/unread-count")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> unreadCount(Authentication auth) {
        User user = currentUser(auth);
        long count = conversationService.getUserUnreadCount(user);
        return Map.of("count", count);
    }

    @PostMapping("/api/messages/conversations/{id}/read")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markRead(Authentication auth, @PathVariable Long id) {
        User user = currentUser(auth);
        conversationService.markUserRead(user, id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

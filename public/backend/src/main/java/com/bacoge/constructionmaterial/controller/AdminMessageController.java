package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.model.Conversation;
import com.bacoge.constructionmaterial.model.Message;
import com.bacoge.constructionmaterial.model.User;
import com.bacoge.constructionmaterial.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessageController {

    @Autowired
    private ConversationService conversationService;

    // Page inbox admin
    @GetMapping("/messages")
    public String adminMessagesPage(Model model) {
        return "admin/messages";
    }

    // APIs admin
    @GetMapping("/api/messages/conversations")
    @ResponseBody
    @Transactional(readOnly = true)
    public Map<String, Object> listConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Conversation> convs = conversationService.getAdminConversations(page, size);
        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        for (Conversation c : convs.getContent()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("subject", c.getSubject());
            m.put("status", c.getStatus() != null ? c.getStatus().name() : null);
            m.put("createdAt", c.getCreatedAt());
            m.put("lastMessageDate", c.getLastMessageAt());
            m.put("hasUnreadMessages", c.getAdminUnreadCount() > 0);

            Map<String, Object> uobj = new HashMap<>();
            try {
                User u = c.getUser();
                if (u != null) {
                    uobj.put("id", u.getId());
                    uobj.put("firstName", u.getFirstName());
                    uobj.put("lastName", u.getLastName());
                    uobj.put("email", u.getEmail());
                }
            } catch (Exception ignore) {}
            m.put("user", uobj);
            items.add(m);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("content", items);
        res.put("totalElements", convs.getTotalElements());
        res.put("totalPages", convs.getTotalPages());
        res.put("page", convs.getNumber());
        return res;
    }

    @GetMapping("/api/messages/conversations/{id}")
    @ResponseBody
    @Transactional(readOnly = true)
    public Map<String, Object> getConversation(@PathVariable Long id) {
        List<Message> msgs = conversationService.getMessages(id);
        java.util.List<java.util.Map<String, Object>> arr = new java.util.ArrayList<>();
        for (Message msg : msgs) {
            Map<String, Object> mm = new HashMap<>();
            mm.put("id", msg.getId());
            mm.put("content", msg.getContent());
            mm.put("senderType", msg.getSenderType() != null ? msg.getSenderType().name() : null);
            mm.put("createdAt", msg.getCreatedAt());
            mm.put("isReadByUser", msg.isReadByUser());
            mm.put("isReadByAdmin", msg.isReadByAdmin());
            arr.add(mm);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("messages", arr);
        return res;
    }

    @PostMapping("/api/messages/conversations/{id}/reply")
    @ResponseBody
    public ResponseEntity<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "").trim();
        if (content.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Contenu vide"));
        }
        conversationService.adminReply(id, content, null);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/api/messages/conversations/{id}/read")
    @ResponseBody
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        conversationService.markAdminRead(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/api/messages/unread-count")
    @ResponseBody
    public Map<String, Object> unreadCount() {
        long count = conversationService.getAdminUnreadCount();
        return Map.of("count", count);
    }
}

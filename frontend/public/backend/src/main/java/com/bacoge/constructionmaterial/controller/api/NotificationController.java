package com.bacoge.constructionmaterial.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/_stub_notifications") // Deprecated: do not use in production. Real endpoints exist.
public class NotificationController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        Map<String, Object> n1 = new HashMap<>();
        n1.put("id", 1);
        n1.put("title", "Nouvelle commande");
        n1.put("message", "La commande #1001 a été passée");
        n1.put("type", "ORDER");
        n1.put("relatedEntityType", "ORDER");
        n1.put("priority", "HIGH");
        n1.put("isRead", false);
        n1.put("createdAt", Instant.now().toString());
        n1.put("actionUrl", "/admin/orders/1001");

        Map<String, Object> n2 = new HashMap<>();
        n2.put("id", 2);
        n2.put("title", "Produit en rupture");
        n2.put("message", "Le produit X est en rupture");
        n2.put("type", "PRODUCT");
        n2.put("relatedEntityType", "PRODUCT");
        n2.put("priority", "MEDIUM");
        n2.put("isRead", true);
        n2.put("createdAt", Instant.now().minusSeconds(3600).toString());
        n2.put("actionUrl", "/admin/products");

        Map<String, Object> res = new HashMap<>();
        res.put("notifications", Arrays.asList(n1, n2));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable("id") String id) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("id", id);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead() {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("updated", "all");
        return ResponseEntity.ok(res);
    }
}

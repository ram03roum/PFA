package com.bacoge.constructionmaterial.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CsrfController {

    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(CsrfToken token, HttpServletResponse response) {
        // Expose the token in a header as well (useful for debugging)
        if (token != null) {
            response.setHeader("X-CSRF-TOKEN", token.getToken());
            return Map.of("token", token.getToken());
        }
        return Map.of("token", "");
    }
}

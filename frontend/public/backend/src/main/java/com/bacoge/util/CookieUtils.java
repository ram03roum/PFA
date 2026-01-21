package com.bacoge.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

public class CookieUtils {

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .maxAge(maxAge)
                .sameSite("Lax")
                .secure(true)
                .build();
        
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serialize(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return Base64.getUrlEncoder().encodeToString(json.getBytes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());
            String json = new String(decodedBytes);
            return objectMapper.readValue(json, cls);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize object from JSON", e);
        }
    }
    
    public static <T> T deserialize(String value, TypeReference<T> typeReference) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(value);
            String json = new String(decodedBytes);
            return objectMapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize object from JSON", e);
        }
    }
}

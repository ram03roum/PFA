package com.bacoge.constructionmaterial.controller;

import com.bacoge.constructionmaterial.service.ProductImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ProductImageController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.dir:${user.home}/bacoge-uploads}")
    private String uploadDir;
    
    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        // Vérifier si le fichier est vide
        if (file.isEmpty()) {
            response.put("success", false);
            response.put("message", "Aucun fichier sélectionné");
            return ResponseEntity.badRequest().body(response);
        }

        // Vérifier la taille du fichier
        if (file.getSize() > MAX_FILE_SIZE) {
            response.put("success", false);
            response.put("message", "La taille du fichier dépasse la limite autorisée (5MB)");
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
        }
        
        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            response.put("success", false);
            response.put("message", "Type de fichier non autorisé. Types acceptés: " + String.join(", ", ALLOWED_CONTENT_TYPES));
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
        }

        try {
            // Créer le répertoire s'il n'existe pas
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom de fichier unique
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Sauvegarder le fichier
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // Retourner la réponse
            String relativePath = "/uploads/" + uniqueFilename;
            
            response.put("success", true);
            response.put("url", relativePath);
            response.put("filename", uniqueFilename);
            response.put("originalName", originalFilename);
            response.put("size", file.getSize());
            response.put("contentType", contentType);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Erreur lors du téléchargement du fichier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Aucun fichier fourni"));
        }
        
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                errors.add("Fichier invalide ou vide");
                continue;
            }
            
            try {
                ResponseEntity<Map<String, Object>> uploadResponse = uploadImage(file);
                if (uploadResponse == null) {
                    errors.add(String.format("%s: Erreur inconnue lors du téléchargement", 
                        file.getOriginalFilename()));
                    continue;
                }
                
                Map<String, Object> responseBody = uploadResponse.getBody();
                if (uploadResponse.getStatusCode().is2xxSuccessful() && responseBody != null) {
                    if (Boolean.TRUE.equals(responseBody.get("success"))) {
                        uploadedFiles.add(responseBody);
                    } else {
                        String message = responseBody.get("message") != null ? 
                            responseBody.get("message").toString() : "Erreur inconnue";
                        errors.add(String.format("%s: %s", 
                            file.getOriginalFilename(), message));
                    }
                } else {
                    String message = responseBody != null && responseBody.get("message") != null ? 
                        responseBody.get("message").toString() : 
                        "Erreur lors du téléchargement du fichier";
                    errors.add(String.format("%s: %s", 
                        file.getOriginalFilename(), message));
                }
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        response.put("success", !uploadedFiles.isEmpty() && errors.isEmpty());
        response.put("uploadedFiles", uploadedFiles);
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }
        
        return ResponseEntity.status(
            !uploadedFiles.isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST
        ).body(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            productImageService.deleteImage(id);
            response.put("success", true);
            response.put("message", "Image supprimée avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression de l'image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @DeleteMapping("/file/{filename}")
    public ResponseEntity<Map<String, Object>> deleteImageByFilename(@PathVariable String filename) {
        Map<String, Object> response = new HashMap<>();
        try {
            Path filePath = Paths.get(uploadDir, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                response.put("success", true);
                response.put("message", "Fichier supprimé avec succès");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Fichier non trouvé");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la suppression du fichier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

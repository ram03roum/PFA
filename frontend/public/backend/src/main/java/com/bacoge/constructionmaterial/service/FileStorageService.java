package com.bacoge.constructionmaterial.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadPath;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", 
        "image/png", 
        "image/gif",
        "image/webp"
    );

    @PostConstruct
    public void init() {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Dossier de téléchargement créé: {}", uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Impossible de créer le dossier de téléchargement: {}", e.getMessage(), e);
            throw new RuntimeException("Impossible d'initialiser le stockage des fichiers", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, "");
    }
    
    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        // Validation du fichier
        validateFile(file);
        
        // Créer le sous-dossier s'il n'existe pas
        Path uploadDir = Paths.get(uploadPath, subDirectory);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.info("Dossier de téléchargement créé: {}", uploadDir.toAbsolutePath());
        }
        
        // Créer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // Enregistrer le fichier dans le sous-dossier
        Path targetLocation = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Fichier enregistré avec succès: {}", targetLocation.toAbsolutePath());
        
        // Retourner le chemin relatif pour le stockage en base de données
        return subDirectory.isEmpty() ? fileName : subDirectory + "/" + fileName;
    }
    
    public List<String> storeMultipleFiles(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> filenames = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                if (!file.isEmpty()) {
                    String filename = storeFile(file);
                    filenames.add(filename);
                }
            } catch (IOException e) {
                log.error("Erreur lors de l'enregistrement d'un fichier: {}", e.getMessage(), e);
                // Continuer avec les fichiers suivants
            }
        }
        
        return filenames;
    }
    
    public boolean deleteFile(String filename) {
        try {
            if (filename == null || filename.isEmpty()) {
                return false;
            }
            
            Path filePath = Paths.get(uploadPath).resolve(filename).normalize();
            if (!Files.exists(filePath)) {
                log.warn("Le fichier n'existe pas: {}", filename);
                return false;
            }
            
            Files.delete(filePath);
            log.info("Fichier supprimé avec succès: {}", filename);
            return true;
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier {}: {}", filename, e.getMessage(), e);
            return false;
        }
    }
    
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }
        
        // Vérifier la taille du fichier
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La taille du fichier dépasse la limite autorisée (5MB)");
        }
        
        // Vérifier le type de contenu
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Type de fichier non autorisé: " + contentType);
        }
        
        // Vérifier l'extension du fichier
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }
        
        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Extension de fichier non autorisée: " + extension);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex).toLowerCase();
    }
    
    public String getFileUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        return "/uploads/" + filename;
    }
}
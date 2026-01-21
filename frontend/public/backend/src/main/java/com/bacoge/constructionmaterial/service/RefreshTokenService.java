package com.bacoge.constructionmaterial.service;

import com.bacoge.constructionmaterial.config.properties.JwtProperties;
import com.bacoge.constructionmaterial.model.RefreshToken;
import com.bacoge.constructionmaterial.repository.RefreshTokenRepository;
import com.bacoge.constructionmaterial.repository.UserRepository;
import com.bacoge.exceptions.TokenRefreshException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        // Supprimer d'abord tout refresh token existant pour cet utilisateur
        deleteByUserId(userId);
        // Forcer le flush pour éviter des violations de contrainte unique si une contrainte existe sur user_id
        refreshTokenRepository.flush();
        
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtProperties.getRefreshExpirationMs()));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            try {
                refreshTokenRepository.deleteById(token.getId());
            } catch (org.springframework.dao.DataAccessException e) {
                // Token already deleted or other database issue, ignore
                logger.debug("Erreur lors de la suppression du refresh token ID: {}, erreur: {}", token.getId(), e.getMessage());
            }
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        try {
            // La méthode deleteByUser_Id retourne maintenant le nombre de lignes supprimées
            int deletedCount = refreshTokenRepository.deleteByUser_Id(userId);
            if (deletedCount > 0) {
                logger.debug("Suppression de {} refresh token(s) pour l'utilisateur ID: {}", deletedCount, userId);
            } else {
                logger.debug("Aucun refresh token à supprimer pour l'utilisateur ID: {}", userId);
            }
        } catch (Exception e) {
            // Ignorer toutes les erreurs liées à la suppression des refresh tokens
            // Cela évite l'erreur "Batch update returned unexpected row count"
            logger.debug("Erreur lors de la suppression des refresh tokens pour l'utilisateur ID: {} - {}", userId, e.getMessage());
        }
    }
}

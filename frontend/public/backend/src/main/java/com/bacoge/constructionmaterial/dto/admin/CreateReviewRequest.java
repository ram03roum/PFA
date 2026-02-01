package com.bacoge.constructionmaterial.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être au minimum 1")
    @Max(value = 5, message = "La note ne peut pas dépasser 5")
    private Integer rating;

    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    private String comment;

    @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
    private String title;

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;
}

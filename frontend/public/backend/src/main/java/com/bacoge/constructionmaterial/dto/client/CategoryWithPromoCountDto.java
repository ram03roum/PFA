package com.bacoge.constructionmaterial.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour afficher une catégorie avec le nombre de promotions actives associées.
 * Utilisé principalement pour l'affichage dans la sidebar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithPromoCountDto {
    private Long id;
    private String name;
    private String icon;
    private int promoCount;
}

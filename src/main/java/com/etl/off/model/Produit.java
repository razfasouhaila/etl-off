package com.etl.off.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String scoreNutritionnel;
    private Double energie_100g;
    private Double graisse_100g;
    private Double sucre_100g;
    private Double sel_100g;
    private Double fibres_100g;
    private Double proteines_100g;
    private Double vitA_100g;
    private Double vitD_100g;
    private Double vitE_100g;
    private Double vitK_100g;
    private Double vitC_100g;
    private Double vitB1_100g;
    private Double vitB2_100g;
    private Double vitPP_100g;
    private Double vitB6_100g;
    private Double vitB9_100g;
    private Double vitB12_100g;
    private Double calcium_100g;
    private Double magnesium_100g;
    private Double fer_100g;
    private Double betaCarotene_100g;
    private Boolean contientHuilePalme;

    @Column(length = 10000)
    private String texteIngredients;

    @ManyToOne
    @JoinColumn(name = "categorie_id") // clé étrangère vers la table Categorie
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "marque_id") // clé étrangère vers la table Marque
    private Marque marque;

    @ManyToMany
    private Set<Ingredient> ingredients;

    @ManyToMany
    private Set<Additif> additifs;

    @ManyToMany
    private Set<Allergen> allergenes;
}

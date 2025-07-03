package com.etl.off.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String nutritionScore;
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
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne
    @JoinColumn(name = "marque_id")
    private Marque marque;

    @ManyToMany
    @JoinTable(
            name = "produit_ingredients",
            joinColumns = @JoinColumn(name = "produit_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredient> ingredients;

    @ManyToMany
    @JoinTable(
            name = "produit_additifs",
            joinColumns = @JoinColumn(name = "produit_id"),
            inverseJoinColumns = @JoinColumn(name = "additif_id")
    )
    private Set<Additif> additifs;

    @ManyToMany
    @JoinTable(
            name = "produit_allergenes",
            joinColumns = @JoinColumn(name = "produit_id"),
            inverseJoinColumns = @JoinColumn(name = "allergene_id")
    )
    private Set<Allergen> allergenes;
}

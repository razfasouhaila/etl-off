package com.etl.off.service;

import com.etl.off.model.*;
import com.etl.off.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

@Service
public class ProductETLService {

    @Autowired private ProduitRepository produitRepository;
    @Autowired private MarqueRepository marqueRepository;
    @Autowired private CategorieRepository categorieRepository;
    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private AdditifRepository additifRepository;
    @Autowired private AllergenRepository allergenRepository;

    public void runETL(String csvPath) {
        int lineCount = 0;
        int successCount = 0;
        int errorCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            String[] headers = reader.readLine().split("\\|", -1);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim(), i);
            }

            while ((line = reader.readLine()) != null) {
                lineCount++;
                String[] columns = line.split("\\|", -1);
                if (columns.length < headers.length) {
                    System.err.println(" Ligne " + lineCount + " ignorée (colonnes incomplètes)");
                    errorCount++;
                    continue;
                }

                try {
                    String nomProduit = clean(get(columns, headerIndex, "nom"));
                    String nomMarque = clean(get(columns, headerIndex, "marque"));
                    String nomCategorie = clean(get(columns, headerIndex, "categorie"));

                    if (nomProduit == null || nomProduit.isBlank()) throw new Exception("Nom produit vide");
                    if (nomMarque == null || nomMarque.isBlank()) throw new Exception("Marque vide");
                    if (nomCategorie == null || nomCategorie.isBlank()) throw new Exception("Catégorie vide");

                    Produit produit = new Produit();
                    produit.setNom(nomProduit);

                    Categorie cat = categorieRepository.findByNom(nomCategorie).orElseGet(() -> {
                        Categorie c = new Categorie();
                        c.setNom(nomCategorie);
                        return categorieRepository.save(c);
                    });

                    Marque marque = marqueRepository.findByNom(nomMarque).orElseGet(() -> {
                        Marque m = new Marque();
                        m.setNom(nomMarque);
                        return marqueRepository.save(m);
                    });

                    produit.setCategorie(cat);
                    produit.setMarque(marque);

                    produit.setNutritionScore(get(columns, headerIndex, "nutritionGradeFr"));
                    produit.setEnergie_100g(parseDouble(get(columns, headerIndex, "energie100g")));
                    produit.setGraisse_100g(parseDouble(get(columns, headerIndex, "graisse100g")));
                    produit.setSucre_100g(parseDouble(get(columns, headerIndex, "sucres100g")));
                    produit.setFibres_100g(parseDouble(get(columns, headerIndex, "fibres100g")));
                    produit.setProteines_100g(parseDouble(get(columns, headerIndex, "proteines100g")));
                    produit.setSel_100g(parseDouble(get(columns, headerIndex, "sel100g")));
                    produit.setVitA_100g(parseDouble(get(columns, headerIndex, "vitA100g")));
                    produit.setVitD_100g(parseDouble(get(columns, headerIndex, "vitD100g")));
                    produit.setVitE_100g(parseDouble(get(columns, headerIndex, "vitE100g")));
                    produit.setVitK_100g(parseDouble(get(columns, headerIndex, "vitK100g")));
                    produit.setVitC_100g(parseDouble(get(columns, headerIndex, "vitC100g")));
                    produit.setVitB1_100g(parseDouble(get(columns, headerIndex, "vitB1100g")));
                    produit.setVitB2_100g(parseDouble(get(columns, headerIndex, "vitB2100g")));
                    produit.setVitPP_100g(parseDouble(get(columns, headerIndex, "vitPP100g")));
                    produit.setVitB6_100g(parseDouble(get(columns, headerIndex, "vitB6100g")));
                    produit.setVitB9_100g(parseDouble(get(columns, headerIndex, "vitB9100g")));
                    produit.setVitB12_100g(parseDouble(get(columns, headerIndex, "vitB12100g")));
                    produit.setCalcium_100g(parseDouble(get(columns, headerIndex, "calcium100g")));
                    produit.setMagnesium_100g(parseDouble(get(columns, headerIndex, "magnesium100g")));
                    produit.setFer_100g(parseDouble(get(columns, headerIndex, "fer100g")));
                    produit.setBetaCarotene_100g(parseDouble(get(columns, headerIndex, "betaCarotene100g")));
                    produit.setContientHuilePalme("1".equals(get(columns, headerIndex, "presenceHuilePalme")));
                    produit.setTexteIngredients(get(columns, headerIndex, "ingredients"));


                    produitRepository.save(produit);
                    successCount++;
                } catch (Exception e) {
                    System.err.println(" Erreur ligne " + lineCount + ": " + e.getMessage());
                    errorCount++;
                }
            }

        } catch (Exception e) {
            System.err.println(" Erreur d'ouverture du fichier CSV : " + e.getMessage());
        }

        System.out.println("\n Résumé :");
        System.out.println("Lignes totales traitées : " + lineCount);
        System.out.println(" Insérées avec succès : " + successCount);
        System.out.println(" Ignorées en erreur : " + errorCount);
    }

    private String get(String[] columns, Map<String, Integer> map, String key) {
        Integer idx = map.get(key);
        return (idx != null && idx < columns.length) ? columns[idx].trim() : null;
    }

    private Double parseDouble(String val) {
        try {
            return (val != null && !val.isBlank()) ? Double.parseDouble(val) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String clean(String input) {
        if (input == null) return "";

        // Minuscule + suppression espaces en double
        input = input.trim().toLowerCase().replaceAll("\\s+", " ");

        // Supprimer tout ce qui ressemble à un pourcentage, un chiffre seul ou un code inutile
        input = input.replaceAll("\\b\\d+\\b", "");              // chiffres isolés
        input = input.replaceAll("\\d+%?", "");                  // nombres avec %
        input = input.replaceAll("\\bfr\\b|\\bvoir\\b.*", "");   // 'fr', 'voir ...'

        // Nettoyer les caractères spéciaux (hors ponctuation utile)
        input = input.replaceAll("[^a-zàâäéèêëîïôöùûüç ,\\-']", "");

        // Supprimer les virgules et points de fin
        input = input.replaceAll("^[,;\\s]+|[,;\\.\\s]+$", "");

        return input.trim();
    }


}

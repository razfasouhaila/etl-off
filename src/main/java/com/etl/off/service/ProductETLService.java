package com.etl.off.service;

import com.etl.off.model.*;
import com.etl.off.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            int lineCount = 0;
            String[] headers = reader.readLine().split("\\|", -1);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim(), i);
            }

            while ((line = reader.readLine()) != null) {
                lineCount++;
                String[] columns = line.split("\\|", -1);
                if (columns.length < headers.length) continue;

                String nomProduit = get(columns, headerIndex, "nom");
                if (nomProduit == null || nomProduit.isBlank()) continue;

                String categorieNom = get(columns, headerIndex, "categorie");
                String marqueNom = get(columns, headerIndex, "marque");

                Categorie categorie = categorieRepository.findByNom(categorieNom)
                        .orElseGet(() -> categorieRepository.save(new Categorie(categorieNom)));
                Marque marque = marqueRepository.findByNom(marqueNom)
                        .orElseGet(() -> marqueRepository.save(new Marque(marqueNom)));

                Produit produit = new Produit();
                produit.setNom(nomProduit);
                produit.setCategorie(categorie);
                produit.setMarque(marque);
                produit.setScoreNutritionnel(get(columns, headerIndex, "nutritionGradeFr"));
                produit.setTexteIngredients(get(columns, headerIndex, "ingredients"));
                produit.setEnergie_100g(parseDouble(columns, headerIndex, "energie100g"));
                produit.setGraisse_100g(parseDouble(columns, headerIndex, "graisse100g"));
                produit.setSucre_100g(parseDouble(columns, headerIndex, "sucres100g"));
                produit.setSel_100g(parseDouble(columns, headerIndex, "sel100g"));
                produit.setFibres_100g(parseDouble(columns, headerIndex, "fibres100g"));
                produit.setProteines_100g(parseDouble(columns, headerIndex, "proteines100g"));
                produit.setVitA_100g(parseDouble(columns, headerIndex, "vitA100g"));
                produit.setVitD_100g(parseDouble(columns, headerIndex, "vitD100g"));
                produit.setVitE_100g(parseDouble(columns, headerIndex, "vitE100g"));
                produit.setVitK_100g(parseDouble(columns, headerIndex, "vitK100g"));
                produit.setVitC_100g(parseDouble(columns, headerIndex, "vitC100g"));
                produit.setVitB1_100g(parseDouble(columns, headerIndex, "vitB1100g"));
                produit.setVitB2_100g(parseDouble(columns, headerIndex, "vitB2100g"));
                produit.setVitPP_100g(parseDouble(columns, headerIndex, "vitPP100g"));
                produit.setVitB6_100g(parseDouble(columns, headerIndex, "vitB6100g"));
                produit.setVitB9_100g(parseDouble(columns, headerIndex, "vitB9100g"));
                produit.setVitB12_100g(parseDouble(columns, headerIndex, "vitB12100g"));
                produit.setCalcium_100g(parseDouble(columns, headerIndex, "calcium100g"));
                produit.setMagnesium_100g(parseDouble(columns, headerIndex, "magnesium100g"));
                produit.setFer_100g(parseDouble(columns, headerIndex, "iron100g"));
                produit.setBetaCarotene_100g(parseDouble(columns, headerIndex, "betaCarotene100g"));
                produit.setContientHuilePalme(parseBoolean(columns, headerIndex, "presenceHuilePalme"));

                produitRepository.save(produit);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String get(String[] cols, Map<String, Integer> idx, String key) {
        int i = idx.getOrDefault(key, -1);
        return (i >= 0 && i < cols.length) ? cols[i].trim() : null;
    }

    private Double parseDouble(String[] cols, Map<String, Integer> idx, String key) {
        try {
            String val = get(cols, idx, key);
            return (val == null || val.isBlank()) ? null : Double.parseDouble(val);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBoolean(String[] cols, Map<String, Integer> idx, String key) {
        String val = get(cols, idx, key);
        return val != null && (val.equals("1") || val.equalsIgnoreCase("true"));
    }
}
package com.etl.off.service;

import com.etl.off.model.*;
import com.etl.off.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductETLService {

    @Autowired private ProduitRepository produitRepository;
    @Autowired private MarqueRepository marqueRepository;
    @Autowired private CategorieRepository categorieRepository;
    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private AdditifRepository additifRepository;
    @Autowired private AllergenRepository allergenRepository;

    private Map<String, Ingredient> existingIngredients;
    private Map<String, Additif> existingAdditifs;
    private Map<String, Allergen> existingAllergens;
    private Map<String, Marque> existingMarques;
    private Map<String, Categorie> existingCategories;

    public void runETL(String csvPath) {
        nettoyerIngredientsExistants();
        preloadCaches();

        int lineCount = 0, successCount = 0, errorCount = 0;
        int batchSize = 500;
        List<Produit> produitsBatch = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String[] headers = reader.readLine().split("\\|", -1);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim(), i);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                try {
                    String[] columns = line.split("\\|", -1);
                    if (columns.length < headers.length) {
                        errorCount++;
                        continue;
                    }

                    Produit produit = processLine(columns, headerIndex);
                    produitsBatch.add(produit);
                    successCount++;

                    if (produitsBatch.size() >= batchSize) {
                        produitRepository.saveAll(produitsBatch);
                        produitsBatch.clear();
                    }
                } catch (Exception e) {
                    errorCount++;
                }
            }

            if (!produitsBatch.isEmpty()) {
                produitRepository.saveAll(produitsBatch);
            }

        } catch (Exception e) {
            System.err.println("Erreur lecture fichier : " + e.getMessage());
        }

        System.out.println("--- Résumé ---");
        System.out.println("Lignes traitées : " + lineCount);
        System.out.println("Succès : " + successCount);
        System.out.println("Erreurs : " + errorCount);
        System.out.println("Durée totale : " + (System.currentTimeMillis() - startTime) / 1000.0 + " s");
    }

    private void preloadCaches() {
        existingIngredients = ingredientRepository.findAll().stream()
                .collect(Collectors.toMap(i -> i.getNom().toLowerCase(), i -> i));
        existingAdditifs = additifRepository.findAll().stream()
                .collect(Collectors.toMap(a -> a.getCode().toLowerCase(), a -> a));
        existingAllergens = allergenRepository.findAll().stream()
                .collect(Collectors.toMap(a -> a.getNom().toLowerCase(), a -> a));
        existingMarques = marqueRepository.findAll().stream()
                .collect(Collectors.toMap(m -> m.getNom().toLowerCase(), m -> m));
        existingCategories = categorieRepository.findAll().stream()
                .collect(Collectors.toMap(c -> c.getNom().toLowerCase(), c -> c));
    }

    private Produit processLine(String[] columns, Map<String, Integer> headerIndex) throws Exception {
        String nomProduit = clean(get(columns, headerIndex, "nom"));
        String nomMarque = clean(get(columns, headerIndex, "marque"));
        String nomCategorie = clean(get(columns, headerIndex, "categorie"));

        if (nomProduit == null || nomProduit.isBlank()) throw new Exception("Nom produit vide");
        if (nomMarque == null || nomMarque.isBlank()) throw new Exception("Marque vide");
        if (nomCategorie == null || nomCategorie.isBlank()) throw new Exception("Catégorie vide");

        Produit produit = new Produit();
        produit.setNom(nomProduit);

        Categorie cat = existingCategories.computeIfAbsent(nomCategorie.toLowerCase(), key -> {
            Categorie c = new Categorie(nomCategorie);
            return categorieRepository.save(c);
        });

        Marque marque = existingMarques.computeIfAbsent(nomMarque.toLowerCase(), key -> {
            Marque m = new Marque(nomMarque);
            return marqueRepository.save(m);
        });

        produit.setCategorie(cat);
        produit.setMarque(marque);
        produit.setIngredients(parseIngredients(get(columns, headerIndex, "ingredients")));
        produit.setAdditifs(parseAdditifs(get(columns, headerIndex, "additifs")));
        produit.setAllergenes(parseAllergenes(get(columns, headerIndex, "allergenes")));
        produit.setTexteIngredients(get(columns, headerIndex, "ingredients"));

        setNutritionValues(produit, columns, headerIndex);
        return produit;
    }

    private Set<Ingredient> parseIngredients(String raw) {
        Set<Ingredient> set = new HashSet<>();
        if (raw == null) return set;
        String[] parts = raw.split("[,;\\-]");
        for (String part : parts) {
            String cleaned = clean(part);
            if (cleaned.length() > 1) {
                Ingredient i = existingIngredients.get(cleaned.toLowerCase());
                if (i == null) {
                    i = new Ingredient(cleaned);
                    i = ingredientRepository.save(i);
                    existingIngredients.put(cleaned.toLowerCase(), i);
                }
                set.add(i);
            }
        }
        return set;
    }

    private Set<Allergen> parseAllergenes(String raw) {
        Set<Allergen> set = new HashSet<>();
        if (raw == null) return set;
        String[] parts = raw.split("[,;\\-]");
        for (String part : parts) {
            String cleaned = clean(part);
            if (!cleaned.isBlank()) {
                Allergen a = existingAllergens.get(cleaned.toLowerCase());
                if (a == null) {
                    a = new Allergen(cleaned);
                    a = allergenRepository.save(a);
                    existingAllergens.put(cleaned.toLowerCase(), a);
                }
                set.add(a);
            }
        }
        return set;
    }

    private Set<Additif> parseAdditifs(String raw) {
        Set<Additif> set = new HashSet<>();
        if (raw == null || raw.isBlank()) return set;

        String[] parts = raw.split(",");
        for (String part : parts) {
            String cleaned = clean(part);
            if (cleaned.length() <= 1) continue;

            String code = null, nom = null;
            if (cleaned.matches("e\\d+[a-z]*\\s*-\\s*.*")) {
                String[] tokens = cleaned.split("\\s*-\\s*", 2);
                code = tokens[0].toUpperCase();
                nom = tokens.length > 1 ? tokens[1].trim() : code;
            } else if (cleaned.matches("e\\d+[a-z]*")) {
                code = cleaned.toUpperCase();
                nom = code;
            } else {
                continue; // ignore malformés
            }

            if (code == null || code.length() < 2) continue;

            Additif a = existingAdditifs.get(code.toLowerCase());
            if (a == null) {
                try {
                    a = new Additif(nom, code);
                    a = additifRepository.save(a);
                    existingAdditifs.put(code.toLowerCase(), a);
                } catch (Exception e) {
                    System.err.println("Additif ignoré : " + code + " - " + nom);
                    continue;
                }
            }
            set.add(a);
        }

        return set;
    }

    private void setNutritionValues(Produit produit, String[] columns, Map<String, Integer> headerIndex) {
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
        input = input.trim().toLowerCase().replaceAll("\\s+", " ");
        input = input.replaceAll("\\(.*?\\)", "");
        input = input.replaceAll("\\d+%+", "");
        input = input.replaceAll("[*_]", "");
        input = input.replaceAll("\\bfr\\b|\\bvoir\\b.*", "");
        input = input.replaceAll("^[,;\\.\\s']+|[,;\\.\\s']+$", "");
        return input.trim();
    }

    public void nettoyerIngredientsExistants() {
        List<Ingredient> all = ingredientRepository.findAll();
        int supprimes = 0;

        for (Ingredient ing : all) {
            String nomNettoye = clean(ing.getNom());
            boolean mauvais = nomNettoye.length() > 50 || nomNettoye.matches(".*\\d.*") || nomNettoye.isBlank();

            if (mauvais) {
                ingredientRepository.delete(ing);
                supprimes++;
            } else if (!nomNettoye.equals(ing.getNom())) {
                ing.setNom(nomNettoye);
                ingredientRepository.save(ing);
            }
        }

        System.out.println("Nettoyage terminé : " + supprimes + " supprimés");
    }
}

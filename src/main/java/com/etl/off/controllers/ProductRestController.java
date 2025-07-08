package com.etl.off.controllers;


import com.etl.off.model.Produit;
import com.etl.off.repository.AdditifRepository;
import com.etl.off.repository.AllergenRepository;
import com.etl.off.repository.IngredientRepository;
import com.etl.off.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@RestController
@RequestMapping("/api")
public class ProductRestController {

    @Autowired private ProduitRepository produitRepository;
    @Autowired private IngredientRepository ingredientRepository;
    @Autowired private AdditifRepository additifRepository;
    @Autowired private AllergenRepository allergenRepository;

    // 1. Top N produits pour une marque
    @GetMapping("/products/top-by-brand")
    public List<Produit> getTopProductsByBrand(@RequestParam String brand, @RequestParam int limit) {
        return (List<Produit>) produitRepository.findByMarqueNomIgnoreCaseOrderByIdAsc(brand)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 2. Top N produits pour une catégorie
    @GetMapping("/products/top-by-category")
    public List<Produit> getTopProductsByCategory(@RequestParam String category, @RequestParam int limit) {
        return (List<Produit>) produitRepository.findByCategorieNomIgnoreCaseOrderByIdAsc(category)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 3. Top N produits pour une marque + catégorie
    @GetMapping("/products/top-by-brand-category")
    public List<Produit> getTopProductsByBrandAndCategory(@RequestParam String brand, @RequestParam String category, @RequestParam int limit) {
        return produitRepository.findByMarqueNomIgnoreCaseAndCategorieNomIgnoreCaseOrderByIdAsc(brand, category)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

}

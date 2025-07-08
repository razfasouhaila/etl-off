package com.etl.off.controllers;

import com.etl.off.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientRestController {

    @Autowired
    private ProduitRepository produitRepository;

    @GetMapping("/top")
    public List<Map.Entry<String, Long>> getTopIngredients(@RequestParam int limit) {
        return produitRepository.findAll().stream()
                .flatMap(p -> p.getIngredients().stream())
                .collect(Collectors.groupingBy(i -> i.getNom(), Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}

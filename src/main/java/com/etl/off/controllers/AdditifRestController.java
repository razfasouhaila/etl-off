package com.etl.off.controllers;

import com.etl.off.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/additives")
public class AdditifRestController {

    @Autowired
    private ProduitRepository produitRepository;

    @GetMapping("/top")
    public List<Map.Entry<String, Long>> getTopAdditifs(@RequestParam int limit) {
        return produitRepository.findAll().stream()
                .flatMap(p -> p.getAdditifs().stream())
                .collect(Collectors.groupingBy(a -> a.getNom(), Collectors.counting()))
                .entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}

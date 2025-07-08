package com.etl.off.repository;

import com.etl.off.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Optional<Produit> findByNom(String nom);

    List<Produit> findByMarqueNomIgnoreCaseOrderByIdAsc(String brand);

    List<Produit> findByCategorieNomIgnoreCaseOrderByIdAsc(String category);

    List<Produit> findByMarqueNomIgnoreCaseAndCategorieNomIgnoreCaseOrderByIdAsc(String brand, String category);
}

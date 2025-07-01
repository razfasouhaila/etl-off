package com.etl.off.repository;

import com.etl.off.model.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    Optional<Allergen> findByNom(String nom);
}

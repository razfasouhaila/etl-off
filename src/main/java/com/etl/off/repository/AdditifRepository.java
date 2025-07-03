package com.etl.off.repository;

import com.etl.off.model.Additif;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdditifRepository extends JpaRepository<Additif, Long> {
    Optional<Additif> findByNom(String nom);
}

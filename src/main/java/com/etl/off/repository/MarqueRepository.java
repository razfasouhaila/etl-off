package com.etl.off.repository;

import com.etl.off.model.Marque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarqueRepository extends JpaRepository<Marque, Long> {
    Optional<Marque> findByNom(String nom);
}

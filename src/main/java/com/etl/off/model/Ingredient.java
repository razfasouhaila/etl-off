package com.etl.off.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@Entity
@AllArgsConstructor
@Getter
@Setter
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2048, unique = true, nullable = false)
    private String nom;

    public Ingredient(String nom) {
        this.nom = nom;
    }
}

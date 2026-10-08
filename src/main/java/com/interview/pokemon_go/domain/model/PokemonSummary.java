package com.interview.pokemon_go.domain.model;

import java.util.List;
import java.util.Objects;

public record PokemonSummary(
        int id,
        String name,
        String spriteUrl,
        String category,
        Weight weight,
        List<Ability> abilities) {

    public PokemonSummary {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(weight, "weight must not be null");
        abilities = List.copyOf(Objects.requireNonNull(abilities, "abilities must not be null"));
    }
}

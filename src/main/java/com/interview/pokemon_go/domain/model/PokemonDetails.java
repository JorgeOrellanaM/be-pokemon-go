package com.interview.pokemon_go.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Full view of one Pokemon (US02). {@code description} is empty, never null, when no text exists.
 */
public record PokemonDetails(
        int id,
        String name,
        String imageUrl,
        String category,
        List<String> types,
        List<BaseStat> stats,
        String description,
        EvolutionStage evolutionChain) {

    public PokemonDetails {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        types = List.copyOf(Objects.requireNonNull(types, "types must not be null"));
        stats = List.copyOf(Objects.requireNonNull(stats, "stats must not be null"));
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(evolutionChain, "evolutionChain must not be null");
    }
}

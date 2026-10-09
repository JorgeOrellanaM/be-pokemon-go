package com.interview.pokemon_go.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * One node of an evolution chain. It is a tree, not a list: a Pokemon can evolve into several
 * others (e.g. Eevee), so each stage holds all of its next stages.
 */
public record EvolutionStage(int id, String name, List<EvolutionStage> evolvesTo) {

    public EvolutionStage {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        evolvesTo = List.copyOf(Objects.requireNonNull(evolvesTo, "evolvesTo must not be null"));
    }
}

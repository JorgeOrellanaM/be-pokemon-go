package com.interview.pokemon_go.domain.model;

import java.util.Objects;

public record Ability(String name, boolean hidden) {

    public Ability {
        Objects.requireNonNull(name, "ability name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("ability name must not be blank");
        }
    }
}

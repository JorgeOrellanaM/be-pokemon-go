package com.interview.pokemon_go.domain.model;

import java.util.Objects;

/**
 * A core statistic (hp, attack, defense, …). Base stats are stored in a single byte by the games,
 * so a value outside 0–255 means corrupt data.
 */
public record BaseStat(String name, int value) {

    public static final int MAX_VALUE = 255;

    public BaseStat {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (value < 0 || value > MAX_VALUE) {
            throw new IllegalArgumentException("value must be between 0 and " + MAX_VALUE);
        }
    }
}

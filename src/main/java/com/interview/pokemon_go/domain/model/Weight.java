package com.interview.pokemon_go.domain.model;

/**
 * Pokemon weight expressed in hectograms, the unit used by PokeAPI.
 */
public record Weight(double hectograms) {

    public Weight {
        if (hectograms < 0) {
            throw new IllegalArgumentException("weight must not be negative");
        }
    }

    public double kilograms() {
        return hectograms / 10.0;
    }
}

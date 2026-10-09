package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.domain.exception.DomainValidationException;

final class PokedexIds {

    private static final String INVALID_ID = "id must be a positive whole number";

    private PokedexIds() {
    }

    /**
     * Pokédex numbers start at 1, so a non-positive id is rejected as invalid input before any lookup;
     * an id that is valid but unknown is a not-found.
     */
    static void requireValid(int id) {
        if (id <= 0) {
            throw new DomainValidationException(INVALID_ID);
        }
    }
}

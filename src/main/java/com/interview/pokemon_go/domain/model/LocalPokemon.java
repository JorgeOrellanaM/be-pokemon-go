package com.interview.pokemon_go.domain.model;

import java.util.Objects;

/**
 * A Pokemon replicated into the local store (US03): the data copied from PokeAPI plus the
 * {@link PokemonCustomization} this service owns and lets clients edit (US04).
 */
public record LocalPokemon(PokemonSummary pokemon, PokemonCustomization customization) {

    public LocalPokemon {
        Objects.requireNonNull(pokemon, "pokemon must not be null");
        Objects.requireNonNull(customization, "customization must not be null");
    }

    /**
     * A freshly synced Pokemon carries only PokeAPI data; it is customized later.
     */
    public static LocalPokemon replicaOf(PokemonSummary pokemon) {
        return new LocalPokemon(pokemon, PokemonCustomization.NONE);
    }

    public LocalPokemon customizedWith(PokemonCustomization newCustomization) {
        return new LocalPokemon(pokemon, newCustomization);
    }

    public int id() {
        return pokemon.id();
    }
}

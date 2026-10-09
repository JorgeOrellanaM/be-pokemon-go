package com.interview.pokemon_go.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Subset of PokeAPI's {@code /pokemon/{id}} response used by this service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record PokeApiPokemonDTO(
        int id,
        String name,
        Sprites sprites,
        List<Stat> stats,
        List<Type> types,
        NamedResource species) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Sprites(@JsonProperty("front_default") String frontDefault, OtherSprites other) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OtherSprites(@JsonProperty("official-artwork") Artwork officialArtwork) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Artwork(@JsonProperty("front_default") String frontDefault) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Stat(@JsonProperty("base_stat") int baseStat, NamedResource stat) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Type(int slot, NamedResource type) {
    }
}

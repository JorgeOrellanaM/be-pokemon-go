package com.interview.pokemon_go.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Subset of PokeAPI's {@code /pokemon-species/{id}} response used by this service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record PokeApiSpeciesDTO(
        @JsonProperty("flavor_text_entries") List<FlavorText> flavorTextEntries,
        List<Genus> genera,
        @JsonProperty("evolution_chain") Resource evolutionChain) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FlavorText(@JsonProperty("flavor_text") String flavorText, NamedResource language) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Genus(String genus, NamedResource language) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Resource(String url) {
    }
}

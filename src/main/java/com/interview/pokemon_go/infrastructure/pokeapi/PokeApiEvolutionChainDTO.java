package com.interview.pokemon_go.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Subset of PokeAPI's {@code /evolution-chain/{id}} response used by this service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record PokeApiEvolutionChainDTO(Link chain) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Link(NamedResource species, @JsonProperty("evolves_to") List<Link> evolvesTo) {
    }
}

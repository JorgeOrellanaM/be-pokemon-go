package com.interview.pokemon_go.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Subset of PokeAPI's {@code /pokemon?offset=…&limit=…} response: the total and one page of links.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record PokeApiPokemonPageDTO(int count, List<NamedResource> results) {
}

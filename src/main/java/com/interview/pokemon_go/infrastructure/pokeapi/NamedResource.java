package com.interview.pokemon_go.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PokeAPI's {@code {"name": …, "url": …}} reference to another resource.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record NamedResource(String name, String url) {
}

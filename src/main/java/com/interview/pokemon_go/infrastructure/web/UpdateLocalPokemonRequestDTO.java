package com.interview.pokemon_go.infrastructure.web;

import java.util.List;

/**
 * Body of {@code PUT /api/v1/local-pokemon/{id}}: the full set of custom fields. A missing field is
 * {@code null} and clears the stored value. The rules (lengths, tag limits) are checked by the domain
 * {@link com.interview.pokemon_go.domain.model.PokemonCustomization}, not here.
 */
public record UpdateLocalPokemonRequestDTO(String localizedName, String region, List<String> tags) {
}

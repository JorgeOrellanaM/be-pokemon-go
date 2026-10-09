package com.interview.pokemon_go.infrastructure.web;

import java.util.List;

/**
 * A synced Pokemon: the PokeAPI data plus the fields this service owns ({@code localizedName},
 * {@code region}, {@code tags}). Unset custom texts are {@code null}; tags are an empty list.
 */
public record LocalPokemonResponseDTO(
        int id,
        String name,
        String spriteUrl,
        String category,
        double weightKg,
        List<AbilityResponseDTO> abilities,
        String localizedName,
        String region,
        List<String> tags) {
}

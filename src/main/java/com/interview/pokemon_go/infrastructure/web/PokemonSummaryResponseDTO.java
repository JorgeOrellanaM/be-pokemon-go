package com.interview.pokemon_go.infrastructure.web;

import java.util.List;

public record PokemonSummaryResponseDTO(
        int id,
        String name,
        String spriteUrl,
        String category,
        double weightKg,
        List<AbilityResponseDTO> abilities) {
}

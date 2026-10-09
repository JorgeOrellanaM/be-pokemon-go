package com.interview.pokemon_go.infrastructure.web;

import java.util.List;

public record PokemonDetailsResponseDTO(
        int id,
        String name,
        String imageUrl,
        String category,
        List<String> types,
        List<BaseStatResponseDTO> stats,
        String description,
        EvolutionStageResponseDTO evolutionChain) {
}

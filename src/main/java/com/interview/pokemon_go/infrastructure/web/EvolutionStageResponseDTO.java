package com.interview.pokemon_go.infrastructure.web;

import java.util.List;

public record EvolutionStageResponseDTO(int id, String name, List<EvolutionStageResponseDTO> evolvesTo) {
}

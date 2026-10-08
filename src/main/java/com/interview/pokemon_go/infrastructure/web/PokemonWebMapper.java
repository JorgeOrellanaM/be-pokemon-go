package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;

final class PokemonWebMapper {

    private PokemonWebMapper() {
    }

    static PokemonSummaryResponseDTO toResponse(PokemonSummary summary) {
        return new PokemonSummaryResponseDTO(
                summary.id(),
                summary.name(),
                summary.spriteUrl(),
                summary.category(),
                summary.weight().kilograms(),
                summary.abilities().stream()
                        .map(ability -> new AbilityResponseDTO(ability.name(), ability.hidden()))
                        .toList());
    }

    static PageResponseDTO<PokemonSummaryResponseDTO> toPageResponse(PageResult<PokemonSummary> page) {
        return new PageResponseDTO<>(
                page.items().stream().map(PokemonWebMapper::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}

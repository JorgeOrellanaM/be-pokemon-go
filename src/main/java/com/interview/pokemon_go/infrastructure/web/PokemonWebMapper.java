package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.domain.model.EvolutionStage;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonDetails;
import com.interview.pokemon_go.domain.model.PokemonSummary;

final class PokemonWebMapper {

    private PokemonWebMapper() {
    }

    /**
     * Weight is exposed in kilograms for API clients; the domain keeps hectograms, the PokeAPI unit.
     */
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

    static PokemonDetailsResponseDTO toDetailsResponse(PokemonDetails details) {
        return new PokemonDetailsResponseDTO(
                details.id(),
                details.name(),
                details.imageUrl(),
                details.category(),
                details.types(),
                details.stats().stream()
                        .map(stat -> new BaseStatResponseDTO(stat.name(), stat.value()))
                        .toList(),
                details.description(),
                toStageResponse(details.evolutionChain()));
    }

    private static EvolutionStageResponseDTO toStageResponse(EvolutionStage stage) {
        return new EvolutionStageResponseDTO(
                stage.id(),
                stage.name(),
                stage.evolvesTo().stream().map(PokemonWebMapper::toStageResponse).toList());
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

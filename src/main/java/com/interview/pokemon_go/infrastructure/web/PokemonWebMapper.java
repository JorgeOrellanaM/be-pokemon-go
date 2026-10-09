package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.domain.model.EvolutionStage;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonCustomization;
import com.interview.pokemon_go.domain.model.PokemonDetails;
import com.interview.pokemon_go.domain.model.PokemonSummary;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
                toAbilityResponses(summary));
    }

    /**
     * Same units as {@link #toResponse}: weight in kilograms.
     */
    static LocalPokemonResponseDTO toLocalResponse(LocalPokemon local) {
        PokemonSummary pokemon = local.pokemon();
        return new LocalPokemonResponseDTO(
                pokemon.id(),
                pokemon.name(),
                pokemon.spriteUrl(),
                pokemon.category(),
                pokemon.weight().kilograms(),
                toAbilityResponses(pokemon),
                local.customization().localizedName(),
                local.customization().region(),
                local.customization().tags());
    }

    /**
     * PUT replaces every custom field, so a missing tag list means "no tags". Building the
     * {@link PokemonCustomization} validates the request and fails with a 400 listing each invalid field.
     */
    static PokemonCustomization toCustomization(UpdateLocalPokemonRequestDTO request) {
        return new PokemonCustomization(request.localizedName(), request.region(),
                Optional.ofNullable(request.tags()).orElse(List.of()));
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
        return toPageResponse(page, PokemonWebMapper::toResponse);
    }

    static PageResponseDTO<LocalPokemonResponseDTO> toLocalPageResponse(PageResult<LocalPokemon> page) {
        return toPageResponse(page, PokemonWebMapper::toLocalResponse);
    }

    private static <T, R> PageResponseDTO<R> toPageResponse(PageResult<T> page, Function<T, R> toItem) {
        return new PageResponseDTO<>(
                page.items().stream().map(toItem).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }

    private static List<AbilityResponseDTO> toAbilityResponses(PokemonSummary summary) {
        return summary.abilities().stream()
                .map(ability -> new AbilityResponseDTO(ability.name(), ability.hidden()))
                .toList();
    }
}

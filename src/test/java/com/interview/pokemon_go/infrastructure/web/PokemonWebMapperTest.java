package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.BaseStat;
import com.interview.pokemon_go.domain.model.EvolutionStage;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonDetails;
import com.interview.pokemon_go.domain.model.PokemonCustomization;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonWebMapperTest {

    private static final PokemonSummary PIKACHU = new PokemonSummary(25, "pikachu",
            "https://example.org/25.png", "Mouse Pokémon", new Weight(60),
            List.of(new Ability("static", false), new Ability("lightning-rod", true)));

    @Test
    void mapsSummaryWithWeightInKilograms() {
        PokemonSummaryResponseDTO dto = PokemonWebMapper.toResponse(PIKACHU);

        assertThat(dto.id()).isEqualTo(25);
        assertThat(dto.name()).isEqualTo("pikachu");
        assertThat(dto.spriteUrl()).isEqualTo("https://example.org/25.png");
        assertThat(dto.category()).isEqualTo("Mouse Pokémon");
        assertThat(dto.weightKg()).isEqualTo(6.0);
        assertThat(dto.abilities()).containsExactly(
                new AbilityResponseDTO("static", false), new AbilityResponseDTO("lightning-rod", true));
    }

    @Test
    void mapsDetailsWithNestedEvolutionChain() {
        PokemonDetails details = new PokemonDetails(25, "pikachu", "https://example.org/25.png",
                "Mouse Pokémon", List.of("electric"), List.of(new BaseStat("hp", 35)), "Electric mouse.",
                new EvolutionStage(172, "pichu", List.of(new EvolutionStage(25, "pikachu",
                        List.of(new EvolutionStage(26, "raichu", List.of()))))));

        PokemonDetailsResponseDTO dto = PokemonWebMapper.toDetailsResponse(details);

        assertThat(dto.id()).isEqualTo(25);
        assertThat(dto.name()).isEqualTo("pikachu");
        assertThat(dto.imageUrl()).isEqualTo("https://example.org/25.png");
        assertThat(dto.category()).isEqualTo("Mouse Pokémon");
        assertThat(dto.types()).containsExactly("electric");
        assertThat(dto.stats()).containsExactly(new BaseStatResponseDTO("hp", 35));
        assertThat(dto.description()).isEqualTo("Electric mouse.");
        assertThat(dto.evolutionChain()).isEqualTo(new EvolutionStageResponseDTO(172, "pichu",
                List.of(new EvolutionStageResponseDTO(25, "pikachu",
                        List.of(new EvolutionStageResponseDTO(26, "raichu", List.of()))))));
    }

    @Test
    void mapsPageWithTotalPages() {
        PageResult<PokemonSummary> page = new PageResult<>(List.of(PIKACHU), 2, 5, 11);

        PageResponseDTO<PokemonSummaryResponseDTO> dto = PokemonWebMapper.toPageResponse(page);

        assertThat(dto.items()).extracting(PokemonSummaryResponseDTO::name).containsExactly("pikachu");
        assertThat(dto.page()).isEqualTo(2);
        assertThat(dto.size()).isEqualTo(5);
        assertThat(dto.totalElements()).isEqualTo(11);
        assertThat(dto.totalPages()).isEqualTo(3);
    }

    @Test
    void mapsLocalPokemonWithItsCustomFields() {
        LocalPokemon local = new LocalPokemon(PIKACHU, new PokemonCustomization("Pikachu (ES)", "Kanto", List.of("starter")));

        LocalPokemonResponseDTO dto = PokemonWebMapper.toLocalResponse(local);

        assertThat(dto.id()).isEqualTo(25);
        assertThat(dto.name()).isEqualTo("pikachu");
        assertThat(dto.spriteUrl()).isEqualTo("https://example.org/25.png");
        assertThat(dto.category()).isEqualTo("Mouse Pokémon");
        assertThat(dto.weightKg()).isEqualTo(6.0);
        assertThat(dto.abilities()).containsExactly(
                new AbilityResponseDTO("static", false), new AbilityResponseDTO("lightning-rod", true));
        assertThat(dto.localizedName()).isEqualTo("Pikachu (ES)");
        assertThat(dto.region()).isEqualTo("Kanto");
        assertThat(dto.tags()).containsExactly("starter");
    }

    @Test
    void mapsAnUpdateRequestToACustomization() {
        PokemonCustomization customization = PokemonWebMapper.toCustomization(
                new UpdateLocalPokemonRequestDTO("Pikachu (ES)", "Kanto", List.of("starter")));

        assertThat(customization).isEqualTo(new PokemonCustomization("Pikachu (ES)", "Kanto", List.of("starter")));
    }

    @Test
    void mapsAMissingTagListToNoTags() {
        assertThat(PokemonWebMapper.toCustomization(new UpdateLocalPokemonRequestDTO(null, null, null)))
                .isEqualTo(PokemonCustomization.NONE);
    }

    @Test
    void mapsPageOfLocalPokemon() {
        PageResult<LocalPokemon> page = new PageResult<>(List.of(LocalPokemon.replicaOf(PIKACHU)), 0, 5, 11);

        PageResponseDTO<LocalPokemonResponseDTO> dto = PokemonWebMapper.toLocalPageResponse(page);

        assertThat(dto.items()).extracting(LocalPokemonResponseDTO::id).containsExactly(25);
        assertThat(dto.totalElements()).isEqualTo(11);
        assertThat(dto.totalPages()).isEqualTo(3);
    }
}

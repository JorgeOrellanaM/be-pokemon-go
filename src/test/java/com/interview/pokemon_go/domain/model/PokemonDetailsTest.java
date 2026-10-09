package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PokemonDetailsTest {

    private static final EvolutionStage CHAIN = new EvolutionStage(25, "pikachu", List.of());
    private static final List<BaseStat> STATS = List.of(new BaseStat("hp", 35));

    @Test
    void copiesListsDefensively() {
        List<String> types = new ArrayList<>(List.of("electric"));
        List<BaseStat> stats = new ArrayList<>(STATS);
        PokemonDetails details = new PokemonDetails(25, "pikachu", "https://example.org/25.png",
                "Mouse Pokémon", types, stats, "Electric mouse.", CHAIN);

        types.clear();
        stats.clear();

        assertThat(details.types()).containsExactly("electric");
        assertThat(details.stats()).hasSize(1);
    }

    @Test
    void acceptsEmptyDescription() {
        assertThat(pikachuWithDescription("").description()).isEmpty();
    }

    @Test
    void rejectsInvalidIdAndName() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PokemonDetails(0, "pikachu", null, null,
                List.of(), STATS, "", CHAIN));
        assertThatIllegalArgumentException().isThrownBy(() -> new PokemonDetails(25, " ", null, null,
                List.of(), STATS, "", CHAIN));
    }

    @Test
    void rejectsMissingRequiredComponents() {
        assertThatNullPointerException().isThrownBy(() -> pikachuWithDescription(null));
        assertThatNullPointerException().isThrownBy(() -> new PokemonDetails(25, "pikachu", null, null,
                null, STATS, "", CHAIN));
        assertThatNullPointerException().isThrownBy(() -> new PokemonDetails(25, "pikachu", null, null,
                List.of(), null, "", CHAIN));
        assertThatNullPointerException().isThrownBy(() -> new PokemonDetails(25, "pikachu", null, null,
                List.of(), STATS, "", null));
    }

    private static PokemonDetails pikachuWithDescription(String description) {
        return new PokemonDetails(25, "pikachu", "https://example.org/25.png", "Mouse Pokémon",
                List.of("electric"), STATS, description, CHAIN);
    }
}

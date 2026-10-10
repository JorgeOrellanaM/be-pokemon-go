package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PokemonSummaryTest {

    private static final Weight WEIGHT = new Weight(60);
    private static final List<Ability> ABILITIES = List.of(new Ability("static", false));

    @Test
    void keepsValidValues() {
        PokemonSummary pikachu = new PokemonSummary(25, "pikachu", "https://example.org/25.png", "Mouse Pokémon",
                WEIGHT, ABILITIES);

        assertThat(pikachu.id()).isEqualTo(25);
        assertThat(pikachu.name()).isEqualTo("pikachu");
        assertThat(pikachu.weight()).isEqualTo(WEIGHT);
        assertThat(pikachu.abilities()).containsExactlyElementsOf(ABILITIES);
    }

    /**
     * PokeAPI has Pokemon without a sprite or a category, so those two are optional.
     */
    @Test
    void allowsAMissingSpriteAndCategory() {
        PokemonSummary summary = new PokemonSummary(25, "pikachu", null, null, WEIGHT, List.of());

        assertThat(summary.spriteUrl()).isNull();
        assertThat(summary.category()).isNull();
    }

    @Test
    void rejectsANonPositiveId() {
        assertThatIllegalArgumentException().isThrownBy(() -> summary(0, "pikachu"));
        assertThatIllegalArgumentException().isThrownBy(() -> summary(-25, "pikachu"));
    }

    @Test
    void requiresAName() {
        assertThatNullPointerException().isThrownBy(() -> summary(25, null));
        assertThatIllegalArgumentException().isThrownBy(() -> summary(25, " "));
    }

    @Test
    void requiresAWeightAndAbilities() {
        assertThatNullPointerException()
                .isThrownBy(() -> new PokemonSummary(25, "pikachu", null, null, null, ABILITIES));
        assertThatNullPointerException()
                .isThrownBy(() -> new PokemonSummary(25, "pikachu", null, null, WEIGHT, null));
    }

    @Test
    void keepsADefensiveCopyOfTheAbilities() {
        List<Ability> abilities = new ArrayList<>(ABILITIES);
        PokemonSummary summary = new PokemonSummary(25, "pikachu", null, null, WEIGHT, abilities);

        abilities.add(new Ability("lightning-rod", true));

        assertThat(summary.abilities()).containsExactlyElementsOf(ABILITIES);
        assertThat(summary.abilities()).isUnmodifiable();
    }

    private static PokemonSummary summary(int id, String name) {
        return new PokemonSummary(id, name, null, null, WEIGHT, ABILITIES);
    }
}

package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class LocalPokemonTest {

    private static final PokemonSummary PIKACHU = new PokemonSummary(25, "pikachu",
            "https://example.org/25.png", "Mouse Pokémon", new Weight(60),
            List.of(new Ability("static", false), new Ability("lightning-rod", true)));

    @Test
    void replicaHasThePokeApiDataAndNoCustomFields() {
        LocalPokemon replica = LocalPokemon.replicaOf(PIKACHU);

        assertThat(replica.pokemon()).isEqualTo(PIKACHU);
        assertThat(replica.localizedName()).isNull();
        assertThat(replica.region()).isNull();
        assertThat(replica.tags()).isEmpty();
    }

    @Test
    void exposesTheIdOfThePokemon() {
        assertThat(LocalPokemon.replicaOf(PIKACHU).id()).isEqualTo(25);
    }

    @Test
    void keepsCustomFields() {
        LocalPokemon local = new LocalPokemon(PIKACHU, "Pikachu (ES)", "Kanto", List.of("starter", "electric"));

        assertThat(local.localizedName()).isEqualTo("Pikachu (ES)");
        assertThat(local.region()).isEqualTo("Kanto");
        assertThat(local.tags()).containsExactly("starter", "electric");
    }

    @Test
    void copiesTagsDefensively() {
        List<String> tags = new ArrayList<>(List.of("starter"));
        LocalPokemon local = new LocalPokemon(PIKACHU, null, null, tags);

        tags.add("legendary");

        assertThat(local.tags()).containsExactly("starter");
    }

    @Test
    void requiresThePokemonAndTags() {
        assertThatNullPointerException().isThrownBy(() -> new LocalPokemon(null, null, null, List.of()));
        assertThatNullPointerException().isThrownBy(() -> new LocalPokemon(PIKACHU, null, null, null));
    }

    @Test
    void rejectsBlankCustomTexts() {
        assertThatIllegalArgumentException().isThrownBy(() -> new LocalPokemon(PIKACHU, " ", null, List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new LocalPokemon(PIKACHU, null, "", List.of()));
    }

    @Test
    void rejectsBlankOrDuplicateTags() {
        assertThatIllegalArgumentException().isThrownBy(() -> new LocalPokemon(PIKACHU, null, null, List.of(" ")));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LocalPokemon(PIKACHU, null, null, List.of("starter", "starter")));
    }
}

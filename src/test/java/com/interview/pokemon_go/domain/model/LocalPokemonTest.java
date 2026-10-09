package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class LocalPokemonTest {

    private static final PokemonSummary PIKACHU = new PokemonSummary(25, "pikachu",
            "https://example.org/25.png", "Mouse Pokémon", new Weight(60),
            List.of(new Ability("static", false), new Ability("lightning-rod", true)));

    private static final PokemonCustomization CUSTOMIZATION =
            new PokemonCustomization("Pikachu (ES)", "Kanto", List.of("starter", "electric"));

    @Test
    void replicaHasThePokeApiDataAndNoCustomization() {
        LocalPokemon replica = LocalPokemon.replicaOf(PIKACHU);

        assertThat(replica.pokemon()).isEqualTo(PIKACHU);
        assertThat(replica.customization()).isEqualTo(PokemonCustomization.NONE);
    }

    @Test
    void exposesTheIdOfThePokemon() {
        assertThat(LocalPokemon.replicaOf(PIKACHU).id()).isEqualTo(25);
    }

    @Test
    void customizingReplacesOnlyTheCustomFields() {
        LocalPokemon customized = LocalPokemon.replicaOf(PIKACHU).customizedWith(CUSTOMIZATION);

        assertThat(customized.pokemon()).isEqualTo(PIKACHU);
        assertThat(customized.customization()).isEqualTo(CUSTOMIZATION);
    }

    @Test
    void requiresThePokemonAndTheCustomization() {
        assertThatNullPointerException().isThrownBy(() -> new LocalPokemon(null, PokemonCustomization.NONE));
        assertThatNullPointerException().isThrownBy(() -> new LocalPokemon(PIKACHU, null));
    }
}

package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.out.InMemoryPokemonDetailsAdapter;
import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.BaseStat;
import com.interview.pokemon_go.domain.model.EvolutionStage;
import com.interview.pokemon_go.domain.model.PokemonDetails;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetPokemonDetailsServiceTest {

    private static final PokemonDetails PIKACHU = new PokemonDetails(25, "pikachu",
            "https://example.org/25.png", "Mouse Pokémon", List.of("electric"),
            List.of(new BaseStat("hp", 35)), "Electric mouse.",
            new EvolutionStage(172, "pichu", List.of(new EvolutionStage(25, "pikachu", List.of()))));

    private final InMemoryPokemonDetailsAdapter details = new InMemoryPokemonDetailsAdapter(List.of(PIKACHU));
    private final GetPokemonDetailsService service = new GetPokemonDetailsService(details);

    @Test
    void returnsTheDetailsOfAnExistingPokemon() {
        assertThat(service.getById(25)).isEqualTo(PIKACHU);
    }

    @Test
    void throwsNotFoundForUnknownPokemon() {
        assertThatThrownBy(() -> service.getById(99999))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessage("Pokemon with id 99999 was not found");
    }

    @Test
    void rejectsNonPositiveIdWithoutCallingThePort() {
        assertThatThrownBy(() -> service.getById(0))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("id must be a positive whole number");
        assertThatThrownBy(() -> service.getById(-5)).isInstanceOf(DomainValidationException.class);
        assertThat(details.lookups()).isZero();
    }

    @Test
    void requiresAPort() {
        assertThatNullPointerException().isThrownBy(() -> new GetPokemonDetailsService(null));
    }
}

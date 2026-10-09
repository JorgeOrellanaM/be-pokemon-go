package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.out.InMemoryLocalPokemonAdapter;
import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetLocalPokemonServiceTest {

    private static final LocalPokemon PIKACHU = new LocalPokemon(new PokemonSummary(25, "pikachu",
            "https://example.org/25.png", "Mouse Pokémon", new Weight(60), List.of(new Ability("static", false))),
            "Pikachu (ES)", "Kanto", List.of("starter"));

    private final InMemoryLocalPokemonAdapter local = new InMemoryLocalPokemonAdapter();
    private final GetLocalPokemonService service = new GetLocalPokemonService(local);

    @Test
    void returnsAStoredPokemon() {
        local.save(PIKACHU);

        assertThat(service.getById(25)).isEqualTo(PIKACHU);
    }

    @Test
    void throwsNotFoundForAPokemonThatWasNotSynced() {
        assertThatThrownBy(() -> service.getById(4242))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessage("Pokemon with id 4242 was not found");
    }

    @Test
    void rejectsNonPositiveId() {
        assertThatThrownBy(() -> service.getById(0))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("id must be a positive whole number");
    }

    @Test
    void requiresAPort() {
        assertThatNullPointerException().isThrownBy(() -> new GetLocalPokemonService(null));
    }
}

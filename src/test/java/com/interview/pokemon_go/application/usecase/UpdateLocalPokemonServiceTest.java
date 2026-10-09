package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.out.FakePokemonCatalogAdapter;
import com.interview.pokemon_go.application.port.out.InMemoryLocalPokemonAdapter;
import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PokemonCustomization;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateLocalPokemonServiceTest {

    private static final PokemonCustomization CUSTOMIZATION =
            new PokemonCustomization("Pikachu (ES)", "Kanto", List.of("starter"));

    private final InMemoryLocalPokemonAdapter local = new InMemoryLocalPokemonAdapter();
    private final UpdateLocalPokemonService service = new UpdateLocalPokemonService(local);

    @Test
    void replacesTheCustomizationOfAStoredPokemon() {
        LocalPokemon pikachu = LocalPokemon.replicaOf(new FakePokemonCatalogAdapter().findById(25).orElseThrow());
        local.save(pikachu);

        LocalPokemon updated = service.update(25, CUSTOMIZATION);

        assertThat(updated).isEqualTo(pikachu.customizedWith(CUSTOMIZATION));
        assertThat(local.findById(25)).contains(updated);
    }

    @Test
    void throwsNotFoundForAPokemonThatWasNotSynced() {
        assertThatThrownBy(() -> service.update(4242, CUSTOMIZATION))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessage("Pokemon with id 4242 was not found");
    }

    @Test
    void rejectsNonPositiveId() {
        assertThatThrownBy(() -> service.update(0, CUSTOMIZATION))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("id must be a positive whole number");
    }

    @Test
    void requiresACustomizationAndAPort() {
        assertThatNullPointerException().isThrownBy(() -> service.update(25, null));
        assertThatNullPointerException().isThrownBy(() -> new UpdateLocalPokemonService(null));
    }
}

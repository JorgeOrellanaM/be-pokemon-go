package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.out.FakePokemonCatalogAdapter;
import com.interview.pokemon_go.application.port.out.InMemoryLocalPokemonAdapter;
import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.PokemonAlreadySyncedException;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncPokemonServiceTest {

    private final FakePokemonCatalogAdapter catalog = new FakePokemonCatalogAdapter();
    private final InMemoryLocalPokemonAdapter local = new InMemoryLocalPokemonAdapter();
    private final SyncPokemonService service = new SyncPokemonService(catalog, local);

    @Test
    void storesAReplicaOfThePokeApiDataWithoutCustomFields() {
        PokemonSummary pikachu = catalog.findById(25).orElseThrow();

        LocalPokemon synced = service.sync(25);

        assertThat(synced).isEqualTo(LocalPokemon.replicaOf(pikachu));
        assertThat(local.findById(25)).contains(synced);
    }

    @Test
    void rejectsAPokemonThatIsAlreadySyncedWithoutCallingTheCatalog() {
        service.sync(25);
        int lookupsAfterFirstSync = catalog.lookups();

        assertThatThrownBy(() -> service.sync(25))
                .isInstanceOf(PokemonAlreadySyncedException.class)
                .hasMessage("Pokemon with id 25 is already synced");
        assertThat(catalog.lookups()).isEqualTo(lookupsAfterFirstSync);
        assertThat(local.saves()).isEqualTo(1);
    }

    @Test
    void throwsNotFoundWhenThePokemonDoesNotExistInTheCatalog() {
        assertThatThrownBy(() -> service.sync(99999))
                .isInstanceOf(PokemonNotFoundException.class)
                .hasMessage("Pokemon with id 99999 was not found");
        assertThat(local.saves()).isZero();
    }

    @Test
    void rejectsNonPositiveIdWithoutCallingAnyPort() {
        assertThatThrownBy(() -> service.sync(0))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("id must be a positive whole number");
        assertThatThrownBy(() -> service.sync(-1)).isInstanceOf(DomainValidationException.class);
        assertThat(catalog.lookups()).isZero();
        assertThat(local.saves()).isZero();
    }

    @Test
    void requiresBothPorts() {
        assertThatNullPointerException().isThrownBy(() -> new SyncPokemonService(null, local));
        assertThatNullPointerException().isThrownBy(() -> new SyncPokemonService(catalog, null));
    }
}

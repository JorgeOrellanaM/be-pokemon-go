package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.exception.PokemonAlreadySyncedException;
import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonCustomization;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Behaviour every {@link LocalPokemonPort} implementation must share (LSP), so the in-memory fake used
 * in use-case tests cannot drift from the database adapter. {@code @Transactional} is declared here
 * for the same reason as in {@link PokemonCatalogPortContractTest}: database-backed runs roll back.
 */
@Transactional
public abstract class LocalPokemonPortContractTest {

    private LocalPokemonPort store;

    /**
     * Returns an implementation that holds no Pokemon.
     */
    protected abstract LocalPokemonPort emptyStore();

    @BeforeEach
    void createStore() {
        store = emptyStore();
    }

    @Test
    void returnsWhatWasSavedIncludingCustomFields() {
        LocalPokemon pikachu = new LocalPokemon(pokemon(25, "pikachu"),
                new PokemonCustomization("Pikachu (ES)", "Kanto", List.of("starter", "electric")));

        LocalPokemon saved = store.save(pikachu);

        assertThat(saved).isEqualTo(pikachu);
        assertThat(store.findById(25)).contains(pikachu);
    }

    @Test
    void keepsAReplicaWithoutCustomFields() {
        LocalPokemon replica = LocalPokemon.replicaOf(pokemon(1, "bulbasaur"));

        store.save(replica);

        assertThat(store.findById(1)).contains(replica);
    }

    @Test
    void reportsWhichPokemonExist() {
        store.save(LocalPokemon.replicaOf(pokemon(1, "bulbasaur")));

        assertThat(store.existsById(1)).isTrue();
        assertThat(store.existsById(2)).isFalse();
    }

    @Test
    void findsNothingForAnUnknownId() {
        assertThat(store.findById(4242)).isEmpty();
    }

    @Test
    void rejectsSavingTheSamePokemonTwice() {
        store.save(LocalPokemon.replicaOf(pokemon(1, "bulbasaur")));

        assertThatThrownBy(() -> store.save(LocalPokemon.replicaOf(pokemon(1, "bulbasaur"))))
                .isInstanceOf(PokemonAlreadySyncedException.class);
    }

    @Test
    void updatesOnlyTheCustomizationOfAStoredPokemon() {
        store.save(LocalPokemon.replicaOf(pokemon(25, "pikachu")));
        PokemonCustomization customization = new PokemonCustomization("Pikachu (ES)", "Kanto", List.of("starter"));

        LocalPokemon updated = store.updateCustomization(25, customization).orElseThrow();

        assertThat(updated).isEqualTo(LocalPokemon.replicaOf(pokemon(25, "pikachu")).customizedWith(customization));
        assertThat(store.findById(25)).contains(updated);
    }

    @Test
    void replacesTagsKeepingTheRequestedOrderAndAllowingTagsThatWereAlreadyThere() {
        store.save(new LocalPokemon(pokemon(25, "pikachu"),
                new PokemonCustomization("Pikachu", "Kanto", List.of("starter", "mascot"))));

        store.updateCustomization(25, new PokemonCustomization(null, null, List.of("electric", "starter")));

        assertThat(store.findById(25).orElseThrow().customization())
                .isEqualTo(new PokemonCustomization(null, null, List.of("electric", "starter")));
    }

    @Test
    void clearsTheCustomization() {
        store.save(new LocalPokemon(pokemon(25, "pikachu"),
                new PokemonCustomization("Pikachu", "Kanto", List.of("starter"))));

        store.updateCustomization(25, PokemonCustomization.NONE);

        assertThat(store.findById(25).orElseThrow().customization()).isEqualTo(PokemonCustomization.NONE);
    }

    @Test
    void updatesNothingForAnUnknownId() {
        assertThat(store.updateCustomization(4242, PokemonCustomization.NONE)).isEmpty();
        assertThat(store.existsById(4242)).isFalse();
    }

    @Test
    void returnsPagesOrderedByPokedexNumber() {
        List.of(pokemon(25, "pikachu"), pokemon(3, "venusaur"), pokemon(1, "bulbasaur"), pokemon(7, "squirtle"))
                .forEach(summary -> store.save(LocalPokemon.replicaOf(summary)));

        PageResult<LocalPokemon> result = store.findPage(new PageQuery(1, 2));

        assertThat(result.items()).extracting(LocalPokemon::id).containsExactly(7, 25);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(4);
    }

    @Test
    void returnsAnEmptyPageForAnEmptyStore() {
        PageResult<LocalPokemon> result = store.findPage(new PageQuery(0, 20));

        assertThat(result.items()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    private static PokemonSummary pokemon(int id, String name) {
        return new PokemonSummary(id, name, "https://example.org/%d.png".formatted(id), "Test Pokémon",
                new Weight(id * 10), List.of(new Ability("main-" + name, false), new Ability("hidden-" + name, true)));
    }
}

package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.interview.pokemon_go.infrastructure.pokeapi.PokeApiTestData.listEntry;
import static com.interview.pokemon_go.infrastructure.pokeapi.PokeApiTestData.pokemon;
import static com.interview.pokemon_go.infrastructure.pokeapi.PokeApiTestData.species;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Page assembly from PokeAPI, tested against {@link FakePokeApiClient}. Most tests run the per-Pokemon
 * calls on the calling thread so the order of requests is deterministic.
 */
class PokeApiPokemonCatalogAdapterTest {

    private final FakePokeApiClient pokeApi = new FakePokeApiClient();
    private final PokeApiPokemonCatalogAdapter adapter = new PokeApiPokemonCatalogAdapter(pokeApi, Runnable::run);

    @Test
    void buildsAPageFromTheListThePokemonAndTheirSpecies() {
        pokeApi.withPokemonList(List.of(listEntry(1, "bulbasaur"), listEntry(2, "ivysaur"), listEntry(3, "venusaur")))
                .withPokemon(2, pokemon(2, "ivysaur", 2))
                .withSpecies(2, species("Seed Pokémon", "", 1));

        PageResult<PokemonSummary> page = adapter.findPage(new PageQuery(1, 1));

        assertThat(page.items()).extracting(PokemonSummary::name).containsExactly("ivysaur");
        assertThat(page.items().getFirst().category()).isEqualTo("Seed Pokémon");
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(1);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(pokeApi.requests()).containsExactly("pokemon?offset=1&limit=1", "pokemon/2", "pokemon-species/2");
    }

    @Test
    void followsTheSpeciesLinkOfEachPokemon() {
        pokeApi.withPokemonList(List.of(listEntry(10034, "charizard-mega-x")))
                .withPokemon(10034, pokemon(10034, "charizard-mega-x", 6))
                .withSpecies(6, species("Flame Pokémon", "", 2));

        assertThat(adapter.findPage(new PageQuery(0, 1)).items().getFirst().category()).isEqualTo("Flame Pokémon");
        assertThat(pokeApi.requests()).contains("pokemon-species/6");
    }

    @Test
    void keepsTheListOrderWhenParallelCallsFinishOutOfOrder() {
        pokeApi.withPokemonList(List.of(listEntry(1, "bulbasaur"), listEntry(2, "ivysaur"), listEntry(3, "venusaur")))
                .withPokemon(1, pokemon(1, "bulbasaur", 1)).withSpecies(1, species("Seed Pokémon", "", 1))
                .withPokemon(2, pokemon(2, "ivysaur", 2)).withSpecies(2, species("Seed Pokémon", "", 1))
                .withPokemon(3, pokemon(3, "venusaur", 3)).withSpecies(3, species("Seed Pokémon", "", 1))
                .withPokemonDelay(1, Duration.ofMillis(200));

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            PokeApiPokemonCatalogAdapter parallel = new PokeApiPokemonCatalogAdapter(pokeApi, executor);

            assertThat(parallel.findPage(new PageQuery(0, 3)).items())
                    .extracting(PokemonSummary::name).containsExactly("bulbasaur", "ivysaur", "venusaur");
        }
    }

    @Test
    void reportsAFailedSpeciesCallAsUnavailableAndNotAsAWrappedException() {
        pokeApi.withPokemonList(List.of(listEntry(1, "bulbasaur")))
                .withPokemon(1, pokemon(1, "bulbasaur", 1));

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            PokeApiPokemonCatalogAdapter parallel = new PokeApiPokemonCatalogAdapter(pokeApi, executor);

            assertThatThrownBy(() -> parallel.findPage(new PageQuery(0, 1)))
                    .isExactlyInstanceOf(ExternalServiceUnavailableException.class);
        }
    }

    @Test
    void reportsAListedPokemonThatCannotBeReturnedAsUnavailable() {
        pokeApi.withPokemonList(List.of(listEntry(1, "bulbasaur")));

        assertThatThrownBy(() -> adapter.findPage(new PageQuery(0, 1)))
                .isExactlyInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessage("PokeAPI listed a Pokemon it cannot return");
    }

    @Test
    void makesNoPerPokemonCallsBeyondTheEnd() {
        pokeApi.withPokemonList(List.of(listEntry(1, "bulbasaur")));

        PageResult<PokemonSummary> page = adapter.findPage(new PageQuery(5, 10));

        assertThat(page.items()).isEmpty();
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(pokeApi.requests()).containsExactly("pokemon?offset=50&limit=10");
    }

    @Test
    void requiresAClientAndAnExecutor() {
        assertThatNullPointerException().isThrownBy(() -> new PokeApiPokemonCatalogAdapter(null, Runnable::run));
        assertThatNullPointerException().isThrownBy(() -> new PokeApiPokemonCatalogAdapter(pokeApi, null));
    }
}

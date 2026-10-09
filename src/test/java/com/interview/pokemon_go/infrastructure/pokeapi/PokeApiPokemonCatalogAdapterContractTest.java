package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.application.port.out.PokemonCatalogPort;
import com.interview.pokemon_go.application.port.out.PokemonCatalogPortContractTest;
import com.interview.pokemon_go.domain.model.PokemonSummary;

import java.util.Comparator;
import java.util.List;

import static com.interview.pokemon_go.infrastructure.pokeapi.PokeApiTestData.listEntry;
import static com.interview.pokemon_go.infrastructure.pokeapi.PokeApiTestData.pokemonOf;
import static com.interview.pokemon_go.infrastructure.pokeapi.PokeApiTestData.speciesOf;

/**
 * Runs the shared catalog contract against the PokeAPI adapter. PokeAPI lists Pokemon in Pokédex
 * order, so the fake list is sorted the same way.
 */
class PokeApiPokemonCatalogAdapterContractTest extends PokemonCatalogPortContractTest {

    @Override
    protected PokemonCatalogPort catalogWith(List<PokemonSummary> pokemon) {
        FakePokeApiClient pokeApi = new FakePokeApiClient().withPokemonList(pokemon.stream()
                .sorted(Comparator.comparingInt(PokemonSummary::id))
                .map(summary -> listEntry(summary.id(), summary.name()))
                .toList());
        pokemon.forEach(summary -> pokeApi
                .withPokemon(summary.id(), pokemonOf(summary))
                .withSpecies(summary.id(), speciesOf(summary)));
        return new PokeApiPokemonCatalogAdapter(pokeApi, Runnable::run);
    }
}

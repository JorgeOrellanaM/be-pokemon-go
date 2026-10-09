package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hand-written, in-memory {@link PokeApiClient} that follows the same contract as the HTTP client:
 * unknown Pokemon are empty, any other missing resource is an unavailable PokeAPI. Records every
 * request so tests can check which resources were asked for.
 */
class FakePokeApiClient implements PokeApiClient {

    private final Map<Integer, PokeApiPokemonDTO> pokemon = new HashMap<>();
    private final Map<Integer, PokeApiSpeciesDTO> species = new HashMap<>();
    private final Map<Integer, PokeApiEvolutionChainDTO> evolutionChains = new HashMap<>();
    private final List<String> requests = new ArrayList<>();

    FakePokeApiClient withPokemon(int id, PokeApiPokemonDTO dto) {
        pokemon.put(id, dto);
        return this;
    }

    FakePokeApiClient withSpecies(int id, PokeApiSpeciesDTO dto) {
        species.put(id, dto);
        return this;
    }

    FakePokeApiClient withEvolutionChain(int id, PokeApiEvolutionChainDTO dto) {
        evolutionChains.put(id, dto);
        return this;
    }

    List<String> requests() {
        return List.copyOf(requests);
    }

    @Override
    public Optional<PokeApiPokemonDTO> findPokemon(int id) {
        requests.add("pokemon/" + id);
        return Optional.ofNullable(pokemon.get(id));
    }

    @Override
    public PokeApiSpeciesDTO getSpecies(int id) {
        requests.add("pokemon-species/" + id);
        return required(species.get(id));
    }

    @Override
    public PokeApiEvolutionChainDTO getEvolutionChain(int id) {
        requests.add("evolution-chain/" + id);
        return required(evolutionChains.get(id));
    }

    private static <T> T required(T resource) {
        return Optional.ofNullable(resource).orElseThrow(() ->
                new ExternalServiceUnavailableException("PokeAPI is unavailable", new IllegalStateException()));
    }
}

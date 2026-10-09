package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hand-written, in-memory {@link PokeApiClient} that follows the same contract as the HTTP client:
 * unknown Pokemon are empty, any other missing resource is an unavailable PokeAPI. Records every
 * request so tests can check which resources were asked for. Safe to call from several threads once
 * it has been set up.
 */
class FakePokeApiClient implements PokeApiClient {

    private final List<NamedResource> pokemonList = new ArrayList<>();
    private final Map<Integer, PokeApiPokemonDTO> pokemon = new HashMap<>();
    private final Map<Integer, PokeApiSpeciesDTO> species = new HashMap<>();
    private final Map<Integer, PokeApiEvolutionChainDTO> evolutionChains = new HashMap<>();
    private final Map<Integer, Duration> pokemonDelays = new HashMap<>();
    private final List<String> requests = Collections.synchronizedList(new ArrayList<>());

    FakePokeApiClient withPokemonList(List<NamedResource> entries) {
        pokemonList.addAll(entries);
        return this;
    }

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

    /**
     * Makes {@link #findPokemon} answer slowly for one Pokemon, so parallel calls finish out of order.
     */
    FakePokeApiClient withPokemonDelay(int id, Duration delay) {
        pokemonDelays.put(id, delay);
        return this;
    }

    List<String> requests() {
        synchronized (requests) {
            return List.copyOf(requests);
        }
    }

    @Override
    public PokeApiPokemonPageDTO listPokemon(int offset, int limit) {
        requests.add("pokemon?offset=%d&limit=%d".formatted(offset, limit));
        int from = Math.min(offset, pokemonList.size());
        int to = Math.min(from + limit, pokemonList.size());
        return new PokeApiPokemonPageDTO(pokemonList.size(), List.copyOf(pokemonList.subList(from, to)));
    }

    @Override
    public Optional<PokeApiPokemonDTO> findPokemon(int id) {
        requests.add("pokemon/" + id);
        Optional.ofNullable(pokemonDelays.get(id)).ifPresent(FakePokeApiClient::sleep);
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

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

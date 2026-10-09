package com.interview.pokemon_go.infrastructure.pokeapi;

import java.util.Optional;

/**
 * The PokeAPI resources this service reads, independent of how they are fetched. The adapters depend
 * on this abstraction, so they know nothing about HTTP; {@link PokeApiHttpClient} is the only class
 * that does.
 * <p>
 * Contract: an unknown Pokemon is an empty result. Every other failure, including a missing species
 * or evolution chain, means PokeAPI is unavailable or inconsistent and is thrown as
 * {@link com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException}.
 */
interface PokeApiClient {

    PokeApiPokemonPageDTO listPokemon(int offset, int limit);

    Optional<PokeApiPokemonDTO> findPokemon(int id);

    PokeApiSpeciesDTO getSpecies(int id);

    PokeApiEvolutionChainDTO getEvolutionChain(int id);
}

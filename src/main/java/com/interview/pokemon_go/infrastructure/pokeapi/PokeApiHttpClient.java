package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.infrastructure.config.CacheConfig;
import com.interview.pokemon_go.infrastructure.config.PokeApiClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;
import java.util.Optional;

@Component
class PokeApiHttpClient implements PokeApiClient {

    private static final String UNAVAILABLE = "PokeAPI is unavailable";

    private final RestClient restClient;

    PokeApiHttpClient(@Qualifier(PokeApiClientConfig.POKEAPI_REST_CLIENT) RestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient, "restClient must not be null");
    }

    /**
     * The list is the entry point of the catalog and must always answer, so every failure is a 503.
     */
    @Override
    @Cacheable(cacheNames = CacheConfig.POKEAPI_POKEMON_PAGES, sync = true)
    public PokeApiPokemonPageDTO listPokemon(int offset, int limit) {
        return getRequired(PokeApiPokemonPageDTO.class, "/pokemon?offset={offset}&limit={limit}", offset, limit);
    }

    /**
     * A 404 here is the only "not found" PokeAPI can answer for a client's request: the id the
     * client asked for does not exist. That answer is cached too: it will not change within the TTL,
     * and repeated requests for an unknown id then never reach PokeAPI.
     */
    @Override
    @Cacheable(cacheNames = CacheConfig.POKEAPI_POKEMON, sync = true)
    public Optional<PokeApiPokemonDTO> findPokemon(int id) {
        try {
            return Optional.of(get(PokeApiPokemonDTO.class, "/pokemon/{id}", id));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.POKEAPI_SPECIES, sync = true)
    public PokeApiSpeciesDTO getSpecies(int id) {
        return getRequired(PokeApiSpeciesDTO.class, "/pokemon-species/{id}", id);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.POKEAPI_EVOLUTION_CHAINS, sync = true)
    public PokeApiEvolutionChainDTO getEvolutionChain(int id) {
        return getRequired(PokeApiEvolutionChainDTO.class, "/evolution-chain/{id}", id);
    }

    /**
     * Resources other than the requested Pokemon are reached through PokeAPI's own list or links, so
     * they must exist: any failure, a 404 included, is PokeAPI's fault and becomes a 503 rather than a
     * "not found" for the client.
     */
    private <T> T getRequired(Class<T> type, String path, Object... uriVariables) {
        try {
            return get(type, path, uriVariables);
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private <T> T get(Class<T> type, String path, Object... uriVariables) {
        return Optional.ofNullable(restClient.get().uri(path, uriVariables).retrieve().body(type))
                .orElseThrow(() -> new RestClientException("PokeAPI returned an empty body for " + path));
    }

    private static ExternalServiceUnavailableException unavailable(RestClientException cause) {
        return new ExternalServiceUnavailableException(UNAVAILABLE, cause);
    }
}

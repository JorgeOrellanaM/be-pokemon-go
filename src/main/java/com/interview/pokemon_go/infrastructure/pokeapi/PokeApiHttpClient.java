package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.infrastructure.config.PokeApiClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
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
     * A 404 here is the only "not found" PokeAPI can answer for a client's request: the id the
     * client asked for does not exist.
     */
    @Override
    public Optional<PokeApiPokemonDTO> findPokemon(int id) {
        try {
            return Optional.of(get("/pokemon/{id}", id, PokeApiPokemonDTO.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    @Override
    public PokeApiSpeciesDTO getSpecies(int id) {
        return getRequired("/pokemon-species/{id}", id, PokeApiSpeciesDTO.class);
    }

    @Override
    public PokeApiEvolutionChainDTO getEvolutionChain(int id) {
        return getRequired("/evolution-chain/{id}", id, PokeApiEvolutionChainDTO.class);
    }

    /**
     * Species and chains are reached through PokeAPI's own links, so they must exist: any failure,
     * a 404 included, is PokeAPI's fault and becomes a 503 rather than a "not found" for the client.
     */
    private <T> T getRequired(String path, int id, Class<T> type) {
        try {
            return get(path, id, type);
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private <T> T get(String path, int id, Class<T> type) {
        return Optional.ofNullable(restClient.get().uri(path, id).retrieve().body(type))
                .orElseThrow(() -> new RestClientException("PokeAPI returned an empty body for " + path));
    }

    private static ExternalServiceUnavailableException unavailable(RestClientException cause) {
        return new ExternalServiceUnavailableException(UNAVAILABLE, cause);
    }
}

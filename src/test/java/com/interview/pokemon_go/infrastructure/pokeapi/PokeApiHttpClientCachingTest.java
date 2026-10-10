package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.infrastructure.config.CacheConfig;
import com.interview.pokemon_go.infrastructure.config.PokeApiClientConfig;
import com.interview.pokemon_go.infrastructure.config.PokeApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Caching of PokeAPI responses through the real Spring cache proxy: each resource is fetched once,
 * failures are never cached so an outage is retried on the next request.
 */
@SpringJUnitConfig({CacheConfig.class, PokeApiHttpClientCachingTest.TestConfig.class})
class PokeApiHttpClientCachingTest {

    private static final String BASE_URL = "https://pokeapi.test/api/v2";

    @Autowired
    private PokeApiClient client;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        server.reset();
        cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
    }

    @Test
    void fetchesAPokemonOnce() {
        expectFixtureOnce("/pokemon/25", "pokemon-25.json");

        PokeApiPokemonDTO first = client.findPokemon(25).orElseThrow();
        PokeApiPokemonDTO second = client.findPokemon(25).orElseThrow();

        assertThat(second).isEqualTo(first);
        server.verify();
    }

    @Test
    void fetchesAPageOfTheListOnce() {
        expectFixtureOnce("/pokemon?offset=0&limit=2", "pokemon-list.json");

        PokeApiPokemonPageDTO first = client.listPokemon(0, 2);
        PokeApiPokemonPageDTO second = client.listPokemon(0, 2);

        assertThat(second).isEqualTo(first);
        server.verify();
    }

    @Test
    void fetchesASpeciesOnce() {
        expectFixtureOnce("/pokemon-species/25", "pokemon-species-25.json");

        PokeApiSpeciesDTO first = client.getSpecies(25);
        PokeApiSpeciesDTO second = client.getSpecies(25);

        assertThat(second).isEqualTo(first);
        server.verify();
    }

    @Test
    void fetchesAnEvolutionChainOnce() {
        expectFixtureOnce("/evolution-chain/10", "evolution-chain-10.json");

        PokeApiEvolutionChainDTO first = client.getEvolutionChain(10);
        PokeApiEvolutionChainDTO second = client.getEvolutionChain(10);

        assertThat(second).isEqualTo(first);
        server.verify();
    }

    @Test
    void cachesEachKeySeparately() {
        expectFixtureOnce("/pokemon/25", "pokemon-25.json");
        expectFixtureOnce("/pokemon/133", "pokemon-133.json");

        assertThat(client.findPokemon(25)).get().extracting(PokeApiPokemonDTO::name).isEqualTo("pikachu");
        assertThat(client.findPokemon(133)).get().extracting(PokeApiPokemonDTO::name).isEqualTo("eevee");
        assertThat(client.findPokemon(25)).get().extracting(PokeApiPokemonDTO::name).isEqualTo("pikachu");
        server.verify();
    }

    @Test
    void remembersAnUnknownPokemon() {
        server.expect(once(), requestTo(BASE_URL + "/pokemon/99999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        assertThat(client.findPokemon(99999)).isEmpty();
        assertThat(client.findPokemon(99999)).isEmpty();
        server.verify();
    }

    @Test
    void doesNotCacheFailures() {
        server.expect(once(), requestTo(BASE_URL + "/pokemon-species/25")).andRespond(withServerError());
        expectFixtureOnce("/pokemon-species/25", "pokemon-species-25.json");

        assertThatThrownBy(() -> client.getSpecies(25)).isInstanceOf(ExternalServiceUnavailableException.class);
        assertThat(client.getSpecies(25)).isNotNull();
        server.verify();
    }

    private void expectFixtureOnce(String path, String fixture) {
        server.expect(once(), requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(new ClassPathResource("pokeapi/" + fixture), MediaType.APPLICATION_JSON));
    }

    @Configuration
    static class TestConfig {

        private final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);

        @Bean
        PokeApiProperties pokeApiProperties() {
            Duration timeout = Duration.ofSeconds(1);
            return new PokeApiProperties(URI.create(BASE_URL), timeout, timeout,
                    new PokeApiProperties.Cache(Duration.ofHours(1), 100));
        }

        @Bean
        MockRestServiceServer mockRestServiceServer() {
            return MockRestServiceServer.bindTo(builder).build();
        }

        @Bean(PokeApiClientConfig.POKEAPI_REST_CLIENT)
        RestClient pokeApiRestClient(MockRestServiceServer server) {
            return builder.build();
        }

        @Bean
        PokeApiHttpClient pokeApiHttpClient(RestClient pokeApiRestClient) {
            return new PokeApiHttpClient(pokeApiRestClient);
        }
    }
}

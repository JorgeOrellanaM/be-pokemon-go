package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * HTTP behaviour of the PokeAPI client against recorded PokeAPI responses: deserialization, the
 * meaning of a 404, and translation of every transport failure.
 */
class PokeApiHttpClientTest {

    private static final String BASE_URL = "https://pokeapi.test/api/v2";

    private MockRestServiceServer server;
    private PokeApiHttpClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new PokeApiHttpClient(builder.build());
    }

    @Test
    void readsAPokemon() {
        expectFixture("/pokemon/25", "pokemon-25.json");

        PokeApiPokemonDTO pikachu = client.findPokemon(25).orElseThrow();

        assertThat(pikachu.id()).isEqualTo(25);
        assertThat(pikachu.name()).isEqualTo("pikachu");
        assertThat(pikachu.sprites().other().officialArtwork().frontDefault()).endsWith("/official-artwork/25.png");
        assertThat(pikachu.stats()).hasSize(6);
        assertThat(pikachu.stats().getFirst().baseStat()).isEqualTo(35);
        assertThat(pikachu.types()).extracting(type -> type.type().name()).containsExactly("electric");
        assertThat(pikachu.species().url()).isEqualTo("https://pokeapi.co/api/v2/pokemon-species/25/");
        server.verify();
    }

    @Test
    void returnsEmptyForUnknownPokemon() {
        server.expect(requestTo(BASE_URL + "/pokemon/99999")).andRespond(withResourceNotFound());

        assertThat(client.findPokemon(99999)).isEmpty();
        server.verify();
    }

    @Test
    void readsASpecies() {
        expectFixture("/pokemon-species/25", "pokemon-species-25.json");

        PokeApiSpeciesDTO species = client.getSpecies(25);

        assertThat(species.flavorTextEntries()).extracting(entry -> entry.language().name())
                .containsExactly("ja", "en", "en");
        assertThat(species.genera()).extracting(PokeApiSpeciesDTO.Genus::genus).contains("Mouse Pokémon");
        assertThat(species.evolutionChain().url()).isEqualTo("https://pokeapi.co/api/v2/evolution-chain/10/");
    }

    @Test
    void readsANestedEvolutionChain() {
        expectFixture("/evolution-chain/10", "evolution-chain-10.json");

        PokeApiEvolutionChainDTO.Link pichu = client.getEvolutionChain(10).chain();

        assertThat(pichu.species().name()).isEqualTo("pichu");
        assertThat(pichu.evolvesTo().getFirst().species().name()).isEqualTo("pikachu");
        assertThat(pichu.evolvesTo().getFirst().evolvesTo().getFirst().species().name()).isEqualTo("raichu");
    }

    @Test
    void treatsAMissingSpeciesAsUnavailable() {
        server.expect(requestTo(BASE_URL + "/pokemon-species/25")).andRespond(withResourceNotFound());

        assertThatThrownBy(() -> client.getSpecies(25)).isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void translatesServerErrorsToServiceUnavailable() {
        server.expect(requestTo(BASE_URL + "/pokemon/25")).andRespond(withServerError());

        assertThatThrownBy(() -> client.findPokemon(25))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessage("PokeAPI is unavailable");
    }

    @Test
    void translatesTimeoutsToServiceUnavailable() {
        server.expect(requestTo(BASE_URL + "/pokemon/25")).andRespond(withException(new SocketTimeoutException()));

        assertThatThrownBy(() -> client.findPokemon(25)).isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void translatesUnreadableResponsesToServiceUnavailable() {
        server.expect(requestTo(BASE_URL + "/evolution-chain/10"))
                .andRespond(withSuccess("<html>maintenance</html>", MediaType.TEXT_HTML));

        assertThatThrownBy(() -> client.getEvolutionChain(10))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void translatesEmptyResponsesToServiceUnavailable() {
        server.expect(requestTo(BASE_URL + "/pokemon/25")).andRespond(withStatus(HttpStatus.OK));

        assertThatThrownBy(() -> client.findPokemon(25)).isInstanceOf(ExternalServiceUnavailableException.class);
    }

    private void expectFixture(String path, String fixture) {
        server.expect(requestTo(BASE_URL + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(new ClassPathResource("pokeapi/" + fixture), MediaType.APPLICATION_JSON));
    }
}

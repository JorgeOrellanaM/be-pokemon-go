package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.domain.model.EvolutionStage;
import com.interview.pokemon_go.domain.model.PokemonDetails;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Orchestration of the three PokeAPI resources, tested against {@link FakePokeApiClient}: no HTTP
 * involved, the HTTP behaviour is covered by {@code PokeApiHttpClientTest}.
 */
class PokeApiPokemonDetailsAdapterTest {

    private static final String API = "https://pokeapi.co/api/v2";
    private static final NamedResource ENGLISH = new NamedResource("en", API + "/language/9/");

    private final FakePokeApiClient pokeApi = new FakePokeApiClient();
    private final PokeApiPokemonDetailsAdapter adapter = new PokeApiPokemonDetailsAdapter(pokeApi);

    @Test
    void combinesPokemonSpeciesAndEvolutionChain() {
        pokeApi.withPokemon(25, pokemon(25, "pikachu", 25))
                .withSpecies(25, species("Electric mouse.", 10))
                .withEvolutionChain(10, chain());

        PokemonDetails pikachu = adapter.findById(25).orElseThrow();

        assertThat(pikachu.name()).isEqualTo("pikachu");
        assertThat(pikachu.description()).isEqualTo("Electric mouse.");
        assertThat(pikachu.evolutionChain()).isEqualTo(new EvolutionStage(172, "pichu",
                List.of(new EvolutionStage(25, "pikachu", List.of()))));
        assertThat(pokeApi.requests()).containsExactly("pokemon/25", "pokemon-species/25", "evolution-chain/10");
    }

    @Test
    void followsTheSpeciesLinkRatherThanThePokemonId() {
        // alternate forms have their own Pokemon id but share the species of the base form
        pokeApi.withPokemon(10034, pokemon(10034, "charizard-mega-x", 6))
                .withSpecies(6, species("Flame Pokémon.", 2))
                .withEvolutionChain(2, chain());

        assertThat(adapter.findById(10034)).isPresent();
        assertThat(pokeApi.requests()).containsExactly("pokemon/10034", "pokemon-species/6", "evolution-chain/2");
    }

    @Test
    void returnsEmptyForUnknownPokemonWithoutFurtherRequests() {
        assertThat(adapter.findById(99999)).isEmpty();
        assertThat(pokeApi.requests()).containsExactly("pokemon/99999");
    }

    @Test
    void propagatesAnUnavailablePokeApi() {
        pokeApi.withPokemon(25, pokemon(25, "pikachu", 25));

        assertThatThrownBy(() -> adapter.findById(25)).isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void requiresAClient() {
        assertThatNullPointerException().isThrownBy(() -> new PokeApiPokemonDetailsAdapter(null));
    }

    private static PokeApiPokemonDTO pokemon(int id, String name, int speciesId) {
        return new PokeApiPokemonDTO(id, name,
                new PokeApiPokemonDTO.Sprites("https://example.org/%d.png".formatted(id), null),
                List.of(new PokeApiPokemonDTO.Stat(35, new NamedResource("hp", API + "/stat/1/"))),
                List.of(new PokeApiPokemonDTO.Type(1, new NamedResource("electric", API + "/type/13/"))),
                new NamedResource(name, API + "/pokemon-species/%d/".formatted(speciesId)));
    }

    private static PokeApiSpeciesDTO species(String description, int evolutionChainId) {
        return new PokeApiSpeciesDTO(
                List.of(new PokeApiSpeciesDTO.FlavorText(description, ENGLISH)),
                List.of(new PokeApiSpeciesDTO.Genus("Test Pokémon", ENGLISH)),
                new PokeApiSpeciesDTO.Resource(API + "/evolution-chain/%d/".formatted(evolutionChainId)));
    }

    private static PokeApiEvolutionChainDTO chain() {
        return new PokeApiEvolutionChainDTO(new PokeApiEvolutionChainDTO.Link(
                new NamedResource("pichu", API + "/pokemon-species/172/"),
                List.of(new PokeApiEvolutionChainDTO.Link(
                        new NamedResource("pikachu", API + "/pokemon-species/25/"), List.of()))));
    }
}

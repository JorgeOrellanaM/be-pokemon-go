package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.model.PokemonSummary;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Builders for PokeAPI DTOs shared by the adapter tests that run against {@link FakePokeApiClient}.
 */
final class PokeApiTestData {

    static final String API = "https://pokeapi.co/api/v2";
    static final NamedResource ENGLISH = new NamedResource("en", API + "/language/9/");

    private PokeApiTestData() {
    }

    static NamedResource listEntry(int id, String name) {
        return new NamedResource(name, API + "/pokemon/%d/".formatted(id));
    }

    static PokeApiPokemonDTO pokemon(int id, String name, int speciesId) {
        return new PokeApiPokemonDTO(id, name, 60,
                new PokeApiPokemonDTO.Sprites("https://example.org/%d.png".formatted(id), null),
                List.of(new PokeApiPokemonDTO.AbilitySlot(new NamedResource("static", API + "/ability/9/"), false, 1)),
                List.of(new PokeApiPokemonDTO.Stat(35, new NamedResource("hp", API + "/stat/1/"))),
                List.of(new PokeApiPokemonDTO.Type(1, new NamedResource("electric", API + "/type/13/"))),
                speciesLink(name, speciesId));
    }

    static PokeApiSpeciesDTO species(String category, String description, int evolutionChainId) {
        return new PokeApiSpeciesDTO(
                List.of(new PokeApiSpeciesDTO.FlavorText(description, ENGLISH)),
                List.of(new PokeApiSpeciesDTO.Genus(category, ENGLISH)),
                new PokeApiSpeciesDTO.Resource(API + "/evolution-chain/%d/".formatted(evolutionChainId)));
    }

    /**
     * The PokeAPI resources a catalog adapter needs to rebuild exactly this summary: abilities get
     * consecutive slots in their current order, and the species carries the category.
     */
    static PokeApiPokemonDTO pokemonOf(PokemonSummary summary) {
        List<PokeApiPokemonDTO.AbilitySlot> abilities = IntStream.range(0, summary.abilities().size())
                .mapToObj(i -> new PokeApiPokemonDTO.AbilitySlot(
                        new NamedResource(summary.abilities().get(i).name(), API + "/ability/0/"),
                        summary.abilities().get(i).hidden(),
                        i + 1))
                .toList();
        return new PokeApiPokemonDTO(summary.id(), summary.name(), (int) summary.weight().hectograms(),
                new PokeApiPokemonDTO.Sprites(summary.spriteUrl(), null), abilities, List.of(), List.of(),
                speciesLink(summary.name(), summary.id()));
    }

    static PokeApiSpeciesDTO speciesOf(PokemonSummary summary) {
        List<PokeApiSpeciesDTO.Genus> genera = Optional.ofNullable(summary.category())
                .map(category -> List.of(new PokeApiSpeciesDTO.Genus(category, ENGLISH)))
                .orElse(List.of());
        return new PokeApiSpeciesDTO(List.of(), genera, new PokeApiSpeciesDTO.Resource(API + "/evolution-chain/1/"));
    }

    private static NamedResource speciesLink(String name, int speciesId) {
        return new NamedResource(name, API + "/pokemon-species/%d/".formatted(speciesId));
    }
}

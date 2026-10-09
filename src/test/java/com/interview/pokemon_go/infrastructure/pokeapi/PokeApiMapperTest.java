package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.model.BaseStat;
import com.interview.pokemon_go.domain.model.EvolutionStage;
import com.interview.pokemon_go.domain.model.PokemonDetails;
import com.interview.pokemon_go.infrastructure.pokeapi.PokeApiSpeciesDTO.FlavorText;
import com.interview.pokemon_go.infrastructure.pokeapi.PokeApiSpeciesDTO.Genus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PokeApiMapperTest {

    private static final String SPECIES_URL = "https://pokeapi.co/api/v2/pokemon-species/%d/";
    private static final NamedResource ENGLISH = new NamedResource("en", "https://pokeapi.co/api/v2/language/9/");
    private static final NamedResource JAPANESE = new NamedResource("ja", "https://pokeapi.co/api/v2/language/11/");

    @Test
    void mapsAllAttributes() {
        PokemonDetails details = PokeApiMapper.toDomain(pokemon(new PokeApiPokemonDTO.Sprites("sprite.png",
                new PokeApiPokemonDTO.OtherSprites(new PokeApiPokemonDTO.Artwork("artwork.png")))), species(
                List.of(new FlavorText("Electric mouse.", ENGLISH))), chain());

        assertThat(details.id()).isEqualTo(25);
        assertThat(details.name()).isEqualTo("pikachu");
        assertThat(details.imageUrl()).isEqualTo("artwork.png");
        assertThat(details.category()).isEqualTo("Mouse Pokémon");
        assertThat(details.types()).containsExactly("electric", "steel");
        assertThat(details.stats()).containsExactly(new BaseStat("hp", 35), new BaseStat("speed", 90));
        assertThat(details.description()).isEqualTo("Electric mouse.");
        assertThat(details.evolutionChain()).isEqualTo(new EvolutionStage(172, "pichu",
                List.of(new EvolutionStage(25, "pikachu", List.of()))));
    }

    @Test
    void fallsBackToTheDefaultSpriteWithoutOfficialArtwork() {
        PokemonDetails details = PokeApiMapper.toDomain(
                pokemon(new PokeApiPokemonDTO.Sprites("sprite.png", null)), species(List.of()), chain());

        assertThat(details.imageUrl()).isEqualTo("sprite.png");
    }

    @Test
    void usesTheFirstEnglishFlavorText() {
        String description = PokeApiMapper.description(List.of(
                new FlavorText("ピカチュウ", JAPANESE),
                new FlavorText("First.", ENGLISH),
                new FlavorText("Second.", ENGLISH)));

        assertThat(description).isEqualTo("First.");
    }

    @Test
    void replacesPokeApiLineBreaksWithSpaces() {
        String description = PokeApiMapper.description(List.of(
                new FlavorText("When several of\nthese POKéMON\ngather, their\felectricity could", ENGLISH)));

        assertThat(description).isEqualTo("When several of these POKéMON gather, their electricity could");
    }

    @Test
    void joinsWordsSplitBySoftHyphenAtLineEnd() {
        assertThat(PokeApiMapper.description(List.of(new FlavorText("elec­\ntricity", ENGLISH))))
                .isEqualTo("electricity");
    }

    @Test
    void returnsEmptyDescriptionWithoutEnglishText() {
        assertThat(PokeApiMapper.description(List.of(new FlavorText("ピカチュウ", JAPANESE)))).isEmpty();
        assertThat(PokeApiMapper.description(null)).isEmpty();
    }

    @Test
    void readsTheIdAtTheEndOfAResourceUrl() {
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/pokemon-species/133/")).isEqualTo(133);
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/pokemon-species/133")).isEqualTo(133);
    }

    @Test
    void rejectsAUrlWithoutId() {
        assertThatIllegalArgumentException().isThrownBy(() -> PokeApiMapper.idFromUrl("https://pokeapi.co/"));
    }

    private static PokeApiPokemonDTO pokemon(PokeApiPokemonDTO.Sprites sprites) {
        return new PokeApiPokemonDTO(25, "pikachu", sprites,
                List.of(new PokeApiPokemonDTO.Stat(35, new NamedResource("hp", "")),
                        new PokeApiPokemonDTO.Stat(90, new NamedResource("speed", ""))),
                // deliberately out of slot order
                List.of(new PokeApiPokemonDTO.Type(2, new NamedResource("steel", "")),
                        new PokeApiPokemonDTO.Type(1, new NamedResource("electric", ""))),
                new NamedResource("pikachu", SPECIES_URL.formatted(25)));
    }

    private static PokeApiSpeciesDTO species(List<FlavorText> flavorTexts) {
        return new PokeApiSpeciesDTO(flavorTexts,
                List.of(new Genus("ねずみポケモン", JAPANESE), new Genus("Mouse Pokémon", ENGLISH)),
                new PokeApiSpeciesDTO.Resource("https://pokeapi.co/api/v2/evolution-chain/10/"));
    }

    private static PokeApiEvolutionChainDTO chain() {
        return new PokeApiEvolutionChainDTO(new PokeApiEvolutionChainDTO.Link(
                new NamedResource("pichu", SPECIES_URL.formatted(172)),
                List.of(new PokeApiEvolutionChainDTO.Link(
                        new NamedResource("pikachu", SPECIES_URL.formatted(25)), List.of()))));
    }
}

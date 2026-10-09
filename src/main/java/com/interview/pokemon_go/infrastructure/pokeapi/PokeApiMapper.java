package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.domain.model.BaseStat;
import com.interview.pokemon_go.domain.model.EvolutionStage;
import com.interview.pokemon_go.domain.model.PokemonDetails;
import com.interview.pokemon_go.infrastructure.pokeapi.PokeApiSpeciesDTO.FlavorText;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

final class PokeApiMapper {

    private static final String LANGUAGE = "en";
    private static final Pattern SOFT_HYPHEN_LINE_BREAK = Pattern.compile("­\\s+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern TRAILING_ID = Pattern.compile(".*/(\\d+)/?$");

    private PokeApiMapper() {
    }

    static PokemonDetails toDomain(PokeApiPokemonDTO pokemon, PokeApiSpeciesDTO species,
                                   PokeApiEvolutionChainDTO evolutionChain) {
        return new PokemonDetails(
                pokemon.id(),
                pokemon.name(),
                imageUrl(pokemon.sprites()),
                category(species),
                types(pokemon),
                pokemon.stats().stream()
                        .map(stat -> new BaseStat(stat.stat().name(), stat.baseStat()))
                        .toList(),
                description(species.flavorTextEntries()),
                toStage(evolutionChain.chain()));
    }

    /**
     * The official artwork is the large image meant for a detail page; not every Pokemon form has
     * one, so the small default sprite is the fallback.
     */
    private static String imageUrl(PokeApiPokemonDTO.Sprites sprites) {
        return Optional.ofNullable(sprites.other())
                .map(PokeApiPokemonDTO.OtherSprites::officialArtwork)
                .map(PokeApiPokemonDTO.Artwork::frontDefault)
                .orElse(sprites.frontDefault());
    }

    private static String category(PokeApiSpeciesDTO species) {
        return Optional.ofNullable(species.genera()).orElse(List.of()).stream()
                .filter(genus -> LANGUAGE.equals(genus.language().name()))
                .map(PokeApiSpeciesDTO.Genus::genus)
                .findFirst()
                .orElse(null);
    }

    /**
     * PokeAPI lists a Pokemon's types by slot (primary first), not necessarily in that order.
     */
    private static List<String> types(PokeApiPokemonDTO pokemon) {
        return pokemon.types().stream()
                .sorted(Comparator.comparingInt(PokeApiPokemonDTO.Type::slot))
                .map(type -> type.type().name())
                .toList();
    }

    /**
     * Uses the first English entry. The texts are copied from the games' text boxes, so they
     * contain line breaks and form feeds where the box ended, and soft hyphens where a word was split
     * across lines: split words are joined and every line break becomes a single space. No English
     * entry gives an empty description.
     */
    static String description(List<FlavorText> entries) {
        return Optional.ofNullable(entries).orElse(List.of()).stream()
                .filter(entry -> LANGUAGE.equals(entry.language().name()))
                .map(FlavorText::flavorText)
                .findFirst()
                .map(text -> SOFT_HYPHEN_LINE_BREAK.matcher(text).replaceAll(""))
                .map(text -> WHITESPACE.matcher(text).replaceAll(" ").strip())
                .orElse("");
    }

    /**
     * The evolution chain only names species and links to them; the Pokédex number is the last
     * segment of that link (e.g. {@code …/pokemon-species/133/}).
     */
    static int idFromUrl(String url) {
        var matcher = TRAILING_ID.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("PokeAPI resource URL has no id: " + url);
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static EvolutionStage toStage(PokeApiEvolutionChainDTO.Link link) {
        return new EvolutionStage(
                idFromUrl(link.species().url()),
                link.species().name(),
                link.evolvesTo().stream().map(PokeApiMapper::toStage).toList());
    }
}

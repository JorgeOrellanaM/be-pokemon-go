package com.interview.pokemon_go.domain.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * A Pokemon replicated into the local store (US03): the data copied from PokeAPI plus the fields this
 * service owns. {@code localizedName} and {@code region} are null until someone sets them; {@code tags}
 * is empty, never null.
 */
public record LocalPokemon(
        PokemonSummary pokemon,
        String localizedName,
        String region,
        List<String> tags) {

    public LocalPokemon {
        Objects.requireNonNull(pokemon, "pokemon must not be null");
        requireNotBlankIfPresent(localizedName, "localizedName");
        requireNotBlankIfPresent(region, "region");
        tags = List.copyOf(Objects.requireNonNull(tags, "tags must not be null"));
        if (tags.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("tags must not be blank");
        }
        if (new HashSet<>(tags).size() != tags.size()) {
            throw new IllegalArgumentException("tags must not repeat");
        }
    }

    /**
     * A freshly synced Pokemon carries only PokeAPI data; the custom fields are filled in later.
     */
    public static LocalPokemon replicaOf(PokemonSummary pokemon) {
        return new LocalPokemon(pokemon, null, null, List.of());
    }

    public int id() {
        return pokemon.id();
    }

    private static void requireNotBlankIfPresent(String value, String name) {
        if (value != null && value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}

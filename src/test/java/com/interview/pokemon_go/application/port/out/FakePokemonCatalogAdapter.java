package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;

import java.util.Comparator;
import java.util.List;

/**
 * Hand-written, in-memory fake of {@link PokemonCatalogPort} for use-case tests. Verified against the
 * same {@link PokemonCatalogPortContractTest} as the real adapter.
 */
public class FakePokemonCatalogAdapter implements PokemonCatalogPort {

    private static final String SPRITE_URL =
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%d.png";

    private static final List<PokemonSummary> DEMO_CATALOG = List.of(
            pokemon(1, "bulbasaur", "Seed Pokémon", 69, "overgrow", "chlorophyll"),
            pokemon(2, "ivysaur", "Seed Pokémon", 130, "overgrow", "chlorophyll"),
            pokemon(3, "venusaur", "Seed Pokémon", 1000, "overgrow", "chlorophyll"),
            pokemon(4, "charmander", "Lizard Pokémon", 85, "blaze", "solar-power"),
            pokemon(5, "charmeleon", "Flame Pokémon", 190, "blaze", "solar-power"),
            pokemon(6, "charizard", "Flame Pokémon", 905, "blaze", "solar-power"),
            pokemon(7, "squirtle", "Tiny Turtle Pokémon", 90, "torrent", "rain-dish"),
            pokemon(8, "wartortle", "Turtle Pokémon", 225, "torrent", "rain-dish"),
            pokemon(9, "blastoise", "Shellfish Pokémon", 855, "torrent", "rain-dish"),
            pokemon(25, "pikachu", "Mouse Pokémon", 60, "static", "lightning-rod"));

    private final List<PokemonSummary> catalog;

    public FakePokemonCatalogAdapter() {
        this(DEMO_CATALOG);
    }

    public FakePokemonCatalogAdapter(List<PokemonSummary> pokemon) {
        this.catalog = pokemon.stream().sorted(Comparator.comparingInt(PokemonSummary::id)).toList();
    }

    @Override
    public PageResult<PokemonSummary> findPage(PageQuery query) {
        int from = Math.min(query.offset(), catalog.size());
        int to = Math.min(from + query.size(), catalog.size());
        return new PageResult<>(catalog.subList(from, to), query.page(), query.size(), catalog.size());
    }

    private static PokemonSummary pokemon(int id, String name, String category, double hectograms,
                                          String ability, String hiddenAbility) {
        return new PokemonSummary(
                id,
                name,
                SPRITE_URL.formatted(id),
                category,
                new Weight(hectograms),
                List.of(new Ability(ability, false), new Ability(hiddenAbility, true)));
    }
}

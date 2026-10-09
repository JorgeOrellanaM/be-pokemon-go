package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.exception.PokemonAlreadySyncedException;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonCustomization;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Hand-written, in-memory fake of {@link LocalPokemonPort} for use-case tests. Verified against the
 * same {@link LocalPokemonPortContractTest} as the database adapter. Counts saves so tests can prove
 * nothing was stored.
 */
public class InMemoryLocalPokemonAdapter implements LocalPokemonPort {

    // keyed and sorted by Pokédex number, like the database adapter's pages
    private final Map<Integer, LocalPokemon> pokemon = new TreeMap<>();
    private int saves;

    @Override
    public boolean existsById(int id) {
        return pokemon.containsKey(id);
    }

    @Override
    public LocalPokemon save(LocalPokemon local) {
        saves++;
        if (pokemon.putIfAbsent(local.id(), local) != null) {
            throw new PokemonAlreadySyncedException(local.id());
        }
        return local;
    }

    @Override
    public Optional<LocalPokemon> findById(int id) {
        return Optional.ofNullable(pokemon.get(id));
    }

    @Override
    public Optional<LocalPokemon> updateCustomization(int id, PokemonCustomization customization) {
        return Optional.ofNullable(pokemon.computeIfPresent(id, (key, local) -> local.customizedWith(customization)));
    }

    @Override
    public PageResult<LocalPokemon> findPage(PageQuery query) {
        List<LocalPokemon> all = List.copyOf(pokemon.values());
        int from = Math.min(query.offset(), all.size());
        int to = Math.min(from + query.size(), all.size());
        return new PageResult<>(all.subList(from, to), query.page(), query.size(), all.size());
    }

    public int saves() {
        return saves;
    }
}

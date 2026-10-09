package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonCustomization;

import java.util.Optional;

/**
 * The local store of synced Pokemon (US03), identified by Pokédex number. Pages are ordered by
 * Pokédex number. Saving a Pokemon that is already stored throws
 * {@link com.interview.pokemon_go.domain.exception.PokemonAlreadySyncedException}. The shared contract
 * is verified by {@code LocalPokemonPortContractTest}.
 */
public interface LocalPokemonPort {

    boolean existsById(int id);

    LocalPokemon save(LocalPokemon pokemon);

    Optional<LocalPokemon> findById(int id);

    /**
     * Replaces the custom fields of a stored Pokemon in one step; the PokeAPI data is left untouched.
     * An id that is not stored is an empty result.
     */
    Optional<LocalPokemon> updateCustomization(int id, PokemonCustomization customization);

    PageResult<LocalPokemon> findPage(PageQuery query);
}

package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;

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

    PageResult<LocalPokemon> findPage(PageQuery query);
}

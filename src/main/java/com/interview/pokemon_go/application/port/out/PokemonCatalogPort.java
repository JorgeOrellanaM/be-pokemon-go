package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;

import java.util.Optional;

/**
 * Source of the Pokemon catalog. Implementations return pages ordered by Pokédex number, return an
 * unknown Pokédex number as an empty result, and translate infrastructure failures into
 * {@link com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException}.
 * The shared contract is verified by {@code PokemonCatalogPortContractTest}.
 */
public interface PokemonCatalogPort {

    PageResult<PokemonSummary> findPage(PageQuery query);

    Optional<PokemonSummary> findById(int id);
}

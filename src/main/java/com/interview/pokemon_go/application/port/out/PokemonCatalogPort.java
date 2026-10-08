package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;

/**
 * Source of the Pokemon catalog. Implementations return pages ordered by Pokédex number and translate
 * infrastructure failures into {@link com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException}.
 * The shared contract is verified by {@code PokemonCatalogPortContractTest}.
 */
public interface PokemonCatalogPort {

    PageResult<PokemonSummary> findPage(PageQuery query);
}

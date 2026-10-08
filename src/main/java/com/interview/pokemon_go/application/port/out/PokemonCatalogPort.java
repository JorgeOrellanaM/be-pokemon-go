package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;

/**
 * External Pokemon catalog (PokeAPI). Implementations must translate transport
 * failures into {@link com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException}.
 */
public interface PokemonCatalogPort {

    PageResult<PokemonSummary> findPage(PageQuery query);
}

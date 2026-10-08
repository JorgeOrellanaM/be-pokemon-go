package com.interview.pokemon_go.application.port.in;

import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;

public interface ListPokemonUseCase {

    PageResult<PokemonSummary> list(PageQuery query);
}

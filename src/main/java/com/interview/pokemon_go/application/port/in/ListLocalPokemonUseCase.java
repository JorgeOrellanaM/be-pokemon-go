package com.interview.pokemon_go.application.port.in;

import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;

public interface ListLocalPokemonUseCase {

    PageResult<LocalPokemon> list(PageQuery query);
}

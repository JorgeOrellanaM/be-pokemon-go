package com.interview.pokemon_go.application.port.in;

import com.interview.pokemon_go.domain.model.LocalPokemon;

public interface GetLocalPokemonUseCase {

    LocalPokemon getById(int id);
}

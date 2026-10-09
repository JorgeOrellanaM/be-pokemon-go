package com.interview.pokemon_go.application.port.in;

import com.interview.pokemon_go.domain.model.PokemonDetails;

public interface GetPokemonDetailsUseCase {

    PokemonDetails getById(int id);
}

package com.interview.pokemon_go.application.port.in;

import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PokemonCustomization;

public interface UpdateLocalPokemonUseCase {

    LocalPokemon update(int id, PokemonCustomization customization);
}

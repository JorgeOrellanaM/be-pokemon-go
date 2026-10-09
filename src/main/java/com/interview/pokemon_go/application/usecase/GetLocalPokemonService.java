package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.GetLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.out.LocalPokemonPort;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.LocalPokemon;

import java.util.Objects;

public class GetLocalPokemonService implements GetLocalPokemonUseCase {

    private final LocalPokemonPort local;

    public GetLocalPokemonService(LocalPokemonPort local) {
        this.local = Objects.requireNonNull(local, "local must not be null");
    }

    @Override
    public LocalPokemon getById(int id) {
        PokedexIds.requireValid(id);
        return local.findById(id).orElseThrow(() -> new PokemonNotFoundException(id));
    }
}

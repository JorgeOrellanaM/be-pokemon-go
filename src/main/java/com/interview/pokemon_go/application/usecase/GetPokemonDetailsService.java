package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.GetPokemonDetailsUseCase;
import com.interview.pokemon_go.application.port.out.PokemonDetailsPort;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.PokemonDetails;

import java.util.Objects;

public class GetPokemonDetailsService implements GetPokemonDetailsUseCase {

    private final PokemonDetailsPort details;

    public GetPokemonDetailsService(PokemonDetailsPort details) {
        this.details = Objects.requireNonNull(details, "details must not be null");
    }

    @Override
    public PokemonDetails getById(int id) {
        PokedexIds.requireValid(id);
        return details.findById(id).orElseThrow(() -> new PokemonNotFoundException(id));
    }
}

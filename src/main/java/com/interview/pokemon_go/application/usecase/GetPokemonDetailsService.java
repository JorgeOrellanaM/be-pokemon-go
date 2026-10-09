package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.GetPokemonDetailsUseCase;
import com.interview.pokemon_go.application.port.out.PokemonDetailsPort;
import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.PokemonDetails;

import java.util.Objects;

public class GetPokemonDetailsService implements GetPokemonDetailsUseCase {

    static final String INVALID_ID = "id must be a positive whole number";

    private final PokemonDetailsPort details;

    public GetPokemonDetailsService(PokemonDetailsPort details) {
        this.details = Objects.requireNonNull(details, "details must not be null");
    }

    /**
     * Pokédex numbers start at 1, so a non-positive id is rejected as invalid input before any
     * lookup; an id that is valid but unknown is a not-found.
     */
    @Override
    public PokemonDetails getById(int id) {
        if (id <= 0) {
            throw new DomainValidationException(INVALID_ID);
        }
        return details.findById(id).orElseThrow(() -> new PokemonNotFoundException(id));
    }
}

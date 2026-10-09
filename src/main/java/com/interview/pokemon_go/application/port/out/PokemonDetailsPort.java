package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.PokemonDetails;

import java.util.Optional;

/**
 * Source of the full details of one Pokemon, identified by its Pokédex number. An unknown id is an
 * empty result, not an error; infrastructure failures are translated into
 * {@link com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException}.
 */
public interface PokemonDetailsPort {

    Optional<PokemonDetails> findById(int id);
}

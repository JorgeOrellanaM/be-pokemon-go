package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.PokemonDetails;

import java.util.List;
import java.util.Optional;

/**
 * Hand-written, in-memory fake of {@link PokemonDetailsPort} for use-case tests. Counts lookups so
 * tests can prove the port was not called.
 */
public class InMemoryPokemonDetailsAdapter implements PokemonDetailsPort {

    private final List<PokemonDetails> pokemon;
    private int lookups;

    public InMemoryPokemonDetailsAdapter(List<PokemonDetails> pokemon) {
        this.pokemon = List.copyOf(pokemon);
    }

    @Override
    public Optional<PokemonDetails> findById(int id) {
        lookups++;
        return pokemon.stream().filter(details -> details.id() == id).findFirst();
    }

    public int lookups() {
        return lookups;
    }
}

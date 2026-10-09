package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.UpdateLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.out.LocalPokemonPort;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PokemonCustomization;

import java.util.Objects;

public class UpdateLocalPokemonService implements UpdateLocalPokemonUseCase {

    private final LocalPokemonPort local;

    public UpdateLocalPokemonService(LocalPokemonPort local) {
        this.local = Objects.requireNonNull(local, "local must not be null");
    }

    /**
     * US04 - only the fields this service owns can change; the PokeAPI data stays a faithful copy.
     * The customization was already validated when it was built, so only a Pokemon that was never
     * synced can still fail (404).
     */
    @Override
    public LocalPokemon update(int id, PokemonCustomization customization) {
        Objects.requireNonNull(customization, "customization must not be null");
        PokedexIds.requireValid(id);
        return local.updateCustomization(id, customization).orElseThrow(() -> new PokemonNotFoundException(id));
    }
}

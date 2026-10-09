package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.SyncPokemonUseCase;
import com.interview.pokemon_go.application.port.out.LocalPokemonPort;
import com.interview.pokemon_go.application.port.out.PokemonCatalogPort;
import com.interview.pokemon_go.domain.exception.PokemonAlreadySyncedException;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.LocalPokemon;

import java.util.Objects;

public class SyncPokemonService implements SyncPokemonUseCase {

    private final PokemonCatalogPort catalog;
    private final LocalPokemonPort local;

    public SyncPokemonService(PokemonCatalogPort catalog, LocalPokemonPort local) {
        this.catalog = Objects.requireNonNull(catalog, "catalog must not be null");
        this.local = Objects.requireNonNull(local, "local must not be null");
    }

    /**
     * US03 - copies one Pokemon from the catalog into the local store, with empty custom fields.
     * A Pokemon is synced only once: re-syncing would overwrite the custom fields, so it is a conflict.
     * The local store is checked first because it is cheap and saves the remote calls; the store still
     * rejects a duplicate when two syncs of the same Pokemon race past this check.
     */
    @Override
    public LocalPokemon sync(int id) {
        PokedexIds.requireValid(id);
        if (local.existsById(id)) {
            throw new PokemonAlreadySyncedException(id);
        }
        return catalog.findById(id)
                .map(LocalPokemon::replicaOf)
                .map(local::save)
                .orElseThrow(() -> new PokemonNotFoundException(id));
    }
}

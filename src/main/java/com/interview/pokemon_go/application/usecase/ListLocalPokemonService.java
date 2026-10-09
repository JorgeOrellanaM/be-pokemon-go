package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.ListLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.out.LocalPokemonPort;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;

import java.util.Objects;

public class ListLocalPokemonService implements ListLocalPokemonUseCase {

    private final LocalPokemonPort local;

    public ListLocalPokemonService(LocalPokemonPort local) {
        this.local = Objects.requireNonNull(local, "local must not be null");
    }

    @Override
    public PageResult<LocalPokemon> list(PageQuery query) {
        Objects.requireNonNull(query, "query must not be null");
        return local.findPage(query);
    }
}

package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.ListPokemonUseCase;
import com.interview.pokemon_go.application.port.out.PokemonCatalogPort;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;

import java.util.Objects;

public class ListPokemonService implements ListPokemonUseCase {

    private final PokemonCatalogPort catalog;

    public ListPokemonService(PokemonCatalogPort catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog must not be null");
    }

    @Override
    public PageResult<PokemonSummary> list(PageQuery query) {
        Objects.requireNonNull(query, "query must not be null");
        return catalog.findPage(query);
    }
}

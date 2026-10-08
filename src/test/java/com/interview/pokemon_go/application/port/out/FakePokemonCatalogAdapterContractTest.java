package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.PokemonSummary;

import java.util.List;

class FakePokemonCatalogAdapterContractTest extends PokemonCatalogPortContractTest {

    @Override
    protected PokemonCatalogPort catalogWith(List<PokemonSummary> pokemon) {
        return new FakePokemonCatalogAdapter(pokemon);
    }
}

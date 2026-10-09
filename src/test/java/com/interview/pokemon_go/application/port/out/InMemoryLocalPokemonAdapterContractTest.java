package com.interview.pokemon_go.application.port.out;

class InMemoryLocalPokemonAdapterContractTest extends LocalPokemonPortContractTest {

    @Override
    protected LocalPokemonPort emptyStore() {
        return new InMemoryLocalPokemonAdapter();
    }
}

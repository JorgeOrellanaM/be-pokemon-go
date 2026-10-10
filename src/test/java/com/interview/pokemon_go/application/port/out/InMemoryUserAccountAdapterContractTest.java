package com.interview.pokemon_go.application.port.out;

class InMemoryUserAccountAdapterContractTest extends UserAccountPortContractTest {

    @Override
    protected UserAccountPort emptyStore() {
        return new InMemoryUserAccountAdapter();
    }
}

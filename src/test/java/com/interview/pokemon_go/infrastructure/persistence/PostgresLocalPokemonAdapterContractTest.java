package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.application.port.out.LocalPokemonPort;
import com.interview.pokemon_go.application.port.out.LocalPokemonPortContractTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Runs the shared local-store contract against PostgreSQL. Each test runs in a transaction that is
 * rolled back, so deleting the seed rows here leaves the database untouched.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import(PostgresLocalPokemonAdapter.class)
class PostgresLocalPokemonAdapterContractTest extends LocalPokemonPortContractTest {

    @Autowired
    private SpringDataPokemonRepository repository;

    @Autowired
    private PostgresLocalPokemonAdapter adapter;

    @Override
    protected LocalPokemonPort emptyStore() {
        repository.deleteAll();
        // IDENTITY inserts run immediately, so the seed rows must be deleted before the test saves
        repository.flush();
        return adapter;
    }
}

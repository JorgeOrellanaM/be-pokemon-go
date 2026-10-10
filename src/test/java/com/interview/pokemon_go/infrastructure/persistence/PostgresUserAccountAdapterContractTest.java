package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.application.port.out.UserAccountPort;
import com.interview.pokemon_go.application.port.out.UserAccountPortContractTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Runs the shared user-store contract against PostgreSQL. Each test runs in a transaction that is
 * rolled back, so deleting the seeded demo user here leaves the database untouched.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import(PostgresUserAccountAdapter.class)
class PostgresUserAccountAdapterContractTest extends UserAccountPortContractTest {

    @Autowired
    private SpringDataUserRepository repository;

    @Autowired
    private PostgresUserAccountAdapter adapter;

    @Override
    protected UserAccountPort emptyStore() {
        repository.deleteAll();
        repository.flush();
        return adapter;
    }
}

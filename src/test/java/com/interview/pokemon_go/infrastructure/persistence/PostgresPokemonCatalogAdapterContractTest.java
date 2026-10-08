package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.application.port.out.PokemonCatalogPort;
import com.interview.pokemon_go.application.port.out.PokemonCatalogPortContractTest;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Runs the shared catalog contract against the local PostgreSQL database. Each test is rolled back.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import(PostgresPokemonCatalogAdapter.class)
class PostgresPokemonCatalogAdapterContractTest extends PokemonCatalogPortContractTest {

    @Autowired
    private SpringDataPokemonRepository repository;

    @Autowired
    private PostgresPokemonCatalogAdapter adapter;

    @Autowired
    private TestEntityManager entityManager;

    @Override
    protected PokemonCatalogPort catalogWith(List<PokemonSummary> pokemon) {
        repository.deleteAll();
        repository.flush();
        repository.saveAll(pokemon.stream().map(PokemonPersistenceMapper::toEntity).toList());
        repository.flush();
        entityManager.clear();
        return adapter;
    }
}

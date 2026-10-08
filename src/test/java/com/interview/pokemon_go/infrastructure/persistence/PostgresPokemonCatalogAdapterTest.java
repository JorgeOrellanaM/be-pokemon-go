package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Runs against the local PostgreSQL database. Each test runs in a transaction that is rolled back,
 * so the seeded data is left untouched.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import(PostgresPokemonCatalogAdapter.class)
class PostgresPokemonCatalogAdapterTest {

    @Autowired
    private SpringDataPokemonRepository repository;

    @Autowired
    private PostgresPokemonCatalogAdapter adapter;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        // IDENTITY inserts run immediately, so the seed rows must be deleted before saving fixtures
        repository.flush();
        // saved out of Pokédex order, so generated ids differ from Pokédex order: proves the sort key
        repository.saveAll(List.of(
                pokemon(3, "venusaur"),
                pokemon(1, "bulbasaur"),
                pokemon(5, "charmeleon"),
                pokemon(2, "ivysaur"),
                pokemon(4, "charmander")));
        repository.flush();
        // detach fixtures so the adapter reads real rows instead of cached instances
        entityManager.clear();
    }

    @Test
    void returnsFirstPageOrderedByPokedexNumber() {
        PageResult<PokemonSummary> result = adapter.findPage(new PageQuery(0, 2));

        assertThat(result.items()).extracting(PokemonSummary::id).containsExactly(1, 2);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void returnsLastPartialPage() {
        PageResult<PokemonSummary> result = adapter.findPage(new PageQuery(2, 2));

        assertThat(result.items()).extracting(PokemonSummary::id).containsExactly(5);
        assertThat(result.totalElements()).isEqualTo(5);
    }

    @Test
    void returnsEmptyItemsWhenPageIsBeyondTheEnd() {
        PageResult<PokemonSummary> result = adapter.findPage(new PageQuery(10, 2));

        assertThat(result.items()).isEmpty();
        assertThat(result.page()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(5);
    }

    @Test
    void mapsWeightAndAbilities() {
        PokemonSummary first = adapter.findPage(new PageQuery(0, 1)).items().getFirst();

        assertThat(first.name()).isEqualTo("bulbasaur");
        assertThat(first.spriteUrl()).isEqualTo("https://example.org/1.png");
        assertThat(first.category()).isEqualTo("Seed Pokémon");
        assertThat(first.weight().kilograms()).isEqualTo(1.0);
        assertThat(first.abilities()).containsExactly(
                new Ability("overgrow", false), new Ability("chlorophyll", true));
    }

    @Test
    void assignsAnAutoIncrementedPrimaryKeyToEveryPokemon() {
        List<PokemonEntity> pokemon = repository.findAll();

        assertThat(pokemon).hasSize(5);
        assertThat(pokemon).extracting(PokemonEntity::getId).doesNotContainNull().doesNotHaveDuplicates();
    }

    @Test
    void assignsAUniquePrimaryKeyToEveryAbility() {
        List<AbilityEntity> abilities = entityManager.getEntityManager()
                .createQuery("select a from AbilityEntity a", AbilityEntity.class)
                .getResultList();

        assertThat(abilities).hasSize(10);
        assertThat(abilities).extracting(AbilityEntity::getId).doesNotContainNull().doesNotHaveDuplicates();
    }

    private static PokemonEntity pokemon(int id, String name) {
        PokemonEntity entity = new PokemonEntity(
                id, name, "https://example.org/%d.png".formatted(id), "Seed Pokémon", id * 10);
        entity.addAbility("chlorophyll", true);
        entity.addAbility("overgrow", false);
        return entity;
    }
}

package com.interview.pokemon_go.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Runs against the local PostgreSQL database. Each test runs in a transaction that is rolled back,
 * so the seeded data is left untouched.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class SpringDataPokemonRepositoryTest {

    @Autowired
    private SpringDataPokemonRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        // IDENTITY inserts run immediately, so the seed rows must be deleted before saving fixtures
        repository.flush();
        repository.saveAll(List.of(pokemon(3, "venusaur"), pokemon(1, "bulbasaur"), pokemon(2, "ivysaur")));
        repository.flush();
        entityManager.clear();
    }

    @Test
    void assignsAnAutoIncrementedPrimaryKeyToEveryPokemon() {
        List<PokemonEntity> pokemon = repository.findAll();

        assertThat(pokemon).hasSize(3);
        assertThat(pokemon).extracting(PokemonEntity::getId).doesNotContainNull().doesNotHaveDuplicates();
    }

    @Test
    void assignsAUniquePrimaryKeyToEveryAbility() {
        List<AbilityEntity> abilities = entityManager.getEntityManager()
                .createQuery("select a from AbilityEntity a", AbilityEntity.class)
                .getResultList();

        assertThat(abilities).hasSize(6);
        assertThat(abilities).extracting(AbilityEntity::getId).doesNotContainNull().doesNotHaveDuplicates();
    }

    @Test
    void readsAbilitiesWithTheHiddenOneLast() {
        PokemonEntity bulbasaur = repository.findAll().stream()
                .filter(entity -> entity.getPokedexNumber() == 1)
                .findFirst()
                .orElseThrow();

        assertThat(bulbasaur.getAbilities()).extracting(AbilityEntity::getName)
                .containsExactly("overgrow", "chlorophyll");
    }

    private static PokemonEntity pokemon(int pokedexNumber, String name) {
        PokemonEntity entity = new PokemonEntity(
                pokedexNumber, name, "https://example.org/%d.png".formatted(pokedexNumber), "Seed Pokémon",
                pokedexNumber * 10);
        entity.addAbility("chlorophyll", true);
        entity.addAbility("overgrow", false);
        return entity;
    }
}

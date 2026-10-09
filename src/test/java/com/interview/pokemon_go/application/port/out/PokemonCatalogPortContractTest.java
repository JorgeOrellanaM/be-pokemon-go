package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behaviour every {@link PokemonCatalogPort} implementation must share (LSP), so the fake used in
 * use-case tests cannot drift from the real adapter. Each implementation extends this class.
 * <p>
 * {@code @Transactional} is needed here, not only on Spring subclasses: Spring resolves it from the
 * class that declares the test method, so without it database-backed runs would commit instead of
 * rolling back. Plain JUnit runs (the fake) ignore it.
 */
@Transactional
public abstract class PokemonCatalogPortContractTest {

    private PokemonCatalogPort catalog;

    /**
     * Returns an implementation containing exactly the given Pokemon.
     */
    protected abstract PokemonCatalogPort catalogWith(List<PokemonSummary> pokemon);

    @BeforeEach
    void createCatalog() {
        // deliberately out of Pokédex order
        catalog = catalogWith(List.of(
                pokemon(25, "pikachu"),
                pokemon(3, "venusaur"),
                pokemon(1, "bulbasaur"),
                pokemon(7, "squirtle"),
                pokemon(4, "charmander")));
    }

    @Test
    void returnsPagesOrderedByPokedexNumber() {
        PageResult<PokemonSummary> result = catalog.findPage(new PageQuery(0, 3));

        assertThat(result.items()).extracting(PokemonSummary::id).containsExactly(1, 3, 4);
    }

    @Test
    void reportsRequestedPageAndTotals() {
        PageResult<PokemonSummary> result = catalog.findPage(new PageQuery(1, 2));

        assertThat(result.items()).extracting(PokemonSummary::id).containsExactly(4, 7);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void returnsPartialLastPage() {
        assertThat(catalog.findPage(new PageQuery(2, 2)).items())
                .extracting(PokemonSummary::id).containsExactly(25);
    }

    @Test
    void returnsEmptyItemsBeyondTheEnd() {
        PageResult<PokemonSummary> result = catalog.findPage(new PageQuery(9, 2));

        assertThat(result.items()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(5);
    }

    @Test
    void returnsEmptyPageForEmptyCatalog() {
        PageResult<PokemonSummary> result = catalogWith(List.of()).findPage(new PageQuery(0, 20));

        assertThat(result.items()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void keepsAllAttributes() {
        PokemonSummary first = catalog.findPage(new PageQuery(0, 1)).items().getFirst();

        assertThat(first).isEqualTo(pokemon(1, "bulbasaur"));
    }

    @Test
    void findsOnePokemonByPokedexNumber() {
        assertThat(catalog.findById(7)).contains(pokemon(7, "squirtle"));
    }

    @Test
    void findsNothingForAnUnknownPokedexNumber() {
        assertThat(catalog.findById(4242)).isEmpty();
    }

    private static PokemonSummary pokemon(int id, String name) {
        return new PokemonSummary(id, name, "https://example.org/%d.png".formatted(id), "Test Pokémon",
                new Weight(id * 10), List.of(new Ability("main-" + name, false), new Ability("hidden-" + name, true)));
    }
}

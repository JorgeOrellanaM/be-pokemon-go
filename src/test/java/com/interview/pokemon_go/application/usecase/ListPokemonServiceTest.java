package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.infrastructure.pokeapi.FakePokemonCatalogAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ListPokemonServiceTest {

    private final ListPokemonService service = new ListPokemonService(new FakePokemonCatalogAdapter());

    @Test
    void returnsTheRequestedPageFromTheCatalog() {
        PageResult<PokemonSummary> result = service.list(new PageQuery(0, 3));

        assertThat(result.items()).extracting(PokemonSummary::name)
                .containsExactly("bulbasaur", "ivysaur", "venusaur");
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.totalElements()).isEqualTo(10);
        assertThat(result.totalPages()).isEqualTo(4);
    }

    @Test
    void returnsEmptyItemsWhenPageIsBeyondTheEnd() {
        PageResult<PokemonSummary> result = service.list(new PageQuery(5, 10));

        assertThat(result.items()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(10);
    }

    @Test
    void rejectsNullQuery() {
        assertThatNullPointerException().isThrownBy(() -> service.list(null));
    }
}

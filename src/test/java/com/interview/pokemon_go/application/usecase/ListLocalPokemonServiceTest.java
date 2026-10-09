package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.out.FakePokemonCatalogAdapter;
import com.interview.pokemon_go.application.port.out.InMemoryLocalPokemonAdapter;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ListLocalPokemonServiceTest {

    private final InMemoryLocalPokemonAdapter local = new InMemoryLocalPokemonAdapter();
    private final ListLocalPokemonService service = new ListLocalPokemonService(local);

    @Test
    void returnsTheRequestedPageOfStoredPokemon() {
        FakePokemonCatalogAdapter catalog = new FakePokemonCatalogAdapter();
        List.of(25, 1, 4).forEach(id -> local.save(LocalPokemon.replicaOf(catalog.findById(id).orElseThrow())));

        PageResult<LocalPokemon> page = service.list(new PageQuery(0, 2));

        assertThat(page.items()).extracting(LocalPokemon::id).containsExactly(1, 4);
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void requiresAQueryAndAPort() {
        assertThatNullPointerException().isThrownBy(() -> service.list(null));
        assertThatNullPointerException().isThrownBy(() -> new ListLocalPokemonService(null));
    }
}

package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.application.port.out.PokemonCatalogPort;
import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PostgresPokemonCatalogAdapter implements PokemonCatalogPort {

    private final SpringDataPokemonRepository repository;

    public PostgresPokemonCatalogAdapter(SpringDataPokemonRepository repository) {
        this.repository = repository;
    }

    /**
     * Pages are sorted by Pokédex number, not by the generated database id, so the order matches the
     * Pokédex no matter in which order rows were inserted.
     * <p>
     * Mapping to the domain happens inside this read-only transaction: abilities are lazily
     * batch-loaded and {@code open-in-view} is disabled, so they cannot be loaded afterwards.
     * Database failures are translated to {@link ExternalServiceUnavailableException} (503).
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<PokemonSummary> findPage(PageQuery query) {
        try {
            Page<PokemonEntity> page = repository.findAll(
                    PageRequest.of(query.page(), query.size(), Sort.by("pokedexNumber")));
            return new PageResult<>(
                    page.map(PokemonPersistenceMapper::toDomain).getContent(),
                    query.page(),
                    query.size(),
                    page.getTotalElements());
        } catch (DataAccessException e) {
            throw new ExternalServiceUnavailableException("Pokemon database is unavailable", e);
        }
    }
}

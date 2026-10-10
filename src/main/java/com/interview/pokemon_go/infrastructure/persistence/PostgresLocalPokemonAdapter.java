package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.application.port.out.LocalPokemonPort;
import com.interview.pokemon_go.domain.exception.PokemonAlreadySyncedException;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonCustomization;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class PostgresLocalPokemonAdapter implements LocalPokemonPort {

    private static final Sort BY_POKEDEX_NUMBER = Sort.by("pokedexNumber");

    private final SpringDataPokemonRepository repository;

    PostgresLocalPokemonAdapter(SpringDataPokemonRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(int id) {
        return repository.existsByPokedexNumber(id);
    }

    /**
     * Checking {@link #existsById} before saving is not atomic, so two concurrent syncs of the same
     * Pokemon can both pass it. The unique {@code pokedex_number} constraint is what really prevents
     * the duplicate: the insert is flushed here so its violation surfaces in this method and is
     * reported as a conflict (409), not as a database error (500).
     */
    @Override
    @Transactional
    public LocalPokemon save(LocalPokemon pokemon) {
        try {
            return PokemonPersistenceMapper.toDomain(
                    repository.saveAndFlush(PokemonPersistenceMapper.toEntity(pokemon)));
        } catch (DataIntegrityViolationException e) {
            if (UniqueViolations.isUniqueViolation(e)) {
                throw new PokemonAlreadySyncedException(pokemon.id());
            }
            throw e;
        }
    }

    /**
     * Read-only transactions: open-in-view is off, so the lazy abilities and tags must be mapped
     * before the transaction ends.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<LocalPokemon> findById(int id) {
        return repository.findByPokedexNumber(id).map(PokemonPersistenceMapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<LocalPokemon> updateCustomization(int id, PokemonCustomization customization) {
        return repository.findByPokedexNumber(id).map(entity -> {
            entity.customize(customization.localizedName(), customization.region());
            replaceTags(entity, customization.tags());
            return PokemonPersistenceMapper.toDomain(entity);
        });
    }

    /**
     * Hibernate runs inserts before deletes when it flushes, so re-adding a tag the Pokemon already has
     * would break the unique (pokemon_id, name) key. The removals are flushed first; then the new list
     * is added in the requested order, which is the order tags are read back in.
     */
    private void replaceTags(PokemonEntity entity, List<String> tags) {
        entity.clearTags();
        repository.flush();
        tags.forEach(entity::addTag);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<LocalPokemon> findPage(PageQuery query) {
        Page<PokemonEntity> page = repository.findAll(PageRequest.of(query.page(), query.size(), BY_POKEDEX_NUMBER));
        return new PageResult<>(page.map(PokemonPersistenceMapper::toDomain).getContent(),
                query.page(), query.size(), page.getTotalElements());
    }
}

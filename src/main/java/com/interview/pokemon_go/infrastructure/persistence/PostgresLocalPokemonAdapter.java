package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.application.port.out.LocalPokemonPort;
import com.interview.pokemon_go.domain.exception.PokemonAlreadySyncedException;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
            if (isUniqueViolation(e)) {
                throw new PokemonAlreadySyncedException(pokemon.id());
            }
            throw e;
        }
    }

    /**
     * Only a unique-key violation means "already stored"; any other integrity error (e.g. a value too
     * long for its column) is our fault and must stay a 500.
     */
    private static boolean isUniqueViolation(DataIntegrityViolationException e) {
        return e.getCause() instanceof ConstraintViolationException violation
                && violation.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE;
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
    @Transactional(readOnly = true)
    public PageResult<LocalPokemon> findPage(PageQuery query) {
        Page<PokemonEntity> page = repository.findAll(PageRequest.of(query.page(), query.size(), BY_POKEDEX_NUMBER));
        return new PageResult<>(page.map(PokemonPersistenceMapper::toDomain).getContent(),
                query.page(), query.size(), page.getTotalElements());
    }
}

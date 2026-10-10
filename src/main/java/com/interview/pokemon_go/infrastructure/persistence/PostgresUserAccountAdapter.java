package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.application.port.out.UserAccountPort;
import com.interview.pokemon_go.domain.exception.UsernameAlreadyExistsException;
import com.interview.pokemon_go.domain.model.UserAccount;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Component
public class PostgresUserAccountAdapter implements UserAccountPort {

    private final SpringDataUserRepository repository;

    PostgresUserAccountAdapter(SpringDataUserRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findByUsername(String username) {
        return repository.findByUsername(username).map(UserPersistenceMapper::toDomain);
    }

    /**
     * Two concurrent registrations of the same username can both pass the use case's existence check.
     * The unique {@code username} constraint is what really prevents the duplicate: the insert is
     * flushed here so its violation is reported as a conflict (409), not as a database error (500).
     */
    @Override
    @Transactional
    public UserAccount save(UserAccount account) {
        try {
            return UserPersistenceMapper.toDomain(
                    repository.saveAndFlush(UserPersistenceMapper.toEntity(account)));
        } catch (DataIntegrityViolationException e) {
            if (UniqueViolations.isUniqueViolation(e)) {
                throw new UsernameAlreadyExistsException(account.username());
            }
            throw e;
        }
    }
}

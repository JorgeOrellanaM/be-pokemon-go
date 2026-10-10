package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.exception.UsernameAlreadyExistsException;
import com.interview.pokemon_go.domain.model.UserAccount;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hand-written, in-memory fake of {@link UserAccountPort} for use-case tests. Verified against the same
 * {@link UserAccountPortContractTest} as the database adapter.
 */
public class InMemoryUserAccountAdapter implements UserAccountPort {

    private final Map<String, UserAccount> accounts = new HashMap<>();

    @Override
    public boolean existsByUsername(String username) {
        return accounts.containsKey(username);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(accounts.get(username));
    }

    @Override
    public UserAccount save(UserAccount account) {
        if (accounts.putIfAbsent(account.username(), account) != null) {
            throw new UsernameAlreadyExistsException(account.username());
        }
        return account;
    }
}

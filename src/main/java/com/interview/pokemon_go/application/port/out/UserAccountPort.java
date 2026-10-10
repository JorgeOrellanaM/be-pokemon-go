package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.UserAccount;

import java.util.Optional;

/**
 * The store of registered users, identified by their normalized username. Saving a username that is
 * already stored throws {@link com.interview.pokemon_go.domain.exception.UsernameAlreadyExistsException}.
 * The shared contract is verified by {@code UserAccountPortContractTest}.
 */
public interface UserAccountPort {

    boolean existsByUsername(String username);

    Optional<UserAccount> findByUsername(String username);

    UserAccount save(UserAccount account);
}

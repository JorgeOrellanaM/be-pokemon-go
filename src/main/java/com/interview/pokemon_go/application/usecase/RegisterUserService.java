package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.RegisterUserUseCase;
import com.interview.pokemon_go.application.port.out.PasswordHasherPort;
import com.interview.pokemon_go.application.port.out.UserAccountPort;
import com.interview.pokemon_go.domain.exception.UsernameAlreadyExistsException;
import com.interview.pokemon_go.domain.model.Registration;
import com.interview.pokemon_go.domain.model.UserAccount;

import java.util.Objects;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserAccountPort accounts;
    private final PasswordHasherPort hasher;

    public RegisterUserService(UserAccountPort accounts, PasswordHasherPort hasher) {
        this.accounts = Objects.requireNonNull(accounts, "accounts must not be null");
        this.hasher = Objects.requireNonNull(hasher, "hasher must not be null");
    }

    /**
     * The existence check skips the costly hash for the common duplicate case. It is not atomic, so the
     * store still rejects a concurrent duplicate with the same exception.
     */
    @Override
    public UserAccount register(Registration registration) {
        Objects.requireNonNull(registration, "registration must not be null");
        if (accounts.existsByUsername(registration.username())) {
            throw new UsernameAlreadyExistsException(registration.username());
        }
        return accounts.save(new UserAccount(registration.username(), hasher.hash(registration.password())));
    }
}

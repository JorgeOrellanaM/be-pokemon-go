package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.in.AuthenticateUserUseCase;
import com.interview.pokemon_go.application.port.out.AccessTokenIssuerPort;
import com.interview.pokemon_go.application.port.out.PasswordHasherPort;
import com.interview.pokemon_go.application.port.out.UserAccountPort;
import com.interview.pokemon_go.domain.exception.InvalidCredentialsException;
import com.interview.pokemon_go.domain.model.AccessToken;
import com.interview.pokemon_go.domain.model.Credentials;

import java.util.Objects;

public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserAccountPort accounts;
    private final PasswordHasherPort hasher;
    private final AccessTokenIssuerPort tokens;

    public AuthenticateUserService(UserAccountPort accounts, PasswordHasherPort hasher,
                                   AccessTokenIssuerPort tokens) {
        this.accounts = Objects.requireNonNull(accounts, "accounts must not be null");
        this.hasher = Objects.requireNonNull(hasher, "hasher must not be null");
        this.tokens = Objects.requireNonNull(tokens, "tokens must not be null");
    }

    /**
     * An unknown username and a wrong password fail in the same way, so the answer never reveals which
     * usernames exist.
     */
    @Override
    public AccessToken authenticate(Credentials credentials) {
        Objects.requireNonNull(credentials, "credentials must not be null");
        return accounts.findByUsername(credentials.username())
                .filter(account -> hasher.matches(credentials.password(), account.passwordHash()))
                .map(account -> tokens.issue(account.username()))
                .orElseThrow(InvalidCredentialsException::new);
    }
}

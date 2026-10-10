package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.AccessToken;

import java.time.Instant;

/**
 * Issues readable fake tokens ({@code token-for:<username>}) with a fixed expiry.
 */
public class FakeAccessTokenIssuer implements AccessTokenIssuerPort {

    public static final Instant EXPIRES_AT = Instant.parse("2030-01-01T00:00:00Z");

    @Override
    public AccessToken issue(String username) {
        return new AccessToken("token-for:" + username, EXPIRES_AT);
    }
}

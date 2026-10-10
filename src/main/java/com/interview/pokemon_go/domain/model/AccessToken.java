package com.interview.pokemon_go.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * A signed token proving who the caller is, valid until {@code expiresAt}.
 */
public record AccessToken(String value, Instant expiresAt) {

    public AccessToken {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}

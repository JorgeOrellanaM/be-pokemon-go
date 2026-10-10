package com.interview.pokemon_go.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * {@code jwt.*} settings for the access tokens this service issues and verifies. Validated at startup,
 * so a weak or missing secret stops the application instead of producing forgeable tokens.
 */
@ConfigurationProperties("jwt")
public record JwtProperties(String secret, Duration ttl, String issuer) {

    /**
     * HS256 signs with a 256-bit key; RFC 7518 forbids a shorter one.
     */
    public static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        Objects.requireNonNull(secret, "jwt.secret must be set");
        Objects.requireNonNull(ttl, "jwt.ttl must be set");
        Objects.requireNonNull(issuer, "jwt.issuer must be set");
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalArgumentException("jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes");
        }
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("jwt.ttl must be positive");
        }
        if (issuer.isBlank()) {
            throw new IllegalArgumentException("jwt.issuer must not be blank");
        }
    }
}

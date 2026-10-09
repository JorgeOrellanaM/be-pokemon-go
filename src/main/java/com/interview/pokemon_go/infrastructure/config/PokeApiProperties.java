package com.interview.pokemon_go.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

/**
 * {@code pokeapi.*} settings. Validated at startup, so a missing or wrong value stops the
 * application instead of failing on the first request.
 */
@ConfigurationProperties("pokeapi")
public record PokeApiProperties(URI baseUrl, Duration connectTimeout, Duration readTimeout) {

    public PokeApiProperties {
        Objects.requireNonNull(baseUrl, "pokeapi.base-url must be set");
        requirePositive(connectTimeout, "pokeapi.connect-timeout");
        requirePositive(readTimeout, "pokeapi.read-timeout");
    }

    private static void requirePositive(Duration timeout, String name) {
        Objects.requireNonNull(timeout, name + " must be set");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}

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
public record PokeApiProperties(URI baseUrl, Duration connectTimeout, Duration readTimeout, Cache cache) {

    public PokeApiProperties {
        Objects.requireNonNull(baseUrl, "pokeapi.base-url must be set");
        requirePositive(connectTimeout, "pokeapi.connect-timeout");
        requirePositive(readTimeout, "pokeapi.read-timeout");
        Objects.requireNonNull(cache, "pokeapi.cache.* must be set");
    }

    /**
     * {@code pokeapi.cache.*}: how long a PokeAPI response is reused and how many entries each cache
     * keeps.
     */
    public record Cache(Duration ttl, int maxEntries) {

        public Cache {
            requirePositive(ttl, "pokeapi.cache.ttl");
            if (maxEntries <= 0) {
                throw new IllegalArgumentException("pokeapi.cache.max-entries must be positive");
            }
        }
    }

    private static void requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name + " must be set");
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}

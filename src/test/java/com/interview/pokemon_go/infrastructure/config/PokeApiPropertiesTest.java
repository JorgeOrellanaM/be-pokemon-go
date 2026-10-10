package com.interview.pokemon_go.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PokeApiPropertiesTest {

    private static final URI BASE_URL = URI.create("https://pokeapi.co/api/v2");
    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final PokeApiProperties.Cache CACHE = new PokeApiProperties.Cache(Duration.ofHours(24), 2000);

    @Test
    void requiresEverySetting() {
        assertThatNullPointerException().isThrownBy(() -> new PokeApiProperties(null, TIMEOUT, TIMEOUT, CACHE));
        assertThatNullPointerException().isThrownBy(() -> new PokeApiProperties(BASE_URL, null, TIMEOUT, CACHE));
        assertThatNullPointerException().isThrownBy(() -> new PokeApiProperties(BASE_URL, TIMEOUT, null, CACHE));
        assertThatNullPointerException().isThrownBy(() -> new PokeApiProperties(BASE_URL, TIMEOUT, TIMEOUT, null));
        assertThatNullPointerException().isThrownBy(() -> new PokeApiProperties.Cache(null, 2000));
    }

    @Test
    void rejectsNonPositiveTimeouts() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PokeApiProperties(BASE_URL, Duration.ZERO, TIMEOUT, CACHE));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PokeApiProperties(BASE_URL, TIMEOUT, Duration.ofSeconds(-1), CACHE));
    }

    @Test
    void rejectsANonPositiveCacheTtl() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PokeApiProperties.Cache(Duration.ZERO, 2000));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PokeApiProperties.Cache(Duration.ofMinutes(-1), 2000));
    }

    @Test
    void rejectsANonPositiveCacheSize() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PokeApiProperties.Cache(Duration.ofHours(1), 0));
        assertThatIllegalArgumentException().isThrownBy(() -> new PokeApiProperties.Cache(Duration.ofHours(1), -5));
    }
}

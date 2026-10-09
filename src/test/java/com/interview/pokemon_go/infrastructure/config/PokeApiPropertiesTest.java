package com.interview.pokemon_go.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PokeApiPropertiesTest {

    private static final URI BASE_URL = URI.create("https://pokeapi.co/api/v2");
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    @Test
    void requiresEverySetting() {
        assertThatNullPointerException().isThrownBy(() -> new PokeApiProperties(null, TIMEOUT, TIMEOUT));
        assertThatNullPointerException().isThrownBy(() -> new PokeApiProperties(BASE_URL, null, TIMEOUT));
        assertThatNullPointerException().isThrownBy(() -> new PokeApiProperties(BASE_URL, TIMEOUT, null));
    }

    @Test
    void rejectsNonPositiveTimeouts() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PokeApiProperties(BASE_URL, Duration.ZERO, TIMEOUT));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new PokeApiProperties(BASE_URL, TIMEOUT, Duration.ofSeconds(-1)));
    }
}

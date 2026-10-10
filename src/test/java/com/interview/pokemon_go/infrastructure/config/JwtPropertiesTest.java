package com.interview.pokemon_go.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class JwtPropertiesTest {

    private static final String SECRET = "s".repeat(JwtProperties.MIN_SECRET_BYTES);
    private static final Duration TTL = Duration.ofHours(1);

    @Test
    void requiresEverySetting() {
        assertThatNullPointerException().isThrownBy(() -> new JwtProperties(null, TTL, "pokemon-go"));
        assertThatNullPointerException().isThrownBy(() -> new JwtProperties(SECRET, null, "pokemon-go"));
        assertThatNullPointerException().isThrownBy(() -> new JwtProperties(SECRET, TTL, null));
        assertThatIllegalArgumentException().isThrownBy(() -> new JwtProperties(SECRET, TTL, " "));
    }

    /**
     * HS256 needs a key of at least 256 bits; a shorter one would make tokens easy to forge.
     */
    @Test
    void rejectsASecretShorterThan256Bits() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JwtProperties("s".repeat(JwtProperties.MIN_SECRET_BYTES - 1), TTL, "pokemon-go"));
    }

    @Test
    void rejectsANonPositiveTtl() {
        assertThatIllegalArgumentException().isThrownBy(() -> new JwtProperties(SECRET, Duration.ZERO, "pokemon-go"));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JwtProperties(SECRET, Duration.ofMinutes(-1), "pokemon-go"));
    }
}

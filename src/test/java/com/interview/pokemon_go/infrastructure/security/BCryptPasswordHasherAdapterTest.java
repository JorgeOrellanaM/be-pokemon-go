package com.interview.pokemon_go.infrastructure.security;

import com.interview.pokemon_go.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BCryptPasswordHasherAdapterTest {

    private final BCryptPasswordHasherAdapter hasher =
            new BCryptPasswordHasherAdapter(new SecurityConfig().passwordEncoder());

    @Test
    void hashesWithASaltSoEqualPasswordsGetDifferentHashes() {
        String first = hasher.hash("Pokemon123!");
        String second = hasher.hash("Pokemon123!");

        assertThat(first).startsWith("$2").doesNotContain("Pokemon123!").isNotEqualTo(second);
    }

    @Test
    void matchesOnlyTheOriginalPassword() {
        String hash = hasher.hash("Pokemon123!");

        assertThat(hasher.matches("Pokemon123!", hash)).isTrue();
        assertThat(hasher.matches("pokemon123!", hash)).isFalse();
    }
}

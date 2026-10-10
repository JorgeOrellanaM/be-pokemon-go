package com.interview.pokemon_go.infrastructure.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Request bodies may end up in logs (a debug statement, a framework message); the raw password must
 * never be printed.
 */
class AuthRequestDTOTest {

    @Test
    void registerRequestNeverPrintsThePassword() {
        assertThat(new RegisterRequestDTO("ash", "Pokemon123!").toString())
                .contains("ash").doesNotContain("Pokemon123!");
    }

    @Test
    void loginRequestNeverPrintsThePassword() {
        assertThat(new LoginRequestDTO("ash", "Pokemon123!").toString())
                .contains("ash").doesNotContain("Pokemon123!");
    }
}

package com.interview.pokemon_go.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void invalidPageQueryIsInvalidInput() {
        assertThat(new InvalidPageQueryException("bad").category()).isEqualTo(ErrorCategory.INVALID_INPUT);
    }

    @Test
    void externalServiceUnavailableIsUnavailable() {
        assertThat(new ExternalServiceUnavailableException("down", new RuntimeException()).category())
                .isEqualTo(ErrorCategory.UNAVAILABLE);
    }
}

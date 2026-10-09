package com.interview.pokemon_go.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void invalidPageQueryIsInvalidInput() {
        assertThat(new InvalidPageQueryException("bad").category()).isEqualTo(ErrorCategory.INVALID_INPUT);
    }

    @Test
    void domainValidationIsInvalidInput() {
        assertThat(new DomainValidationException("bad").category()).isEqualTo(ErrorCategory.INVALID_INPUT);
    }

    @Test
    void pokemonNotFoundIsNotFoundWithFriendlyMessage() {
        PokemonNotFoundException ex = new PokemonNotFoundException(99999);

        assertThat(ex.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Pokemon with id 99999 was not found");
    }

    @Test
    void externalServiceUnavailableIsUnavailable() {
        assertThat(new ExternalServiceUnavailableException("down", new RuntimeException()).category())
                .isEqualTo(ErrorCategory.UNAVAILABLE);
    }
}

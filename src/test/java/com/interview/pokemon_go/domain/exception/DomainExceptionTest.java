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
    void pokemonAlreadySyncedIsConflictWithFriendlyMessage() {
        PokemonAlreadySyncedException ex = new PokemonAlreadySyncedException(25);

        assertThat(ex.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("Pokemon with id 25 is already synced");
    }

    @Test
    void externalServiceUnavailableIsUnavailable() {
        assertThat(new ExternalServiceUnavailableException("down", new RuntimeException()).category())
                .isEqualTo(ErrorCategory.UNAVAILABLE);
    }

    @Test
    void externalServiceUnavailableCanReportAnInconsistencyWithoutCause() {
        ExternalServiceUnavailableException ex = new ExternalServiceUnavailableException("inconsistent");

        assertThat(ex.category()).isEqualTo(ErrorCategory.UNAVAILABLE);
        assertThat(ex).hasMessage("inconsistent").hasNoCause();
    }
}

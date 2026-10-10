package com.interview.pokemon_go.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void invalidPageQueryIsInvalidInput() {
        assertThat(new InvalidPageQueryException("bad").category()).isEqualTo(ErrorCategory.INVALID_INPUT);
    }

    @Test
    void domainValidationIsInvalidInput() {
        assertThat(new DomainValidationException("bad").category()).isEqualTo(ErrorCategory.INVALID_INPUT);
        assertThat(new DomainValidationException("bad").violations()).isEmpty();
    }

    @Test
    void domainValidationCarriesFieldViolations() {
        DomainValidationException ex = new DomainValidationException("bad",
                List.of(new FieldViolation("region", "must be at most 100 characters")));

        assertThat(ex.violations()).containsExactly(new FieldViolation("region", "must be at most 100 characters"));
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
    void usernameAlreadyExistsIsConflictWithFriendlyMessage() {
        UsernameAlreadyExistsException ex = new UsernameAlreadyExistsException("ash");

        assertThat(ex.category()).isEqualTo(ErrorCategory.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("The username 'ash' is already taken");
    }

    /**
     * The same message whatever was wrong, so a login attempt never reveals which usernames exist.
     */
    @Test
    void invalidCredentialsIsUnauthenticatedWithAGenericMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        assertThat(ex.category()).isEqualTo(ErrorCategory.UNAUTHENTICATED);
        assertThat(ex.getMessage()).isEqualTo("Invalid username or password.");
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

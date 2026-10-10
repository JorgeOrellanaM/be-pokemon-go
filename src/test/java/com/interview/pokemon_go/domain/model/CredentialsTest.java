package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.FieldViolation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class CredentialsTest {

    @Test
    void normalizesTheUsernameLikeRegistration() {
        Credentials credentials = new Credentials(" Ash ", "Pokemon123!");

        assertThat(credentials.username()).isEqualTo("ash");
        assertThat(credentials.password()).isEqualTo("Pokemon123!");
    }

    /**
     * A login never reveals the registration rules: a value that could never be registered simply
     * does not match any account.
     */
    @Test
    void doesNotCheckTheRegistrationFormat() {
        Credentials credentials = new Credentials("a b", "x");

        assertThat(credentials.username()).isEqualTo("a b");
    }

    @Test
    void requiresBothFields() {
        DomainValidationException ex = catchThrowableOfType(DomainValidationException.class,
                () -> new Credentials("  ", null));

        assertThat(ex).hasMessage("Please check the highlighted fields.");
        assertThat(ex.violations()).containsExactly(
                new FieldViolation("username", "must not be blank"),
                new FieldViolation("password", "must not be blank"));
    }

    @Test
    void neverPrintsThePassword() {
        assertThat(new Credentials("ash", "Pokemon123!").toString()).contains("ash").doesNotContain("Pokemon123!");
    }
}

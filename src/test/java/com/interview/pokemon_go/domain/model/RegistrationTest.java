package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.FieldViolation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class RegistrationTest {

    private static final String PASSWORD = "Pokemon123!";

    @Test
    void keepsValidValues() {
        Registration registration = new Registration("ash.ketchum_1-0", PASSWORD);

        assertThat(registration.username()).isEqualTo("ash.ketchum_1-0");
        assertThat(registration.password()).isEqualTo(PASSWORD);
    }

    @Test
    void normalizesTheUsernameSoItIsCaseInsensitive() {
        assertThat(new Registration("  Ash ", PASSWORD).username()).isEqualTo("ash");
    }

    @Test
    void keepsThePasswordExactlyAsTyped() {
        assertThat(new Registration("ash", " pass word ").password()).isEqualTo(" pass word ");
    }

    @Test
    void acceptsValuesAtTheirLimits() {
        new Registration("a".repeat(Registration.MIN_USERNAME_LENGTH), "p".repeat(Registration.MIN_PASSWORD_LENGTH));
        new Registration("a".repeat(Registration.MAX_USERNAME_LENGTH), "p".repeat(Registration.MAX_PASSWORD_BYTES));
    }

    @Test
    void reportsEveryInvalidFieldAtOnce() {
        DomainValidationException ex = invalid(null, " ");

        assertThat(ex).hasMessage("Please check the highlighted fields.");
        assertThat(ex.violations()).containsExactly(
                new FieldViolation("username", "must not be blank"),
                new FieldViolation("password", "must not be blank"));
    }

    @Test
    void rejectsUsernamesOfTheWrongLength() {
        assertThat(invalid("ab", PASSWORD).violations())
                .containsExactly(new FieldViolation("username", "must be between 3 and 30 characters"));
        assertThat(invalid("a".repeat(31), PASSWORD).violations())
                .containsExactly(new FieldViolation("username", "must be between 3 and 30 characters"));
    }

    @Test
    void rejectsUsernamesWithOtherCharacters() {
        assertThat(invalid("ash ketchum", PASSWORD).violations()).containsExactly(
                new FieldViolation("username", "may only contain letters, digits, '.', '_' and '-'"));
        assertThat(invalid("pikachu⚡", PASSWORD).violations()).hasSize(1);
    }

    @Test
    void rejectsPasswordsOfTheWrongLength() {
        assertThat(invalid("ash", "short").violations())
                .containsExactly(new FieldViolation("password", "must be between 8 and 72 characters"));
        assertThat(invalid("ash", "p".repeat(73)).violations()).hasSize(1);
    }

    /**
     * BCrypt only reads the first 72 bytes, so the limit counts UTF-8 bytes, not characters.
     */
    @Test
    void measuresThePasswordLimitInBytes() {
        String thirtySevenTwoByteCharacters = "é".repeat(37);

        assertThat(invalid("ash", thirtySevenTwoByteCharacters).violations()).hasSize(1);
    }

    @Test
    void neverPrintsThePassword() {
        assertThat(new Registration("ash", PASSWORD).toString()).contains("ash").doesNotContain(PASSWORD);
    }

    private static DomainValidationException invalid(String username, String password) {
        return catchThrowableOfType(DomainValidationException.class, () -> new Registration(username, password));
    }
}

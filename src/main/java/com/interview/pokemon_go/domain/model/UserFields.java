package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.FieldViolation;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Rules shared by {@link Registration} and {@link Credentials}, so a user always logs in with the same
 * username they registered.
 */
final class UserFields {

    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String BLANK = "must not be blank";
    static final String MASKED = "***";

    private UserFields() {
    }

    /**
     * Usernames are case-insensitive: "Ash" and " ash " are the same account. Blank means absent.
     */
    static String normalizeUsername(String username) {
        return Optional.ofNullable(username)
                .map(String::strip)
                .filter(stripped -> !stripped.isEmpty())
                .map(stripped -> stripped.toLowerCase(Locale.ROOT))
                .orElse(null);
    }

    /**
     * Reports a missing value and tells the caller whether further rules can be checked.
     */
    static boolean requirePresent(String value, String field, List<FieldViolation> violations) {
        if (value == null || value.isBlank()) {
            violations.add(new FieldViolation(field, BLANK));
            return false;
        }
        return true;
    }
}

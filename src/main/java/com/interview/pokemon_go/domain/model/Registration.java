package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.FieldViolation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A sign-up request. Every invalid field is reported at once as a {@link DomainValidationException},
 * because the values come straight from API clients.
 */
public record Registration(String username, String password) {

    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 30;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_BYTES = 72;

    private static final Pattern USERNAME_CHARACTERS = Pattern.compile("[a-z0-9._-]+");

    private static final String USERNAME_LENGTH =
            "must be between " + MIN_USERNAME_LENGTH + " and " + MAX_USERNAME_LENGTH + " characters";
    private static final String USERNAME_FORMAT = "may only contain letters, digits, '.', '_' and '-'";
    private static final String PASSWORD_LENGTH =
            "must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_BYTES + " characters";

    /**
     * The username is normalized (see {@link UserFields#normalizeUsername}); the password is kept exactly
     * as typed.
     */
    public Registration {
        username = UserFields.normalizeUsername(username);
        List<FieldViolation> violations = new ArrayList<>();
        if (UserFields.requirePresent(username, UserFields.USERNAME, violations)) {
            checkUsername(username, violations);
        }
        if (UserFields.requirePresent(password, UserFields.PASSWORD, violations)) {
            checkPassword(password, violations);
        }
        if (!violations.isEmpty()) {
            throw new DomainValidationException(DomainValidationException.CHECK_FIELDS, violations);
        }
    }

    private static void checkUsername(String username, List<FieldViolation> violations) {
        if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            violations.add(new FieldViolation(UserFields.USERNAME, USERNAME_LENGTH));
        } else if (!USERNAME_CHARACTERS.matcher(username).matches()) {
            violations.add(new FieldViolation(UserFields.USERNAME, USERNAME_FORMAT));
        }
    }

    /**
     * BCrypt silently ignores everything after the first 72 bytes, so a longer password would give a
     * false sense of strength. The limit counts UTF-8 bytes; for plain ASCII that equals characters.
     */
    private static void checkPassword(String password, List<FieldViolation> violations) {
        if (password.length() < MIN_PASSWORD_LENGTH
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            violations.add(new FieldViolation(UserFields.PASSWORD, PASSWORD_LENGTH));
        }
    }

    @Override
    public String toString() {
        return "Registration[username=" + username + ", password=" + UserFields.MASKED + "]";
    }
}

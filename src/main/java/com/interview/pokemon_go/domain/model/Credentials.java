package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.FieldViolation;

import java.util.ArrayList;
import java.util.List;

/**
 * A login attempt. Only presence is checked: the registration rules are not repeated here, so a value
 * that could never be registered simply matches no account (401) instead of revealing the rules.
 */
public record Credentials(String username, String password) {

    public Credentials {
        username = UserFields.normalizeUsername(username);
        List<FieldViolation> violations = new ArrayList<>();
        UserFields.requirePresent(username, UserFields.USERNAME, violations);
        UserFields.requirePresent(password, UserFields.PASSWORD, violations);
        if (!violations.isEmpty()) {
            throw new DomainValidationException(DomainValidationException.CHECK_FIELDS, violations);
        }
    }

    @Override
    public String toString() {
        return "Credentials[username=" + username + ", password=" + UserFields.MASKED + "]";
    }
}

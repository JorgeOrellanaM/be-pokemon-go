package com.interview.pokemon_go.domain.model;

import java.util.Objects;

/**
 * A registered user. Only the password hash is ever stored; the raw password never leaves the use case
 * that received it.
 */
public record UserAccount(String username, String passwordHash) {

    public UserAccount {
        requireText(username, "username");
        requireText(passwordHash, "passwordHash");
    }

    private static void requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    @Override
    public String toString() {
        return "UserAccount[username=" + username + ", passwordHash=" + UserFields.MASKED + "]";
    }
}

package com.interview.pokemon_go.domain.exception;

import java.util.Objects;

/**
 * One invalid input field and a user-friendly explanation, e.g. {@code region}: "must be at most 100
 * characters".
 */
public record FieldViolation(String field, String message) {

    public FieldViolation {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}

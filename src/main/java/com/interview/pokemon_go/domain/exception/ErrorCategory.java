package com.interview.pokemon_go.domain.exception;

/**
 * What kind of failure a {@link DomainException} represents. Outer layers map each category to their
 * own protocol (e.g. an HTTP status), so a new exception only has to pick a category.
 */
public enum ErrorCategory {
    INVALID_INPUT,
    NOT_FOUND,
    CONFLICT,
    UNAUTHENTICATED,
    FORBIDDEN,
    UNAVAILABLE
}

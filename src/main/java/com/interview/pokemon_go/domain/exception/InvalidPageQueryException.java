package com.interview.pokemon_go.domain.exception;

public class InvalidPageQueryException extends DomainException {

    public InvalidPageQueryException(String message) {
        super(message);
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.INVALID_INPUT;
    }
}

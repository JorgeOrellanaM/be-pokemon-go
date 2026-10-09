package com.interview.pokemon_go.domain.exception;

public class DomainValidationException extends DomainException {

    public DomainValidationException(String message) {
        super(message);
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.INVALID_INPUT;
    }
}

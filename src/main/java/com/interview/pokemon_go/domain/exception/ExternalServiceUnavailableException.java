package com.interview.pokemon_go.domain.exception;

public class ExternalServiceUnavailableException extends DomainException {

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.UNAVAILABLE;
    }
}

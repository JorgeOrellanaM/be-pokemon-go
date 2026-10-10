package com.interview.pokemon_go.domain.exception;

public class UsernameAlreadyExistsException extends DomainException {

    private static final String MESSAGE = "The username '%s' is already taken";

    public UsernameAlreadyExistsException(String username) {
        super(MESSAGE.formatted(username));
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.CONFLICT;
    }
}

package com.interview.pokemon_go.domain.exception;

/**
 * A failed login. The message is the same for an unknown username and a wrong password, so a login
 * attempt never reveals which usernames exist.
 */
public class InvalidCredentialsException extends DomainException {

    private static final String MESSAGE = "Invalid username or password.";

    public InvalidCredentialsException() {
        super(MESSAGE);
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.UNAUTHENTICATED;
    }
}

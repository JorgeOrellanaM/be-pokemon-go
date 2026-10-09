package com.interview.pokemon_go.domain.exception;

public class PokemonAlreadySyncedException extends DomainException {

    private static final String MESSAGE = "Pokemon with id %d is already synced";

    public PokemonAlreadySyncedException(int id) {
        super(MESSAGE.formatted(id));
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.CONFLICT;
    }
}

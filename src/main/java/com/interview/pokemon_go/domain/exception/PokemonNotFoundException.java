package com.interview.pokemon_go.domain.exception;

public class PokemonNotFoundException extends DomainException {

    private static final String MESSAGE = "Pokemon with id %d was not found";

    public PokemonNotFoundException(int id) {
        super(MESSAGE.formatted(id));
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.NOT_FOUND;
    }
}

package com.interview.pokemon_go.infrastructure.web;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * What the client is told about an error, before request data, the timestamp and the errorId are added.
 */
record ApiError(HttpStatus status, String message, List<FieldErrorDTO> errors) {

    ApiError {
        errors = List.copyOf(errors);
    }

    static ApiError of(HttpStatus status, String message) {
        return new ApiError(status, message, List.of());
    }
}

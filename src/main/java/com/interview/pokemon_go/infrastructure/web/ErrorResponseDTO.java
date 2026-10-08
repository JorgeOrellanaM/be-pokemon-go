package com.interview.pokemon_go.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

public record ErrorResponseDTO(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        String errorId,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldErrorDTO> errors) {
}

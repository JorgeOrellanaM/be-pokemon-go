package com.interview.pokemon_go.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * The single envelope for every API response: {@code data} on success, {@code error} on failure.
 * The unused key is left out of the JSON.
 */
public record ApiResponseDTO<T>(
        boolean success,
        @JsonInclude(JsonInclude.Include.NON_NULL) T data,
        @JsonInclude(JsonInclude.Include.NON_NULL) ErrorResponseDTO error) {

    public ApiResponseDTO {
        if (success == (error != null)) {
            throw new IllegalArgumentException("a response has an error if and only if it is not successful");
        }
        if (data != null && error != null) {
            throw new IllegalArgumentException("a response cannot carry both data and an error");
        }
    }

    public static <T> ApiResponseDTO<T> ok(T data) {
        return new ApiResponseDTO<>(true, Objects.requireNonNull(data, "data"), null);
    }

    public static ApiResponseDTO<Void> failure(ErrorResponseDTO error) {
        return new ApiResponseDTO<>(false, null, Objects.requireNonNull(error, "error"));
    }
}

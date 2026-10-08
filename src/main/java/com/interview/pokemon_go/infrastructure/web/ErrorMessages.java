package com.interview.pokemon_go.infrastructure.web;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Every user-facing error text of the API, in one place. None of them mention exceptions, Java types
 * or framework internals.
 */
final class ErrorMessages {

    static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";
    static final String SERVICE_UNAVAILABLE = "The service is temporarily unavailable. Please try again later.";
    static final String INVALID_CHARACTERS = "The request contains invalid characters. Please check it and try again.";

    private static final String INVALID_PARAMETER = "Please check the '%s' parameter.";
    private static final String DEFAULT_CLIENT_ERROR =
            "The request could not be processed. Please check it and try again.";

    private static final Map<HttpStatus, String> BY_STATUS = Map.of(
            HttpStatus.BAD_REQUEST, "The request is invalid. Please check it and try again.",
            HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource.",
            HttpStatus.FORBIDDEN, "You do not have permission to access this resource.",
            HttpStatus.NOT_FOUND, "The requested resource was not found.",
            HttpStatus.METHOD_NOT_ALLOWED, "This operation is not supported for this resource.",
            HttpStatus.NOT_ACCEPTABLE, "The requested response format is not supported.",
            HttpStatus.CONFLICT, "The request conflicts with the current state of the resource.",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE, "The request format is not supported.",
            HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE);

    private static final Set<Class<?>> WHOLE_NUMBER_TYPES =
            Set.of(int.class, Integer.class, long.class, Long.class, short.class, Short.class);
    private static final Set<Class<?>> DECIMAL_TYPES =
            Set.of(double.class, Double.class, float.class, Float.class, BigDecimal.class);

    private ErrorMessages() {
    }

    /**
     * Generic message for a status, used whenever the original exception message is not meant for
     * end users (framework and infrastructure errors).
     */
    static String forStatus(HttpStatus status) {
        return Optional.ofNullable(BY_STATUS.get(status))
                .orElse(status.is4xxClientError() ? DEFAULT_CLIENT_ERROR : UNEXPECTED_ERROR);
    }

    static String invalidParameter(String name) {
        return INVALID_PARAMETER.formatted(name);
    }

    /**
     * Describes the expected value in plain words, never as a Java type.
     */
    static String expectedValue(Class<?> type) {
        if (WHOLE_NUMBER_TYPES.contains(type)) {
            return "must be a whole number";
        }
        if (DECIMAL_TYPES.contains(type)) {
            return "must be a number";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "must be true or false";
        }
        return "has an invalid value";
    }
}

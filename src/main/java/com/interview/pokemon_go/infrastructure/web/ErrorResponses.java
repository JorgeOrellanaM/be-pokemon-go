package com.interview.pokemon_go.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Builds every error response of the API and logs it. Technical details (exception messages, stack
 * traces) go only to the log; the client receives a user-friendly message and an {@code errorId}
 * that points to the matching log line.
 */
final class ErrorResponses {

    static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please try again later.";
    static final String SERVICE_UNAVAILABLE = "The service is temporarily unavailable. Please try again later.";

    private static final Logger log = LoggerFactory.getLogger(ErrorResponses.class);

    private static final String DEFAULT_CLIENT_ERROR =
            "The request could not be processed. Please check it and try again.";

    private static final Map<HttpStatus, String> FRIENDLY_MESSAGES = Map.of(
            HttpStatus.BAD_REQUEST, "The request is invalid. Please check it and try again.",
            HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource.",
            HttpStatus.FORBIDDEN, "You do not have permission to access this resource.",
            HttpStatus.NOT_FOUND, "The requested resource was not found.",
            HttpStatus.METHOD_NOT_ALLOWED, "This operation is not supported for this resource.",
            HttpStatus.NOT_ACCEPTABLE, "The requested response format is not supported.",
            HttpStatus.CONFLICT, "The request conflicts with the current state of the resource.",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE, "The request format is not supported.",
            HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE);

    private ErrorResponses() {
    }

    /**
     * Generic message for a status, used whenever the original exception message is not meant for
     * end users (framework and infrastructure errors).
     */
    static String friendlyMessage(HttpStatus status) {
        return Optional.ofNullable(FRIENDLY_MESSAGES.get(status))
                .orElse(status.is4xxClientError() ? DEFAULT_CLIENT_ERROR : UNEXPECTED_ERROR);
    }

    static ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message, List<FieldErrorDTO> errors,
                                                  Throwable cause, String method, String path) {
        return build(status, message, errors, cause, method, path, HttpHeaders.EMPTY);
    }

    /**
     * Client errors (4xx) are expected, so they are logged as a single WARN line without a stack trace.
     * Server errors (5xx) are logged at ERROR with the full stack trace for diagnosis.
     */
    static ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message, List<FieldErrorDTO> errors,
                                                  Throwable cause, String method, String path,
                                                  HttpHeaders headers) {
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        String reason = Optional.ofNullable(cause).map(Throwable::getMessage).orElse("n/a");

        if (status.is5xxServerError()) {
            log.error("errorId={} status={} method={} path={} reason={}",
                    errorId, status.value(), method, path, reason, cause);
        } else {
            log.warn("errorId={} status={} method={} path={} reason={}",
                    errorId, status.value(), method, path, reason);
        }

        ErrorResponseDTO body = new ErrorResponseDTO(status.value(), status.getReasonPhrase(), message, path,
                Instant.now(), errorId, List.copyOf(errors));
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}

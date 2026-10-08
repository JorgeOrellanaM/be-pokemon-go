package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.domain.exception.ErrorCategory;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Builds every error response of the API (an {@link ApiResponseDTO} wrapping an {@link ErrorResponseDTO})
 * and logs it. Technical details (exception messages, stack
 * traces) go only to the log; the client receives the {@link ApiError} message and an {@code errorId}
 * that points to the matching log line.
 */
final class ErrorResponses {

    private static final Logger log = LoggerFactory.getLogger(ErrorResponses.class);

    private static final Map<ErrorCategory, HttpStatus> STATUS_BY_CATEGORY = new EnumMap<>(Map.of(
            ErrorCategory.INVALID_INPUT, HttpStatus.BAD_REQUEST,
            ErrorCategory.UNAUTHENTICATED, HttpStatus.UNAUTHORIZED,
            ErrorCategory.FORBIDDEN, HttpStatus.FORBIDDEN,
            ErrorCategory.NOT_FOUND, HttpStatus.NOT_FOUND,
            ErrorCategory.CONFLICT, HttpStatus.CONFLICT,
            ErrorCategory.UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE));

    private ErrorResponses() {
    }

    static HttpStatus statusOf(ErrorCategory category) {
        return STATUS_BY_CATEGORY.get(category);
    }

    static ResponseEntity<ApiResponseDTO<Void>> build(ApiError error, Throwable cause,
                                                      HttpServletRequest request) {
        return build(error, cause, request, HttpHeaders.EMPTY);
    }

    /**
     * Client errors (4xx) are expected, so they are logged as a single WARN line without a stack trace.
     * Server errors (5xx) are logged at ERROR with the full stack trace for diagnosis.
     */
    static ResponseEntity<ApiResponseDTO<Void>> build(ApiError error, Throwable cause, HttpServletRequest request,
                                                      HttpHeaders headers) {
        HttpStatus status = error.status();
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        String path = originalPath(request);
        String reason = Optional.ofNullable(cause).map(Throwable::getMessage).orElse("n/a");

        if (status.is5xxServerError()) {
            log.error("errorId={} status={} method={} path={} reason={}",
                    errorId, status.value(), request.getMethod(), path, reason, cause);
        } else {
            log.warn("errorId={} status={} method={} path={} reason={}",
                    errorId, status.value(), request.getMethod(), path, reason);
        }

        ErrorResponseDTO body = new ErrorResponseDTO(status.value(), status.getReasonPhrase(), error.message(),
                path, Instant.now(), errorId, error.errors());
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponseDTO.failure(body));
    }

    /**
     * When the container forwards an error to {@code /error}, the URI the client called is kept in a
     * request attribute; otherwise it is the request URI itself. The query string is never included.
     */
    private static String originalPath(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI))
                .map(Object::toString)
                .orElse(request.getRequestURI());
    }
}

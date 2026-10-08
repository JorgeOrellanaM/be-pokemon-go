package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Optional;

/**
 * Extends {@link ResponseEntityExceptionHandler} so Spring MVC's own errors (404, 405, 415, missing
 * parameter…) keep their status, while every response body goes through {@link ErrorResponses}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles every domain exception through its {@link com.interview.pokemon_go.domain.exception.ErrorCategory},
     * so a new exception needs no new handler. 4xx messages are written for end users and returned as
     * they are; 5xx causes are internal, so the client gets a generic message.
     */
    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiResponseDTO<Void>> handleDomain(DomainException ex, HttpServletRequest request) {
        HttpStatus status = ErrorResponses.statusOf(ex.category());
        String message = status.is5xxServerError() ? ErrorMessages.forStatus(status) : ex.getMessage();
        return ErrorResponses.build(ApiError.of(status, message), ex, request);
    }

    /**
     * Tomcat throws this while Spring reads a malformed query string (e.g. {@code ?page=%}). It is
     * the client's mistake, so it is a 400, not a 500.
     */
    @ExceptionHandler(InvalidParameterException.class)
    ResponseEntity<ApiResponseDTO<Void>> handleMalformedParameters(InvalidParameterException ex,
                                                                   HttpServletRequest request) {
        return ErrorResponses.build(ApiError.of(HttpStatus.BAD_REQUEST, ErrorMessages.INVALID_CHARACTERS),
                ex, request);
    }

    /**
     * Last-resort handler. The exception message may contain internals, so it is only logged.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponseDTO<Void>> handleUnexpected(Exception ex, HttpServletRequest request) {
        return ErrorResponses.build(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.UNEXPECTED_ERROR),
                ex, request);
    }

    /**
     * A query parameter that cannot be converted (e.g. {@code page=abc}) becomes a 400 naming the
     * parameter and describing the expected value.
     */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        String field = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : Optional.ofNullable(ex.getPropertyName()).orElse("unknown");
        String expected = Optional.ofNullable(ex.getRequiredType())
                .map(ErrorMessages::expectedValue)
                .orElse("has an invalid value");
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ErrorMessages.invalidParameter(field),
                List.of(new FieldErrorDTO(field, expected)));
        return asObject(ErrorResponses.build(error, ex, servletRequest(request)));
    }

    /**
     * Every other Spring MVC exception ends here. Spring's own messages mention framework internals
     * (e.g. "No static resource …"), so they are replaced by a friendly message for the status.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = Optional.ofNullable(HttpStatus.resolve(statusCode.value()))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        return asObject(ErrorResponses.build(ApiError.of(status, ErrorMessages.forStatus(status)), ex,
                servletRequest(request), headers));
    }

    private static HttpServletRequest servletRequest(WebRequest request) {
        return ((ServletWebRequest) request).getRequest();
    }

    private static ResponseEntity<Object> asObject(ResponseEntity<ApiResponseDTO<Void>> response) {
        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}

package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.domain.exception.InvalidPageQueryException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Extends {@link ResponseEntityExceptionHandler} so Spring MVC's own errors (404, 405, 415, missing
 * parameter…) keep their status, while every response body goes through {@link ErrorResponses}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Set<Class<?>> WHOLE_NUMBER_TYPES =
            Set.of(int.class, Integer.class, long.class, Long.class, short.class, Short.class);
    private static final Set<Class<?>> DECIMAL_TYPES =
            Set.of(double.class, Double.class, float.class, Float.class, BigDecimal.class);

    /**
     * Domain validation messages are written for end users, so they are returned as they are.
     */
    @ExceptionHandler(InvalidPageQueryException.class)
    ResponseEntity<ErrorResponseDTO> handleBadRequest(InvalidPageQueryException ex, HttpServletRequest request) {
        return ErrorResponses.build(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of(), ex,
                request.getMethod(), request.getRequestURI());
    }

    /**
     * The cause (database, PokeAPI) is an internal detail: it is logged, and the client only learns
     * that the service is temporarily unavailable.
     */
    @ExceptionHandler(ExternalServiceUnavailableException.class)
    ResponseEntity<ErrorResponseDTO> handleServiceUnavailable(ExternalServiceUnavailableException ex,
                                                              HttpServletRequest request) {
        return ErrorResponses.build(HttpStatus.SERVICE_UNAVAILABLE, ErrorResponses.SERVICE_UNAVAILABLE,
                List.of(), ex, request.getMethod(), request.getRequestURI());
    }

    /**
     * Tomcat throws this while Spring reads a malformed query string (e.g. {@code ?page=%}). It is
     * the client's mistake, so it is a 400, not a 500.
     */
    @ExceptionHandler(InvalidParameterException.class)
    ResponseEntity<ErrorResponseDTO> handleMalformedParameters(InvalidParameterException ex,
                                                               HttpServletRequest request) {
        return ErrorResponses.build(HttpStatus.BAD_REQUEST,
                "The request contains invalid characters. Please check it and try again.",
                List.of(), ex, request.getMethod(), request.getRequestURI());
    }

    /**
     * Last-resort handler. The exception message may contain internals, so it is only logged.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex, HttpServletRequest request) {
        return ErrorResponses.build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponses.UNEXPECTED_ERROR,
                List.of(), ex, request.getMethod(), request.getRequestURI());
    }

    /**
     * A query parameter that cannot be converted (e.g. {@code page=abc}) becomes a 400 naming the
     * parameter. The expected type is described in plain words, never as a Java type.
     */
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        String field = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : Optional.ofNullable(ex.getPropertyName()).orElse("unknown");
        String expected = Optional.ofNullable(ex.getRequiredType())
                .map(GlobalExceptionHandler::describeExpectedType)
                .orElse("has an invalid value");
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        return asObject(ErrorResponses.build(HttpStatus.BAD_REQUEST,
                "Please check the '%s' parameter.".formatted(field),
                List.of(new FieldErrorDTO(field, expected)), ex,
                servletRequest.getMethod(), servletRequest.getRequestURI()));
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
        HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();
        return asObject(ErrorResponses.build(status, ErrorResponses.friendlyMessage(status), List.of(), ex,
                servletRequest.getMethod(), servletRequest.getRequestURI(), headers));
    }

    private static String describeExpectedType(Class<?> type) {
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

    private static ResponseEntity<Object> asObject(ResponseEntity<ErrorResponseDTO> response) {
        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}

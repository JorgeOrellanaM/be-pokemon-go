package com.interview.pokemon_go.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Objects;

/**
 * Security rejects requests in a servlet filter, before any controller or {@link GlobalExceptionHandler}
 * runs. This writes those 401/403 answers through {@link ErrorResponses}, so they have the same body,
 * {@code errorId} and WARN log line as every other client error. The reason (e.g. why a token could not
 * be decoded) goes only to the log.
 */
@Component
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    // RFC 6750: a 401 tells the client which authentication scheme the resource expects
    private static final String BEARER_CHALLENGE = "Bearer";

    private final JsonMapper jsonMapper;

    public ApiSecurityErrorHandler(JsonMapper jsonMapper) {
        this.jsonMapper = Objects.requireNonNull(jsonMapper, "jsonMapper must not be null");
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.WWW_AUTHENTICATE, BEARER_CHALLENGE);
        write(ErrorResponses.build(ApiError.of(HttpStatus.UNAUTHORIZED,
                ErrorMessages.forStatus(HttpStatus.UNAUTHORIZED)), exception, request, headers), response);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        write(ErrorResponses.build(ApiError.of(HttpStatus.FORBIDDEN, ErrorMessages.forStatus(HttpStatus.FORBIDDEN)),
                exception, request), response);
    }

    private void write(ResponseEntity<ApiResponseDTO<Void>> error, HttpServletResponse response) throws IOException {
        response.setStatus(error.getStatusCode().value());
        error.getHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
        jsonMapper.writeValue(response.getOutputStream(), error.getBody());
    }
}

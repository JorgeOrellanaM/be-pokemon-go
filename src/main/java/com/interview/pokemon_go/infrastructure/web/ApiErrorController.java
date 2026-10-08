package com.interview.pokemon_go.infrastructure.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Replaces Spring Boot's default {@code /error} endpoint. Errors raised outside controllers (servlet
 * filters, security, the container) are forwarded here and get the same body as every other API error.
 */
@RestController
@RequestMapping("${server.error.path:/error}")
public class ApiErrorController implements ErrorController {

    @RequestMapping
    ResponseEntity<ApiResponseDTO<Void>> error(HttpServletRequest request) {
        HttpStatus status = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
                .filter(Integer.class::isInstance)
                .map(code -> HttpStatus.resolve((Integer) code))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
        Throwable cause = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION))
                .filter(Throwable.class::isInstance)
                .map(Throwable.class::cast)
                .orElse(null);

        return ErrorResponses.build(ApiError.of(status, ErrorMessages.forStatus(status)), cause, request);
    }
}

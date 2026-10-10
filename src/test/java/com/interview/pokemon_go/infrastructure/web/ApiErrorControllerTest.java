package com.interview.pokemon_go.infrastructure.web;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers errors raised outside controllers (filters, the servlet container), which Spring Boot
 * forwards to {@code /error}.
 */
@WebMvcTest(ApiErrorController.class)
@ImportApiSecurity
@ExtendWith(OutputCaptureExtension.class)
class ApiErrorControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void returnsGenericServerErrorAndLogsTheCause(CapturedOutput output) {
        MvcTestResult result = mvc.get().uri("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/v1/pokemon")
                .requestAttr(RequestDispatcher.ERROR_EXCEPTION, new IllegalStateException("db password leaked"))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                { "success": false,
                  "error": { "status": 500, "error": "Internal Server Error",
                             "message": "An unexpected error occurred. Please try again later.",
                             "path": "/api/v1/pokemon" } }
                """);
        assertThat(result).bodyText().doesNotContain("password").doesNotContain("IllegalState");
        assertThat(output).contains("db password leaked");
    }

    @Test
    void returnsFriendlyMessageForClientErrorStatus() {
        MvcTestResult result = mvc.get().uri("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/nowhere")
                .exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.error.message").isEqualTo("The requested resource was not found.");
    }

    @Test
    void fallsBackToServerErrorWhenStatusIsMissing() {
        assertThat(mvc.get().uri("/error")).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

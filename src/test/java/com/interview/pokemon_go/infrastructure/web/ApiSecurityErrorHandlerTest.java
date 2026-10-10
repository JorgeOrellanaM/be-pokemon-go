package com.interview.pokemon_go.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test of the security error responses. The 403 path is covered here because no route can
 * produce it yet (there are no roles).
 */
@ExtendWith(OutputCaptureExtension.class)
class ApiSecurityErrorHandlerTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final ApiSecurityErrorHandler handler = new ApiSecurityErrorHandler(jsonMapper);
    private final MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/v1/local-pokemon/25");
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void unauthenticatedRequestsGetA401WithABearerChallenge(CapturedOutput output) throws Exception {
        handler.commence(request, response, new InsufficientAuthenticationException("token signature invalid"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Bearer");
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        JsonNode body = jsonMapper.readTree(response.getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.at("/error/status").asInt()).isEqualTo(401);
        assertThat(body.at("/error/message").asString())
                .isEqualTo("Authentication is required to access this resource.");
        assertThat(body.at("/error/path").asString()).isEqualTo("/api/v1/local-pokemon/25");
        assertThat(response.getContentAsString()).doesNotContain("signature");
        assertThat(output).contains("WARN").contains("status=401 method=PUT").contains("token signature invalid");
    }

    @Test
    void deniedRequestsGetA403WithoutAChallenge(CapturedOutput output) throws Exception {
        handler.handle(request, response, new AccessDeniedException("missing role ADMIN"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE)).isNull();
        JsonNode body = jsonMapper.readTree(response.getContentAsString());
        assertThat(body.at("/error/status").asInt()).isEqualTo(403);
        assertThat(body.at("/error/message").asString())
                .isEqualTo("You do not have permission to access this resource.");
        assertThat(response.getContentAsString()).doesNotContain("ADMIN");
        assertThat(output).contains("WARN").contains("status=403");
    }
}

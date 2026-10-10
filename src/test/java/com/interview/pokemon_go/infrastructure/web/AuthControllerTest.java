package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.application.port.in.AuthenticateUserUseCase;
import com.interview.pokemon_go.application.port.in.RegisterUserUseCase;
import com.interview.pokemon_go.domain.exception.InvalidCredentialsException;
import com.interview.pokemon_go.domain.exception.UsernameAlreadyExistsException;
import com.interview.pokemon_go.domain.model.AccessToken;
import com.interview.pokemon_go.domain.model.Credentials;
import com.interview.pokemon_go.domain.model.Registration;
import com.interview.pokemon_go.domain.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@ImportApiSecurity
@ExtendWith(OutputCaptureExtension.class)
class AuthControllerTest {

    private static final String URL = "/api/v1/auth";
    private static final Instant EXPIRES_AT = Instant.parse("2030-01-01T00:00:00Z");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private RegisterUserUseCase registerUser;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUser;

    @Test
    void registerCreatesTheUserAndPointsToIt() {
        given(registerUser.register(new Registration("ash", "Pokemon123!")))
                .willReturn(new UserAccount("ash", "$2a$10$secret-hash"));

        MvcTestResult result = post("/register", """
                { "username": " Ash ", "password": "Pokemon123!" }
                """);

        assertThat(result).hasStatus(HttpStatus.CREATED)
                .hasHeader(HttpHeaders.LOCATION, "http://localhost/api/v1/auth/me");
        assertThat(result).bodyJson().isStrictlyEqualTo("""
                { "success": true, "data": { "username": "ash" } }
                """);
        assertThat(result).bodyText().doesNotContain("secret-hash").doesNotContain("Pokemon123!");
    }

    @Test
    void registerReportsEveryInvalidField() {
        MvcTestResult result = post("/register", """
                { "username": "a", "password": "short" }
                """);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                { "success": false,
                  "error": { "status": 400, "message": "Please check the highlighted fields.",
                             "errors": [ { "field": "username", "message": "must be between 3 and 30 characters" },
                                         { "field": "password", "message": "must be between 8 and 72 characters" } ] } }
                """);
        verifyNoInteractions(registerUser);
    }

    @Test
    void registerRejectsAnEmptyBodyAndUnknownFields() {
        assertThat(post("/register", "")).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(post("/register", """
                { "username": "ash", "password": "Pokemon123!", "role": "ADMIN" }
                """)).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.error.errors[0].field").isEqualTo("role");
    }

    @Test
    void registerReturnsConflictForATakenUsername() {
        given(registerUser.register(any())).willThrow(new UsernameAlreadyExistsException("ash"));

        MvcTestResult result = post("/register", """
                { "username": "ash", "password": "Pokemon123!" }
                """);

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.error.message").isEqualTo("The username 'ash' is already taken");
    }

    @Test
    void loginReturnsABearerToken() {
        given(authenticateUser.authenticate(new Credentials("ash", "Pokemon123!")))
                .willReturn(new AccessToken("signed.jwt.token", EXPIRES_AT));

        MvcTestResult result = post("/login", """
                { "username": "ash", "password": "Pokemon123!" }
                """);

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().isStrictlyEqualTo("""
                { "success": true,
                  "data": { "accessToken": "signed.jwt.token", "tokenType": "Bearer",
                            "expiresAt": "2030-01-01T00:00:00Z" } }
                """);
    }

    @Test
    void loginRejectsInvalidCredentialsWithAGenericMessageAndLogsAWarning(CapturedOutput output) {
        given(authenticateUser.authenticate(any())).willThrow(new InvalidCredentialsException());

        MvcTestResult result = post("/login", """
                { "username": "ash", "password": "wrong-password" }
                """);

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result).bodyJson().extractingPath("$.error.message").isEqualTo("Invalid username or password.");
        assertThat(result).bodyText().doesNotContain("wrong-password");
        assertThat(output).contains("WARN").contains("status=401").doesNotContain("wrong-password");
    }

    @Test
    void loginRequiresBothFields() {
        MvcTestResult result = post("/login", "{}");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.error.errors").asArray().hasSize(2);
        verifyNoInteractions(authenticateUser);
    }

    @Test
    void meReturnsTheUserOfTheToken() {
        MvcTestResult result = mvc.get().uri(URL + "/me").with(jwt().jwt(token -> token.subject("ash"))).exchange();

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().isStrictlyEqualTo("""
                { "success": true, "data": { "username": "ash" } }
                """);
    }

    @Test
    void meRequiresAToken() {
        assertThat(mvc.get().uri(URL + "/me").exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private MvcTestResult post(String path, String body) {
        return mvc.post().uri(URL + path).contentType(MediaType.APPLICATION_JSON).content(body).exchange();
    }
}

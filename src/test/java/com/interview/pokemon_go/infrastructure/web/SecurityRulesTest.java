package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.application.port.in.GetLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.in.GetPokemonDetailsUseCase;
import com.interview.pokemon_go.application.port.in.ListLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.in.ListPokemonUseCase;
import com.interview.pokemon_go.application.port.in.SyncPokemonUseCase;
import com.interview.pokemon_go.application.port.in.UpdateLocalPokemonUseCase;
import com.interview.pokemon_go.domain.model.PageResult;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * The route policy of {@code SecurityConfig}: reads are public, writes need a bearer token, and any
 * other route is denied by default. Rejections use the shared error envelope.
 */
@WebMvcTest({PokemonController.class, LocalPokemonController.class})
@Import(GlobalExceptionHandler.class)
@ImportApiSecurity
@ExtendWith(OutputCaptureExtension.class)
class SecurityRulesTest {

    private static final String UPDATE_BODY = """
            { "localizedName": "Pikachu (ES)", "region": "Kanto", "tags": [] }
            """;

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ListPokemonUseCase listPokemon;

    @MockitoBean
    private GetPokemonDetailsUseCase getPokemonDetails;

    @MockitoBean
    private SyncPokemonUseCase syncPokemon;

    @MockitoBean
    private GetLocalPokemonUseCase getLocalPokemon;

    @MockitoBean
    private ListLocalPokemonUseCase listLocalPokemon;

    @MockitoBean
    private UpdateLocalPokemonUseCase updateLocalPokemon;

    @Test
    void readsArePublic() {
        given(listPokemon.list(any())).willReturn(new PageResult<>(List.of(), 0, 20, 0));
        given(listLocalPokemon.list(any())).willReturn(new PageResult<>(List.of(), 0, 20, 0));

        assertThat(mvc.get().uri("/api/v1/pokemon").exchange()).hasStatus(HttpStatus.OK);
        assertThat(mvc.get().uri("/api/v1/local-pokemon").exchange()).hasStatus(HttpStatus.OK);
    }

    @Test
    void anonymousWritesAreRejectedWithTheErrorEnvelope(CapturedOutput output) {
        MvcTestResult result = mvc.put().uri("/api/v1/local-pokemon/25")
                .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY).exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .hasHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                { "success": false,
                  "error": { "status": 401, "error": "Unauthorized",
                             "message": "Authentication is required to access this resource.",
                             "path": "/api/v1/local-pokemon/25" } }
                """);
        assertThat(result).bodyJson().extractingPath("$.error.errorId").isNotNull();
        assertThat(output).contains("WARN").contains("status=401 method=PUT path=/api/v1/local-pokemon/25")
                .doesNotContain("ERROR");
        verifyNoInteractions(updateLocalPokemon);
    }

    @Test
    void anonymousSyncIsRejected() {
        assertThat(mvc.post().uri("/api/v1/local-pokemon/25/sync").exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(syncPokemon);
    }

    @Test
    void anInvalidTokenIsRejectedWithoutDetails(CapturedOutput output) {
        MvcTestResult result = mvc.post().uri("/api/v1/local-pokemon/25/sync")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token").exchange();

        assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(result).bodyJson().extractingPath("$.error.message")
                .isEqualTo("Authentication is required to access this resource.");
        assertThat(result).bodyText().doesNotContainIgnoringCase("jwt").doesNotContain("not-a-real-token");
        assertThat(output).contains("status=401");
        verifyNoInteractions(syncPokemon);
    }

    @Test
    void writesWithATokenReachTheUseCase() {
        mvc.put().uri("/api/v1/local-pokemon/25").with(jwt())
                .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY).exchange();
        mvc.post().uri("/api/v1/local-pokemon/25/sync").with(jwt()).exchange();

        verify(updateLocalPokemon).update(anyInt(), any());
        verify(syncPokemon).sync(25);
    }

    @Test
    void unknownRoutesAreDeniedByDefault() {
        assertThat(mvc.get().uri("/api/v1/trainers").exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(mvc.get().uri("/api/v1/trainers").with(jwt()).exchange()).hasStatus(HttpStatus.NOT_FOUND);
    }
}

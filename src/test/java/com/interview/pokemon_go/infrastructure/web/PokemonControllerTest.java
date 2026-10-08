package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.application.port.in.ListPokemonUseCase;
import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;
import com.jayway.jsonpath.JsonPath;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(PokemonController.class)
@Import(GlobalExceptionHandler.class)
@ExtendWith(OutputCaptureExtension.class)
class PokemonControllerTest {

    private static final String URL = "/api/v1/pokemon";

    private static final PokemonSummary PIKACHU = new PokemonSummary(25, "pikachu",
            "https://example.org/25.png", "Mouse Pokémon", new Weight(60),
            List.of(new Ability("static", false), new Ability("lightning-rod", true)));

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ListPokemonUseCase listPokemon;

    @Test
    void returnsPagedPokemon() {
        given(listPokemon.list(new PageQuery(1, 1)))
                .willReturn(new PageResult<>(List.of(PIKACHU), 1, 1, 10));

        var response = mvc.get().uri(URL).param("page", "1").param("size", "1");

        assertThat(response).hasStatusOk().hasContentType(MediaType.APPLICATION_JSON);
        assertThat(response).bodyJson().isLenientlyEqualTo("""
                {
                  "items": [{
                    "id": 25,
                    "name": "pikachu",
                    "spriteUrl": "https://example.org/25.png",
                    "category": "Mouse Pokémon",
                    "weightKg": 6.0,
                    "abilities": [
                      {"name": "static", "hidden": false},
                      {"name": "lightning-rod", "hidden": true}
                    ]
                  }],
                  "page": 1,
                  "size": 1,
                  "totalElements": 10,
                  "totalPages": 10
                }
                """);
    }

    @Test
    void usesDefaultPageAndSize() {
        given(listPokemon.list(any())).willReturn(new PageResult<>(List.of(), 0, 20, 0));

        assertThat(mvc.get().uri(URL)).hasStatusOk();

        verify(listPokemon).list(new PageQuery(0, 20));
    }

    @Test
    void rejectsSizeAboveMaximumWithDomainMessage() {
        MvcTestResult result = mvc.get().uri(URL).param("size", "51").exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                {
                  "status": 400,
                  "error": "Bad Request",
                  "message": "size must be between 1 and 50",
                  "path": "/api/v1/pokemon"
                }
                """);
        assertThat(result).bodyJson().extractingPath("$.errorId").asString().isNotBlank();
        assertThat(result).bodyJson().extractingPath("$.timestamp").asString().isNotBlank();
        assertThat(result).bodyJson().doesNotHavePath("$.errors");
        verify(listPokemon, never()).list(any());
    }

    @Test
    void rejectsNegativePage() {
        assertThat(mvc.get().uri(URL).param("page", "-1")).hasStatus(HttpStatus.BAD_REQUEST);
        verify(listPokemon, never()).list(any());
    }

    @Test
    void rejectsNonNumericPageWithFriendlyFieldError() {
        MvcTestResult result = mvc.get().uri(URL).param("page", "abc").exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                {
                  "status": 400,
                  "message": "Please check the 'page' parameter.",
                  "errors": [ { "field": "page", "message": "must be a whole number" } ]
                }
                """);
        assertThat(result).bodyText()
                .doesNotContain("int").doesNotContain("java").doesNotContain("NumberFormat")
                .doesNotContain("Exception");
    }

    @Test
    void neverEchoesTheQueryStringInThePath() {
        MvcTestResult result = mvc.get().uri(URL + "?page=<script>").exchange();

        assertThat(result).bodyJson().extractingPath("$.path").isEqualTo("/api/v1/pokemon");
        assertThat(result).bodyText().doesNotContain("<script>");
    }

    @Test
    void treatsMalformedRequestParametersAsClientError() {
        given(listPokemon.list(any())).willThrow(new InvalidParameterException(
                "Character decoding failed. Parameter [page] with value [%] has been ignored.", 400));

        MvcTestResult result = mvc.get().uri(URL).exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("The request contains invalid characters. Please check it and try again.");
        assertThat(result).bodyText().doesNotContain("decoding");
    }

    @Test
    void rejectsUnsupportedMethodWithFriendlyMessage() {
        MvcTestResult result = mvc.post().uri(URL).exchange();

        assertThat(result).hasStatus(HttpStatus.METHOD_NOT_ALLOWED).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                { "status": 405, "error": "Method Not Allowed",
                  "message": "This operation is not supported for this resource." }
                """);
    }

    @Test
    void returnsFriendlyNotFoundForUnknownRoute() {
        MvcTestResult result = mvc.get().uri("/api/v1/unknown").exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                { "status": 404, "error": "Not Found",
                  "message": "The requested resource was not found.", "path": "/api/v1/unknown" }
                """);
        assertThat(result).bodyText().doesNotContain("static resource");
    }

    @Test
    void returnsServiceUnavailableWithGenericMessage() {
        given(listPokemon.list(any())).willThrow(
                new ExternalServiceUnavailableException("Pokemon database is unavailable", new RuntimeException()));

        MvcTestResult result = mvc.get().uri(URL).exchange();

        assertThat(result).hasStatus(HttpStatus.SERVICE_UNAVAILABLE).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("The service is temporarily unavailable. Please try again later.");
        assertThat(result).bodyText().doesNotContain("database");
    }

    @Test
    void hidesInternalDetailsButLogsThemWithTheSameErrorId(CapturedOutput output) throws Exception {
        given(listPokemon.list(any())).willThrow(new IllegalStateException("secret internals"));

        MvcTestResult result = mvc.get().uri(URL).exchange();

        assertThat(result).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("An unexpected error occurred. Please try again later.");
        assertThat(result).bodyText().doesNotContain("secret").doesNotContain("IllegalState");

        String errorId = errorId(result);
        assertThat(output).contains("errorId=" + errorId)
                .contains("ERROR")
                .contains("java.lang.IllegalStateException: secret internals");
    }

    @Test
    void logsClientErrorsAsSingleWarnLineWithoutStackTrace(CapturedOutput output) throws Exception {
        MvcTestResult result = mvc.get().uri(URL).param("size", "51").exchange();

        String errorId = errorId(result);
        assertThat(output).contains("WARN").contains("errorId=" + errorId).contains("status=400")
                .contains("reason=size must be between 1 and 50");
        assertThat(output).doesNotContain("\tat com.interview");
    }

    private static String errorId(MvcTestResult result) throws UnsupportedEncodingException {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.errorId");
    }
}

package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.application.port.in.ListPokemonUseCase;
import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(PokemonController.class)
@Import(GlobalExceptionHandler.class)
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
    void rejectsSizeAboveMaximum() {
        var response = mvc.get().uri(URL).param("size", "51");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response).bodyJson().extractingPath("$.detail").asString().contains("size");
        verify(listPokemon, never()).list(any());
    }

    @Test
    void rejectsNegativePage() {
        assertThat(mvc.get().uri(URL).param("page", "-1")).hasStatus(HttpStatus.BAD_REQUEST);
        verify(listPokemon, never()).list(any());
    }

    @Test
    void rejectsNonNumericPageWithFieldError() {
        var response = mvc.get().uri(URL).param("page", "abc");

        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response).bodyJson().extractingPath("$.errors[0].field").isEqualTo("page");
    }

    @Test
    void returnsServiceUnavailableWhenCatalogIsDown() {
        given(listPokemon.list(any()))
                .willThrow(new ExternalServiceUnavailableException("db down", new RuntimeException()));

        var response = mvc.get().uri(URL);

        assertThat(response).hasStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void hidesInternalDetailsOnUnexpectedErrors() {
        given(listPokemon.list(any())).willThrow(new IllegalStateException("secret internals"));

        var response = mvc.get().uri(URL);

        assertThat(response).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(response).bodyText().doesNotContain("secret");
    }
}

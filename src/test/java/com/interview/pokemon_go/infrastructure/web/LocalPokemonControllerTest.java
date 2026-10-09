package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.application.port.in.GetLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.in.ListLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.in.SyncPokemonUseCase;
import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.domain.exception.PokemonAlreadySyncedException;
import com.interview.pokemon_go.domain.exception.PokemonNotFoundException;
import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(LocalPokemonController.class)
@Import(GlobalExceptionHandler.class)
@ExtendWith(OutputCaptureExtension.class)
class LocalPokemonControllerTest {

    private static final String URL = "/api/v1/local-pokemon";

    private static final PokemonSummary PIKACHU = new PokemonSummary(25, "pikachu",
            "https://example.org/25.png", "Mouse Pokémon", new Weight(60),
            List.of(new Ability("static", false), new Ability("lightning-rod", true)));

    private static final LocalPokemon CUSTOMIZED_PIKACHU =
            new LocalPokemon(PIKACHU, "Pikachu (ES)", "Kanto", List.of("starter", "electric"));

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private SyncPokemonUseCase syncPokemon;

    @MockitoBean
    private GetLocalPokemonUseCase getLocalPokemon;

    @MockitoBean
    private ListLocalPokemonUseCase listLocalPokemon;

    @Test
    void syncReturnsCreatedWithLocationAndTheStoredPokemon() {
        given(syncPokemon.sync(25)).willReturn(LocalPokemon.replicaOf(PIKACHU));

        MvcTestResult result = mvc.post().uri(URL + "/25/sync").exchange();

        assertThat(result).hasStatus(HttpStatus.CREATED).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).headers().hasValue(HttpHeaders.LOCATION, "http://localhost/api/v1/local-pokemon/25");
        assertThat(result).bodyJson().isStrictlyEqualTo("""
                {
                  "success": true,
                  "data": {
                    "id": 25,
                    "name": "pikachu",
                    "spriteUrl": "https://example.org/25.png",
                    "category": "Mouse Pokémon",
                    "weightKg": 6.0,
                    "abilities": [
                      {"name": "static", "hidden": false},
                      {"name": "lightning-rod", "hidden": true}
                    ],
                    "localizedName": null,
                    "region": null,
                    "tags": []
                  }
                }
                """);
    }

    @Test
    void syncOfAnAlreadySyncedPokemonIsAConflictLoggedAsWarning(CapturedOutput output) {
        given(syncPokemon.sync(25)).willThrow(new PokemonAlreadySyncedException(25));

        MvcTestResult result = mvc.post().uri(URL + "/25/sync").exchange();

        assertThat(result).hasStatus(HttpStatus.CONFLICT).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                { "success": false,
                  "error": { "status": 409, "error": "Conflict",
                             "message": "Pokemon with id 25 is already synced",
                             "path": "/api/v1/local-pokemon/25/sync" } }
                """);
        assertThat(result).bodyJson().doesNotHavePath("$.data");
        assertThat(output).contains("WARN").contains("status=409").doesNotContain("\tat com.interview");
    }

    @Test
    void syncOfAPokemonUnknownToPokeApiIsNotFound() {
        given(syncPokemon.sync(99999)).willThrow(new PokemonNotFoundException(99999));

        MvcTestResult result = mvc.post().uri(URL + "/99999/sync").exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.error.message")
                .isEqualTo("Pokemon with id 99999 was not found");
    }

    @Test
    void syncWhilePokeApiIsDownIsServiceUnavailableWithGenericMessage() {
        given(syncPokemon.sync(25)).willThrow(
                new ExternalServiceUnavailableException("PokeAPI is unavailable", new RuntimeException()));

        MvcTestResult result = mvc.post().uri(URL + "/25/sync").exchange();

        assertThat(result).hasStatus(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(result).bodyJson().extractingPath("$.error.message")
                .isEqualTo("The service is temporarily unavailable. Please try again later.");
        assertThat(result).bodyText().doesNotContain("PokeAPI");
    }

    @Test
    void syncRejectsNonNumericIdWithFriendlyFieldError() {
        MvcTestResult result = mvc.post().uri(URL + "/abc/sync").exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().isLenientlyEqualTo("""
                { "success": false,
                  "error": { "status": 400, "message": "Please check the 'id' parameter.",
                             "errors": [ { "field": "id", "message": "must be a whole number" } ] } }
                """);
        verify(syncPokemon, never()).sync(anyInt());
    }

    @Test
    void syncRejectsInvalidIdWithDomainMessage() {
        given(syncPokemon.sync(0)).willThrow(new DomainValidationException("id must be a positive whole number"));

        MvcTestResult result = mvc.post().uri(URL + "/0/sync").exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.error.message")
                .isEqualTo("id must be a positive whole number");
    }

    @Test
    void syncOnlyAcceptsPost() {
        assertThat(mvc.get().uri(URL + "/25/sync")).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void returnsAStoredPokemonWithItsCustomFields() {
        given(getLocalPokemon.getById(25)).willReturn(CUSTOMIZED_PIKACHU);

        var response = mvc.get().uri(URL + "/25");

        assertThat(response).hasStatusOk().hasContentType(MediaType.APPLICATION_JSON);
        assertThat(response).bodyJson().isLenientlyEqualTo("""
                { "success": true,
                  "data": { "id": 25, "name": "pikachu", "localizedName": "Pikachu (ES)",
                            "region": "Kanto", "tags": ["starter", "electric"] } }
                """);
    }

    @Test
    void returnsNotFoundForAPokemonThatWasNotSynced() {
        given(getLocalPokemon.getById(4242)).willThrow(new PokemonNotFoundException(4242));

        MvcTestResult result = mvc.get().uri(URL + "/4242").exchange();

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.error.path").isEqualTo("/api/v1/local-pokemon/4242");
    }

    @Test
    void returnsPagedLocalPokemon() {
        given(listLocalPokemon.list(new PageQuery(0, 5)))
                .willReturn(new PageResult<>(List.of(CUSTOMIZED_PIKACHU), 0, 5, 11));

        var response = mvc.get().uri(URL).param("page", "0").param("size", "5");

        assertThat(response).hasStatusOk();
        assertThat(response).bodyJson().isLenientlyEqualTo("""
                { "success": true,
                  "data": { "items": [ { "id": 25, "region": "Kanto" } ],
                            "page": 0, "size": 5, "totalElements": 11, "totalPages": 3 } }
                """);
    }

    @Test
    void listUsesDefaultPageAndSize() {
        given(listLocalPokemon.list(any())).willReturn(new PageResult<>(List.of(), 0, 20, 0));

        assertThat(mvc.get().uri(URL)).hasStatusOk();

        verify(listLocalPokemon).list(new PageQuery(0, 20));
    }

    @Test
    void listRejectsSizeAboveMaximum() {
        MvcTestResult result = mvc.get().uri(URL).param("size", "51").exchange();

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.error.message").isEqualTo("size must be between 1 and 50");
        verify(listLocalPokemon, never()).list(any());
    }
}

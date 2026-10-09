package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.application.port.in.GetLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.in.ListLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.in.SyncPokemonUseCase;
import com.interview.pokemon_go.application.port.in.UpdateLocalPokemonUseCase;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PageQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * US03/US04 - Pokemon replicated into the local database, where this service adds and edits its own
 * fields.
 */
@RestController
@RequestMapping(LocalPokemonController.BASE_PATH)
public class LocalPokemonController {

    static final String BASE_PATH = "/api/v1/local-pokemon";

    private final SyncPokemonUseCase syncPokemon;
    private final GetLocalPokemonUseCase getLocalPokemon;
    private final ListLocalPokemonUseCase listLocalPokemon;
    private final UpdateLocalPokemonUseCase updateLocalPokemon;

    public LocalPokemonController(SyncPokemonUseCase syncPokemon, GetLocalPokemonUseCase getLocalPokemon,
                                  ListLocalPokemonUseCase listLocalPokemon,
                                  UpdateLocalPokemonUseCase updateLocalPokemon) {
        this.syncPokemon = syncPokemon;
        this.getLocalPokemon = getLocalPokemon;
        this.listLocalPokemon = listLocalPokemon;
        this.updateLocalPokemon = updateLocalPokemon;
    }

    /**
     * Copies one Pokemon from PokeAPI into the local database. It creates the local resource, so the
     * answer is a 201 pointing at it; a second sync of the same Pokemon is a 409.
     */
    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponseDTO<LocalPokemonResponseDTO>> sync(@PathVariable int id) {
        LocalPokemon synced = syncPokemon.sync(id);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(BASE_PATH + "/{id}")
                .buildAndExpand(synced.id())
                .toUri();
        return ResponseEntity.created(location).body(ApiResponseDTO.ok(PokemonWebMapper.toLocalResponse(synced)));
    }

    @GetMapping("/{id}")
    public ApiResponseDTO<LocalPokemonResponseDTO> getById(@PathVariable int id) {
        return ApiResponseDTO.ok(PokemonWebMapper.toLocalResponse(getLocalPokemon.getById(id)));
    }

    /**
     * US04 - replaces the custom fields of a synced Pokemon; the PokeAPI data cannot be edited.
     */
    @PutMapping("/{id}")
    public ApiResponseDTO<LocalPokemonResponseDTO> update(@PathVariable int id,
                                                          @RequestBody UpdateLocalPokemonRequestDTO request) {
        LocalPokemon updated = updateLocalPokemon.update(id, PokemonWebMapper.toCustomization(request));
        return ApiResponseDTO.ok(PokemonWebMapper.toLocalResponse(updated));
    }

    @GetMapping
    public ApiResponseDTO<PageResponseDTO<LocalPokemonResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponseDTO.ok(PokemonWebMapper.toLocalPageResponse(listLocalPokemon.list(new PageQuery(page, size))));
    }
}

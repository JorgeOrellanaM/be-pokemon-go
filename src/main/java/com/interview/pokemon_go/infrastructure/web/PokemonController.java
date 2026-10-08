package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.application.port.in.ListPokemonUseCase;
import com.interview.pokemon_go.domain.model.PageQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pokemon")
public class PokemonController {

    private final ListPokemonUseCase listPokemon;

    public PokemonController(ListPokemonUseCase listPokemon) {
        this.listPokemon = listPokemon;
    }

    /**
     * US01 - Browse Pokemon. Paging limits are enforced by {@link PageQuery}.
     */
    @GetMapping
    public ApiResponseDTO<PageResponseDTO<PokemonSummaryResponseDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponseDTO.ok(PokemonWebMapper.toPageResponse(listPokemon.list(new PageQuery(page, size))));
    }
}

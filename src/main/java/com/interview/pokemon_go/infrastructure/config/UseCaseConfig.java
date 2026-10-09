package com.interview.pokemon_go.infrastructure.config;

import com.interview.pokemon_go.application.port.in.GetPokemonDetailsUseCase;
import com.interview.pokemon_go.application.port.in.ListPokemonUseCase;
import com.interview.pokemon_go.application.port.out.PokemonCatalogPort;
import com.interview.pokemon_go.application.port.out.PokemonDetailsPort;
import com.interview.pokemon_go.application.usecase.GetPokemonDetailsService;
import com.interview.pokemon_go.application.usecase.ListPokemonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ListPokemonUseCase listPokemonUseCase(PokemonCatalogPort catalog) {
        return new ListPokemonService(catalog);
    }

    @Bean
    public GetPokemonDetailsUseCase getPokemonDetailsUseCase(PokemonDetailsPort details) {
        return new GetPokemonDetailsService(details);
    }
}

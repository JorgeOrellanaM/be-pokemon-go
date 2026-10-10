package com.interview.pokemon_go.infrastructure.config;

import com.interview.pokemon_go.application.port.in.AuthenticateUserUseCase;
import com.interview.pokemon_go.application.port.in.GetLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.in.GetPokemonDetailsUseCase;
import com.interview.pokemon_go.application.port.in.ListLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.in.ListPokemonUseCase;
import com.interview.pokemon_go.application.port.in.RegisterUserUseCase;
import com.interview.pokemon_go.application.port.in.SyncPokemonUseCase;
import com.interview.pokemon_go.application.port.in.UpdateLocalPokemonUseCase;
import com.interview.pokemon_go.application.port.out.AccessTokenIssuerPort;
import com.interview.pokemon_go.application.port.out.LocalPokemonPort;
import com.interview.pokemon_go.application.port.out.PasswordHasherPort;
import com.interview.pokemon_go.application.port.out.PokemonCatalogPort;
import com.interview.pokemon_go.application.port.out.PokemonDetailsPort;
import com.interview.pokemon_go.application.port.out.UserAccountPort;
import com.interview.pokemon_go.application.usecase.AuthenticateUserService;
import com.interview.pokemon_go.application.usecase.GetLocalPokemonService;
import com.interview.pokemon_go.application.usecase.GetPokemonDetailsService;
import com.interview.pokemon_go.application.usecase.ListLocalPokemonService;
import com.interview.pokemon_go.application.usecase.ListPokemonService;
import com.interview.pokemon_go.application.usecase.RegisterUserService;
import com.interview.pokemon_go.application.usecase.SyncPokemonService;
import com.interview.pokemon_go.application.usecase.UpdateLocalPokemonService;
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

    @Bean
    public SyncPokemonUseCase syncPokemonUseCase(PokemonCatalogPort catalog, LocalPokemonPort local) {
        return new SyncPokemonService(catalog, local);
    }

    @Bean
    public GetLocalPokemonUseCase getLocalPokemonUseCase(LocalPokemonPort local) {
        return new GetLocalPokemonService(local);
    }

    @Bean
    public ListLocalPokemonUseCase listLocalPokemonUseCase(LocalPokemonPort local) {
        return new ListLocalPokemonService(local);
    }

    @Bean
    public UpdateLocalPokemonUseCase updateLocalPokemonUseCase(LocalPokemonPort local) {
        return new UpdateLocalPokemonService(local);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserAccountPort accounts, PasswordHasherPort hasher) {
        return new RegisterUserService(accounts, hasher);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(UserAccountPort accounts, PasswordHasherPort hasher,
                                                           AccessTokenIssuerPort tokens) {
        return new AuthenticateUserService(accounts, hasher, tokens);
    }
}

package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.application.port.out.PokemonCatalogPort;
import com.interview.pokemon_go.domain.exception.ExternalServiceUnavailableException;
import com.interview.pokemon_go.domain.model.PageQuery;
import com.interview.pokemon_go.domain.model.PageResult;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.infrastructure.config.PokeApiClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Component
public class PokeApiPokemonCatalogAdapter implements PokemonCatalogPort {

    private static final String LISTED_BUT_MISSING = "PokeAPI listed a Pokemon it cannot return";

    private final PokeApiClient pokeApi;
    private final Executor executor;

    PokeApiPokemonCatalogAdapter(PokeApiClient pokeApi,
                                 @Qualifier(PokeApiClientConfig.POKEAPI_EXECUTOR) Executor executor) {
        this.pokeApi = Objects.requireNonNull(pokeApi, "pokeApi must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    /**
     * PokeAPI's list only returns names and links, in Pokédex order, plus the total. The sprite,
     * weight and abilities come from each Pokemon and the category from its species, so a page costs
     * 1 + 2 × size calls. Without a cache these are made in parallel, which keeps a page at about
     * three round trips instead of one per call; results are joined in list order.
     */
    @Override
    public PageResult<PokemonSummary> findPage(PageQuery query) {
        PokeApiPokemonPageDTO page = pokeApi.listPokemon(query.offset(), query.size());
        List<CompletableFuture<PokemonSummary>> summaries = page.results().stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> summaryOf(entry), executor))
                .toList();
        return new PageResult<>(joinAll(summaries), query.page(), query.size(), page.count());
    }

    /**
     * An unknown id is an empty result; the species is only asked for once the Pokemon exists.
     */
    @Override
    public Optional<PokemonSummary> findById(int id) {
        return pokeApi.findPokemon(id).map(this::withSpecies);
    }

    /**
     * A Pokemon that PokeAPI itself listed must exist, so a missing one is an inconsistency (503),
     * not a "not found".
     */
    private PokemonSummary summaryOf(NamedResource entry) {
        return findById(PokeApiMapper.idFromUrl(entry.url()))
                .orElseThrow(() -> new ExternalServiceUnavailableException(LISTED_BUT_MISSING));
    }

    /**
     * The species is taken from the Pokemon's link, not its id, because alternate forms belong to the
     * species of their base form.
     */
    private PokemonSummary withSpecies(PokeApiPokemonDTO pokemon) {
        PokeApiSpeciesDTO species = pokeApi.getSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()));
        return PokeApiMapper.toSummary(pokemon, species);
    }

    /**
     * {@link CompletableFuture#join} wraps failures in a {@link CompletionException}; the original
     * exception is rethrown so a PokeAPI outage still reaches the client as a 503.
     */
    private static List<PokemonSummary> joinAll(List<CompletableFuture<PokemonSummary>> summaries) {
        try {
            return summaries.stream().map(CompletableFuture::join).toList();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}

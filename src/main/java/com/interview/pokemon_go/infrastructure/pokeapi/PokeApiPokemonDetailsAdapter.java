package com.interview.pokemon_go.infrastructure.pokeapi;

import com.interview.pokemon_go.application.port.out.PokemonDetailsPort;
import com.interview.pokemon_go.domain.model.PokemonDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class PokeApiPokemonDetailsAdapter implements PokemonDetailsPort {

    private final PokeApiClient pokeApi;

    PokeApiPokemonDetailsAdapter(PokeApiClient pokeApi) {
        this.pokeApi = Objects.requireNonNull(pokeApi, "pokeApi must not be null");
    }

    /**
     * Combines three PokeAPI resources: the Pokemon (image, types, stats), its species (description,
     * category) and the species' evolution chain. The species is taken from the Pokemon's link, not
     * from its id, because alternate forms (e.g. Mega Charizard X, id 10034) belong to the species of
     * their base form. Failures are already translated by {@link PokeApiClient}.
     */
    @Override
    public Optional<PokemonDetails> findById(int id) {
        return pokeApi.findPokemon(id).map(this::withSpeciesAndEvolution);
    }

    private PokemonDetails withSpeciesAndEvolution(PokeApiPokemonDTO pokemon) {
        PokeApiSpeciesDTO species = pokeApi.getSpecies(PokeApiMapper.idFromUrl(pokemon.species().url()));
        PokeApiEvolutionChainDTO evolutionChain =
                pokeApi.getEvolutionChain(PokeApiMapper.idFromUrl(species.evolutionChain().url()));
        return PokeApiMapper.toDomain(pokemon, species, evolutionChain);
    }
}

package com.interview.pokemon_go.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPokemonRepository extends JpaRepository<PokemonEntity, Long> {

    boolean existsByPokedexNumber(Integer pokedexNumber);

    Optional<PokemonEntity> findByPokedexNumber(Integer pokedexNumber);
}

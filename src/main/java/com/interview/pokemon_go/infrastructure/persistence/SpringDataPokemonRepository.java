package com.interview.pokemon_go.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPokemonRepository extends JpaRepository<PokemonEntity, Long> {
}

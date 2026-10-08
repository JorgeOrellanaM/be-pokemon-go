package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;

final class PokemonPersistenceMapper {

    private PokemonPersistenceMapper() {
    }

    /**
     * The Pokédex number becomes the domain id. The generated database id is deliberately dropped so
     * it never leaves the persistence adapter.
     */
    static PokemonSummary toDomain(PokemonEntity entity) {
        return new PokemonSummary(
                entity.getPokedexNumber(),
                entity.getName(),
                entity.getSpriteUrl(),
                entity.getCategory(),
                new Weight(entity.getWeightHectograms()),
                entity.getAbilities().stream()
                        .map(ability -> new Ability(ability.getName(), ability.isHidden()))
                        .toList());
    }

    /**
     * Builds a new, unsaved entity: the domain id is stored as the Pokédex number and the database id
     * is left for PostgreSQL to generate.
     */
    static PokemonEntity toEntity(PokemonSummary summary) {
        PokemonEntity entity = new PokemonEntity(
                summary.id(),
                summary.name(),
                summary.spriteUrl(),
                summary.category(),
                summary.weight().hectograms());
        summary.abilities().forEach(ability -> entity.addAbility(ability.name(), ability.hidden()));
        return entity;
    }
}

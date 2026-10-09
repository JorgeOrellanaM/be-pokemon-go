package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PokemonCustomization;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;

final class PokemonPersistenceMapper {

    private PokemonPersistenceMapper() {
    }

    /**
     * Builds a new, unsaved entity: the database id is left to the IDENTITY column. The domain id is
     * the Pokédex number, stored as the unique business key.
     */
    static PokemonEntity toEntity(LocalPokemon local) {
        PokemonSummary pokemon = local.pokemon();
        PokemonEntity entity = new PokemonEntity(pokemon.id(), pokemon.name(), pokemon.spriteUrl(),
                pokemon.category(), pokemon.weight().hectograms());
        pokemon.abilities().forEach(ability -> entity.addAbility(ability.name(), ability.hidden()));
        entity.customize(local.customization().localizedName(), local.customization().region());
        local.customization().tags().forEach(entity::addTag);
        return entity;
    }

    /**
     * Reads the lazy abilities and tags, so it must run inside the transaction that loaded the entity.
     */
    static LocalPokemon toDomain(PokemonEntity entity) {
        PokemonSummary pokemon = new PokemonSummary(
                entity.getPokedexNumber(),
                entity.getName(),
                entity.getSpriteUrl(),
                entity.getCategory(),
                new Weight(entity.getWeightHectograms()),
                entity.getAbilities().stream()
                        .map(ability -> new Ability(ability.getName(), ability.isHidden()))
                        .toList());
        return new LocalPokemon(pokemon, new PokemonCustomization(entity.getLocalizedName(), entity.getRegion(),
                entity.getTags().stream().map(PokemonTagEntity::getName).toList()));
    }
}

package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.domain.model.Ability;
import com.interview.pokemon_go.domain.model.LocalPokemon;
import com.interview.pokemon_go.domain.model.PokemonSummary;
import com.interview.pokemon_go.domain.model.Weight;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class PokemonPersistenceMapperTest {

    private static final LocalPokemon PIKACHU = new LocalPokemon(new PokemonSummary(25, "pikachu",
            "https://example.org/25.png", "Mouse Pokémon", new Weight(60),
            List.of(new Ability("static", false), new Ability("lightning-rod", true))),
            "Pikachu (ES)", "Kanto", List.of("starter", "electric"));

    @Test
    void mapsEveryFieldToTheEntity() {
        PokemonEntity entity = PokemonPersistenceMapper.toEntity(PIKACHU);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getPokedexNumber()).isEqualTo(25);
        assertThat(entity.getName()).isEqualTo("pikachu");
        assertThat(entity.getSpriteUrl()).isEqualTo("https://example.org/25.png");
        assertThat(entity.getCategory()).isEqualTo("Mouse Pokémon");
        assertThat(entity.getWeightHectograms()).isEqualTo(60);
        assertThat(entity.getAbilities()).extracting(AbilityEntity::getName, AbilityEntity::isHidden)
                .containsExactly(tuple("static", false), tuple("lightning-rod", true));
        assertThat(entity.getLocalizedName()).isEqualTo("Pikachu (ES)");
        assertThat(entity.getRegion()).isEqualTo("Kanto");
        assertThat(entity.getTags()).extracting(PokemonTagEntity::getName).containsExactly("starter", "electric");
        assertThat(entity.getTags()).allSatisfy(tag -> assertThat(tag.getPokemon()).isSameAs(entity));
    }

    @Test
    void mapsTheEntityBackToTheSameDomainModel() {
        assertThat(PokemonPersistenceMapper.toDomain(PokemonPersistenceMapper.toEntity(PIKACHU))).isEqualTo(PIKACHU);
    }

    @Test
    void mapsAReplicaWithoutCustomFields() {
        LocalPokemon replica = LocalPokemon.replicaOf(PIKACHU.pokemon());

        assertThat(PokemonPersistenceMapper.toDomain(PokemonPersistenceMapper.toEntity(replica))).isEqualTo(replica);
    }
}

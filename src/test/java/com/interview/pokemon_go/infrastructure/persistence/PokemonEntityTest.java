package com.interview.pokemon_go.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PokemonEntityTest {

    @Test
    void abilitiesCannotBeModifiedFromOutside() {
        PokemonEntity entity = new PokemonEntity(25, "pikachu", null, null, 60);
        entity.addAbility("static", false);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> entity.getAbilities().clear());
        assertThat(entity.getAbilities()).hasSize(1);
    }

    @Test
    void addAbilityLinksTheAbilityToItsPokemon() {
        PokemonEntity entity = new PokemonEntity(25, "pikachu", null, null, 60);

        entity.addAbility("static", false);

        assertThat(entity.getAbilities().getFirst().getPokemon()).isSameAs(entity);
    }

    @Test
    void tagsCannotBeModifiedFromOutside() {
        PokemonEntity entity = new PokemonEntity(25, "pikachu", null, null, 60);
        entity.addTag("starter");

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> entity.getTags().clear());
        assertThat(entity.getTags()).hasSize(1);
    }

    @Test
    void addTagLinksTheTagToItsPokemon() {
        PokemonEntity entity = new PokemonEntity(25, "pikachu", null, null, 60);

        entity.addTag("starter");

        assertThat(entity.getTags().getFirst().getPokemon()).isSameAs(entity);
    }

    @Test
    void clearTagsRemovesEveryTag() {
        PokemonEntity entity = new PokemonEntity(25, "pikachu", null, null, 60);
        entity.addTag("starter");

        entity.clearTags();

        assertThat(entity.getTags()).isEmpty();
    }

    @Test
    void customizeSetsTheLocalFields() {
        PokemonEntity entity = new PokemonEntity(25, "pikachu", null, null, 60);

        entity.customize("Pikachu (ES)", "Kanto");

        assertThat(entity.getLocalizedName()).isEqualTo("Pikachu (ES)");
        assertThat(entity.getRegion()).isEqualTo("Kanto");
    }
}

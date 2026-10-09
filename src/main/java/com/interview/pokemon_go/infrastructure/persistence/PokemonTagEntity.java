package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.domain.model.PokemonCustomization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * An internal classification tag of a local Pokemon (US03), owned by this service, not by PokeAPI.
 */
@Entity
@Table(name = "pokemon_tag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pokemon_id", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PokemonTagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private PokemonEntity pokemon;

    @Column(nullable = false, length = PokemonCustomization.MAX_TAG_LENGTH)
    private String name;

    PokemonTagEntity(PokemonEntity pokemon, String name) {
        this.pokemon = pokemon;
        this.name = name;
    }
}

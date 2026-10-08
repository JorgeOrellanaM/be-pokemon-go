package com.interview.pokemon_go.infrastructure.persistence;

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
 * An ability of a Pokemon. The primary key is auto-incremented; (pokemon_id, name) stays unique.
 */
@Entity
@Table(name = "pokemon_ability",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pokemon_id", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AbilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private PokemonEntity pokemon;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean hidden;

    AbilityEntity(PokemonEntity pokemon, String name, boolean hidden) {
        this.pokemon = pokemon;
        this.name = name;
        this.hidden = hidden;
    }
}

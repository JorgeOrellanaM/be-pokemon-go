package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.domain.model.PokemonCustomization;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Local copy of a Pokemon. The primary key is auto-incremented; the Pokédex number is a unique
 * business key and is what the domain exposes as the Pokemon id.
 */
@Entity
@Table(name = "pokemon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PokemonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pokedex_number", nullable = false, unique = true)
    private Integer pokedexNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "sprite_url", length = 500)
    private String spriteUrl;

    @Column(length = 100)
    private String category;

    @Column(name = "weight_hectograms", nullable = false)
    private double weightHectograms;

    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("hidden ASC, name ASC")
    @BatchSize(size = 50)
    private List<AbilityEntity> abilities = new ArrayList<>();

    // Fields below are owned by this service (US03), not copied from PokeAPI; nullable until set.

    @Column(name = "localized_name", length = PokemonCustomization.MAX_TEXT_LENGTH)
    private String localizedName;

    @Column(length = PokemonCustomization.MAX_TEXT_LENGTH)
    private String region;

    @OneToMany(mappedBy = "pokemon", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    private List<PokemonTagEntity> tags = new ArrayList<>();

    public PokemonEntity(Integer pokedexNumber, String name, String spriteUrl, String category,
                         double weightHectograms) {
        this.pokedexNumber = pokedexNumber;
        this.name = name;
        this.spriteUrl = spriteUrl;
        this.category = category;
        this.weightHectograms = weightHectograms;
    }

    /**
     * Read-only view: callers must go through {@link #addAbility} so the back-reference is always set.
     */
    public List<AbilityEntity> getAbilities() {
        return Collections.unmodifiableList(abilities);
    }

    /**
     * Abilities must be added through this method: it sets the back-reference to this Pokemon, which
     * is the owning side ({@code mappedBy}) of the relationship and fills {@code pokemon_id}.
     */
    public void addAbility(String name, boolean hidden) {
        abilities.add(new AbilityEntity(this, name, hidden));
    }

    /**
     * Read-only view in insertion order: callers must go through {@link #addTag}.
     */
    public List<PokemonTagEntity> getTags() {
        return Collections.unmodifiableList(tags);
    }

    /**
     * Same back-reference rule as {@link #addAbility}.
     */
    public void addTag(String name) {
        tags.add(new PokemonTagEntity(this, name));
    }

    /**
     * Orphan removal deletes the cleared tags on the next flush.
     */
    public void clearTags() {
        tags.clear();
    }

    public void customize(String localizedName, String region) {
        this.localizedName = localizedName;
        this.region = region;
    }
}

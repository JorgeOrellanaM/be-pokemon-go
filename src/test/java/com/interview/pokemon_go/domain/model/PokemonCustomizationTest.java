package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.FieldViolation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class PokemonCustomizationTest {

    @Test
    void keepsValidValues() {
        PokemonCustomization customization = new PokemonCustomization("Pikachu (ES)", "Kanto",
                List.of("starter", "electric"));

        assertThat(customization.localizedName()).isEqualTo("Pikachu (ES)");
        assertThat(customization.region()).isEqualTo("Kanto");
        assertThat(customization.tags()).containsExactly("starter", "electric");
    }

    @Test
    void noneHasNoValues() {
        assertThat(PokemonCustomization.NONE.localizedName()).isNull();
        assertThat(PokemonCustomization.NONE.region()).isNull();
        assertThat(PokemonCustomization.NONE.tags()).isEmpty();
    }

    @Test
    void stripsSurroundingWhitespace() {
        PokemonCustomization customization = new PokemonCustomization("  Pikachu ", " Kanto\t", List.of(" starter "));

        assertThat(customization).isEqualTo(new PokemonCustomization("Pikachu", "Kanto", List.of("starter")));
    }

    @Test
    void treatsBlankTextsAsCleared() {
        PokemonCustomization customization = new PokemonCustomization("   ", "", List.of());

        assertThat(customization).isEqualTo(PokemonCustomization.NONE);
    }

    @Test
    void acceptsTextsAndTagsAtTheirMaximumLength() {
        PokemonCustomization customization = new PokemonCustomization(
                "a".repeat(PokemonCustomization.MAX_TEXT_LENGTH), "b".repeat(PokemonCustomization.MAX_TEXT_LENGTH),
                tags(PokemonCustomization.MAX_TAGS, PokemonCustomization.MAX_TAG_LENGTH));

        assertThat(customization.tags()).hasSize(PokemonCustomization.MAX_TAGS);
    }

    @Test
    void reportsEveryInvalidFieldAtOnce() {
        DomainValidationException ex = invalid(() -> new PokemonCustomization(
                "a".repeat(101), "b".repeat(101), List.of("c".repeat(51))));

        assertThat(ex).hasMessage("Please check the highlighted fields.");
        assertThat(ex.violations()).containsExactly(
                new FieldViolation("localizedName", "must be at most 100 characters"),
                new FieldViolation("region", "must be at most 100 characters"),
                new FieldViolation("tags", "each tag must be at most 50 characters"));
    }

    @Test
    void rejectsTooManyTags() {
        assertThat(invalid(() -> new PokemonCustomization(null, null, tags(11, 5))).violations())
                .containsExactly(new FieldViolation("tags", "must contain at most 10 tags"));
    }

    @Test
    void rejectsMissingOrBlankTags() {
        assertThat(invalid(() -> new PokemonCustomization(null, null, Arrays.asList("starter", null))).violations())
                .containsExactly(new FieldViolation("tags", "must not contain empty tags"));
        assertThat(invalid(() -> new PokemonCustomization(null, null, List.of("starter", "  "))).violations())
                .containsExactly(new FieldViolation("tags", "must not contain empty tags"));
    }

    @Test
    void rejectsDuplicateTagsIgnoringCaseAndWhitespace() {
        assertThat(invalid(() -> new PokemonCustomization(null, null, List.of("Starter", " starter"))).violations())
                .containsExactly(new FieldViolation("tags", "must not contain duplicate tags"));
    }

    @Test
    void copiesTagsDefensively() {
        List<String> tags = new ArrayList<>(List.of("starter"));
        PokemonCustomization customization = new PokemonCustomization(null, null, tags);

        tags.add("legendary");

        assertThat(customization.tags()).containsExactly("starter");
    }

    @Test
    void requiresATagList() {
        assertThatNullPointerException().isThrownBy(() -> new PokemonCustomization(null, null, null));
    }

    private static DomainValidationException invalid(Runnable action) {
        return catchThrowableOfType(DomainValidationException.class, action::run);
    }

    private static List<String> tags(int count, int length) {
        return IntStream.range(0, count)
                .mapToObj(i -> (char) ('a' + i) + "x".repeat(length - 1))
                .toList();
    }
}

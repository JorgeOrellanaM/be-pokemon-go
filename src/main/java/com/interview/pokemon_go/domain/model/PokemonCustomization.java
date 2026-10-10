package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.DomainValidationException;
import com.interview.pokemon_go.domain.exception.FieldViolation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The fields this service owns for a local Pokemon (US03/US04): a localized name, a region and
 * internal classification tags. They come from API clients, so every rule failure is a
 * {@link DomainValidationException} listing all invalid fields at once, never a programming error.
 */
public record PokemonCustomization(String localizedName, String region, List<String> tags) {

    public static final int MAX_TEXT_LENGTH = 100;
    public static final int MAX_TAG_LENGTH = 50;
    public static final int MAX_TAGS = 10;

    private static final String TEXT_TOO_LONG = "must be at most " + MAX_TEXT_LENGTH + " characters";
    private static final String TOO_MANY_TAGS = "must contain at most " + MAX_TAGS + " tags";
    private static final String EMPTY_TAG = "must not contain empty tags";
    private static final String TAG_TOO_LONG = "each tag must be at most " + MAX_TAG_LENGTH + " characters";
    private static final String DUPLICATE_TAG = "must not contain duplicate tags";

    public static final PokemonCustomization NONE = new PokemonCustomization(null, null, List.of());

    /**
     * Surrounding whitespace is stripped, and a text that is blank after stripping means "not set", so
     * clients can clear a field with an empty string as well as with {@code null}.
     */
    public PokemonCustomization {
        Objects.requireNonNull(tags, "tags must not be null");
        localizedName = normalize(localizedName);
        region = normalize(region);
        List<FieldViolation> violations = new ArrayList<>();
        checkLength(localizedName, "localizedName", violations);
        checkLength(region, "region", violations);
        tags = normalizeTags(tags, violations);
        if (!violations.isEmpty()) {
            throw new DomainValidationException(DomainValidationException.CHECK_FIELDS, violations);
        }
    }

    private static String normalize(String text) {
        return Optional.ofNullable(text).map(String::strip).filter(stripped -> !stripped.isEmpty()).orElse(null);
    }

    private static void checkLength(String text, String field, List<FieldViolation> violations) {
        if (text != null && text.length() > MAX_TEXT_LENGTH) {
            violations.add(new FieldViolation(field, TEXT_TOO_LONG));
        }
    }

    /**
     * Tags are labels, so "Starter" and " starter" are the same tag: duplicates are compared without
     * case and surrounding whitespace. Each rule is reported once, however many tags break it.
     */
    private static List<String> normalizeTags(List<String> tags, List<FieldViolation> violations) {
        if (tags.size() > MAX_TAGS) {
            violations.add(new FieldViolation("tags", TOO_MANY_TAGS));
        }
        List<String> normalized = tags.stream().map(PokemonCustomization::normalize).toList();
        List<String> present = normalized.stream().filter(Objects::nonNull).toList();
        if (present.size() < normalized.size()) {
            violations.add(new FieldViolation("tags", EMPTY_TAG));
        }
        if (present.stream().anyMatch(tag -> tag.length() > MAX_TAG_LENGTH)) {
            violations.add(new FieldViolation("tags", TAG_TOO_LONG));
        }
        if (new HashSet<>(present.stream().map(tag -> tag.toLowerCase(Locale.ROOT)).toList()).size() < present.size()) {
            violations.add(new FieldViolation("tags", DUPLICATE_TAG));
        }
        return present;
    }
}

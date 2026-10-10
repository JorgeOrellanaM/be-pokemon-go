package com.interview.pokemon_go.domain.exception;

import java.util.List;

public class DomainValidationException extends DomainException {

    /**
     * Summary for a payload with invalid fields; the fields themselves are listed in the violations.
     */
    public static final String CHECK_FIELDS = "Please check the highlighted fields.";

    private final List<FieldViolation> violations;

    public DomainValidationException(String message) {
        this(message, List.of());
    }

    public DomainValidationException(String message, List<FieldViolation> violations) {
        super(message);
        this.violations = List.copyOf(violations);
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.INVALID_INPUT;
    }

    public List<FieldViolation> violations() {
        return violations;
    }
}

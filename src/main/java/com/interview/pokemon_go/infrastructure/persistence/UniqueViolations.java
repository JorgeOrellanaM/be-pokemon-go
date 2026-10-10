package com.interview.pokemon_go.infrastructure.persistence;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

final class UniqueViolations {

    private UniqueViolations() {
    }

    /**
     * Only a unique-key violation means "already stored"; any other integrity error (e.g. a value too
     * long for its column) is our fault and must stay a 500.
     */
    static boolean isUniqueViolation(DataIntegrityViolationException e) {
        return e.getCause() instanceof ConstraintViolationException violation
                && violation.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE;
    }
}

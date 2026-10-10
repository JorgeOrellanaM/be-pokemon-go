package com.interview.pokemon_go.infrastructure.persistence;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UniqueViolationsTest {

    @Test
    void aUniqueKeyViolationMeansAlreadyStored() {
        assertThat(UniqueViolations.isUniqueViolation(
                integrityError(ConstraintViolationException.ConstraintKind.UNIQUE))).isTrue();
    }

    /**
     * Any other integrity error (not null, value too long...) is a fault on our side and must stay a
     * 500, not be reported as a conflict.
     */
    @Test
    void anyOtherConstraintIsNotAUniqueViolation() {
        assertThat(UniqueViolations.isUniqueViolation(
                integrityError(ConstraintViolationException.ConstraintKind.NOT_NULL))).isFalse();
        assertThat(UniqueViolations.isUniqueViolation(
                integrityError(ConstraintViolationException.ConstraintKind.OTHER))).isFalse();
    }

    @Test
    void anErrorWithoutAConstraintCauseIsNotAUniqueViolation() {
        assertThat(UniqueViolations.isUniqueViolation(new DataIntegrityViolationException("boom"))).isFalse();
        assertThat(UniqueViolations.isUniqueViolation(
                new DataIntegrityViolationException("boom", new SQLException("value too long")))).isFalse();
    }

    private static DataIntegrityViolationException integrityError(ConstraintViolationException.ConstraintKind kind) {
        return new DataIntegrityViolationException("could not execute statement",
                new ConstraintViolationException("violation", new SQLException("duplicate"), kind, "uk_name"));
    }
}

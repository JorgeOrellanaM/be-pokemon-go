package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class WeightTest {

    @Test
    void convertsHectogramsToKilograms() {
        assertThat(new Weight(60).kilograms()).isEqualTo(6.0);
        assertThat(new Weight(905).kilograms()).isEqualTo(90.5);
    }

    @Test
    void acceptsZero() {
        assertThat(new Weight(0).kilograms()).isZero();
    }

    @Test
    void rejectsANegativeWeight() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Weight(-1))
                .withMessage("weight must not be negative");
    }
}

package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class BaseStatTest {

    @Test
    void acceptsValuesWithinRange() {
        assertThat(new BaseStat("hp", 0).value()).isZero();
        assertThat(new BaseStat("hp", BaseStat.MAX_VALUE).value()).isEqualTo(255);
    }

    @Test
    void rejectsNegativeValue() {
        assertThatIllegalArgumentException().isThrownBy(() -> new BaseStat("hp", -1));
    }

    @Test
    void rejectsValueAboveMaximum() {
        assertThatIllegalArgumentException().isThrownBy(() -> new BaseStat("hp", 256));
    }

    @Test
    void rejectsMissingName() {
        assertThatNullPointerException().isThrownBy(() -> new BaseStat(null, 10));
        assertThatIllegalArgumentException().isThrownBy(() -> new BaseStat(" ", 10));
    }
}

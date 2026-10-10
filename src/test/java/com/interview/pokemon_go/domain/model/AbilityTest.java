package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class AbilityTest {

    @Test
    void keepsValidValues() {
        Ability ability = new Ability("lightning-rod", true);

        assertThat(ability.name()).isEqualTo("lightning-rod");
        assertThat(ability.hidden()).isTrue();
    }

    @Test
    void requiresAName() {
        assertThatNullPointerException().isThrownBy(() -> new Ability(null, false));
        assertThatIllegalArgumentException().isThrownBy(() -> new Ability("  ", false));
    }
}

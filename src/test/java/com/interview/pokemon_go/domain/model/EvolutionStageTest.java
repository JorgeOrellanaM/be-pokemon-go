package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EvolutionStageTest {

    @Test
    void supportsBranchingChains() {
        EvolutionStage eevee = new EvolutionStage(133, "eevee", List.of(
                new EvolutionStage(134, "vaporeon", List.of()),
                new EvolutionStage(135, "jolteon", List.of())));

        assertThat(eevee.evolvesTo()).extracting(EvolutionStage::name).containsExactly("vaporeon", "jolteon");
    }

    @Test
    void copiesNextStagesDefensively() {
        List<EvolutionStage> next = new ArrayList<>(List.of(new EvolutionStage(26, "raichu", List.of())));
        EvolutionStage pikachu = new EvolutionStage(25, "pikachu", next);

        next.clear();

        assertThat(pikachu.evolvesTo()).hasSize(1);
    }

    @Test
    void rejectsInvalidComponents() {
        assertThatIllegalArgumentException().isThrownBy(() -> new EvolutionStage(0, "pikachu", List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new EvolutionStage(25, " ", List.of()));
        assertThatNullPointerException().isThrownBy(() -> new EvolutionStage(25, null, List.of()));
        assertThatNullPointerException().isThrownBy(() -> new EvolutionStage(25, "pikachu", null));
    }
}

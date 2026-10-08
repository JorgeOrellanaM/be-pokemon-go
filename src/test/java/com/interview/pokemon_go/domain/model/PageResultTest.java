package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PageResultTest {

    @Test
    void roundsTotalPagesUp() {
        assertThat(new PageResult<>(List.of(), 0, 5, 11).totalPages()).isEqualTo(3);
        assertThat(new PageResult<>(List.of(), 0, 5, 10).totalPages()).isEqualTo(2);
    }

    @Test
    void hasNoPagesWhenEmpty() {
        assertThat(new PageResult<>(List.of(), 0, 20, 0).totalPages()).isZero();
    }

    @Test
    void copiesItemsDefensively() {
        List<String> items = new ArrayList<>(List.of("a"));
        PageResult<String> page = new PageResult<>(items, 0, 1, 1);

        items.add("b");

        assertThat(page.items()).containsExactly("a");
    }

    @Test
    void rejectsNullItems() {
        assertThatNullPointerException().isThrownBy(() -> new PageResult<>(null, 0, 1, 0));
    }

    @Test
    void rejectsNegativePage() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PageResult<>(List.of(), -1, 1, 0));
    }

    @Test
    void rejectsSizeBelowOne() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PageResult<>(List.of(), 0, 0, 0));
    }

    @Test
    void rejectsNegativeTotal() {
        assertThatIllegalArgumentException().isThrownBy(() -> new PageResult<>(List.of(), 0, 1, -1));
    }
}

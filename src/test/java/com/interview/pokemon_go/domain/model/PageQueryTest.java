package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.InvalidPageQueryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageQueryTest {

    @Test
    void computesOffsetFromZeroBasedPage() {
        assertThat(new PageQuery(0, 20).offset()).isZero();
        assertThat(new PageQuery(3, 20).offset()).isEqualTo(60);
    }

    @Test
    void acceptsSizeLimits() {
        assertThat(new PageQuery(0, 1).size()).isEqualTo(1);
        assertThat(new PageQuery(0, PageQuery.MAX_SIZE).size()).isEqualTo(PageQuery.MAX_SIZE);
    }

    @Test
    void rejectsNegativePage() {
        assertThatThrownBy(() -> new PageQuery(-1, 20))
                .isInstanceOf(InvalidPageQueryException.class)
                .hasMessage("page must be greater than or equal to 0");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5, PageQuery.MAX_SIZE + 1})
    void rejectsSizeOutOfRange(int size) {
        assertThatThrownBy(() -> new PageQuery(0, size))
                .isInstanceOf(InvalidPageQueryException.class)
                .hasMessage("size must be between 1 and " + PageQuery.MAX_SIZE);
    }
}

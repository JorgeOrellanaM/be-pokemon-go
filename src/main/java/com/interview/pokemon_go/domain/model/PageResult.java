package com.interview.pokemon_go.domain.model;

import java.util.List;
import java.util.Objects;

public record PageResult<T>(List<T> items, int page, int size, long totalElements) {

    public PageResult {
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be at least 1");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must not be negative");
        }
    }

    /**
     * Rounds up so a partial last page still counts (11 items / size 5 = 3 pages).
     */
    public int totalPages() {
        return (int) Math.ceil((double) totalElements / size);
    }
}

package com.interview.pokemon_go.domain.model;

import java.util.List;
import java.util.Objects;

public record PageResult<T>(List<T> items, int page, int size, long totalElements) {

    public PageResult {
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must not be negative");
        }
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }
}

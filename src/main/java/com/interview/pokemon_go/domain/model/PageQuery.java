package com.interview.pokemon_go.domain.model;

import com.interview.pokemon_go.domain.exception.InvalidPageQueryException;

public record PageQuery(int page, int size) {

    public static final int MAX_SIZE = 50;

    public PageQuery {
        if (page < 0) {
            throw new InvalidPageQueryException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidPageQueryException("size must be between 1 and " + MAX_SIZE);
        }
    }

    /**
     * Number of rows to skip: pages are zero-based, so page 0 starts at row 0.
     */
    public int offset() {
        return page * size;
    }
}

package com.interview.pokemon_go.infrastructure.web;

import java.util.List;

public record PageResponseDTO<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
}

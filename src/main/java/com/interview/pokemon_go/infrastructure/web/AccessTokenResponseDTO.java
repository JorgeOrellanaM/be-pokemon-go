package com.interview.pokemon_go.infrastructure.web;

import java.time.Instant;

/**
 * Sent back on login. Clients pass {@code accessToken} as {@code Authorization: <tokenType> <accessToken>}.
 */
public record AccessTokenResponseDTO(String accessToken, String tokenType, Instant expiresAt) {
}

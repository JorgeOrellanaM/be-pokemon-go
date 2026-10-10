package com.interview.pokemon_go.infrastructure.web;

/**
 * Body of {@code POST /api/v1/auth/register}. The rules are checked by the domain
 * {@link com.interview.pokemon_go.domain.model.Registration}, not here. {@code toString} masks the
 * password so it can never reach a log.
 */
public record RegisterRequestDTO(String username, String password) {

    @Override
    public String toString() {
        return "RegisterRequestDTO[username=" + username + ", password=***]";
    }
}

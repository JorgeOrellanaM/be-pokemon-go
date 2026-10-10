package com.interview.pokemon_go.infrastructure.web;

/**
 * Body of {@code POST /api/v1/auth/login}. {@code toString} masks the password so it can never reach a
 * log.
 */
public record LoginRequestDTO(String username, String password) {

    @Override
    public String toString() {
        return "LoginRequestDTO[username=" + username + ", password=***]";
    }
}

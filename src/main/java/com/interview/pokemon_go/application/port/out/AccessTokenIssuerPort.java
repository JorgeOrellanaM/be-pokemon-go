package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.model.AccessToken;

/**
 * Issues the token an authenticated user sends on protected requests. The token format (JWT) is an
 * infrastructure detail.
 */
public interface AccessTokenIssuerPort {

    AccessToken issue(String username);
}

package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.domain.model.AccessToken;
import com.interview.pokemon_go.domain.model.Credentials;
import com.interview.pokemon_go.domain.model.Registration;
import com.interview.pokemon_go.domain.model.UserAccount;

final class AuthWebMapper {

    static final String TOKEN_TYPE = "Bearer";

    private AuthWebMapper() {
    }

    static Registration toRegistration(RegisterRequestDTO request) {
        return new Registration(request.username(), request.password());
    }

    static Credentials toCredentials(LoginRequestDTO request) {
        return new Credentials(request.username(), request.password());
    }

    /**
     * Only the username leaves the service; the password hash never does.
     */
    static UserResponseDTO toUserResponse(UserAccount account) {
        return new UserResponseDTO(account.username());
    }

    static AccessTokenResponseDTO toTokenResponse(AccessToken token) {
        return new AccessTokenResponseDTO(token.value(), TOKEN_TYPE, token.expiresAt());
    }
}

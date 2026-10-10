package com.interview.pokemon_go.application.port.in;

import com.interview.pokemon_go.domain.model.AccessToken;
import com.interview.pokemon_go.domain.model.Credentials;

public interface AuthenticateUserUseCase {

    AccessToken authenticate(Credentials credentials);
}

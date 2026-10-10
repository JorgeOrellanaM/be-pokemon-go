package com.interview.pokemon_go.application.port.in;

import com.interview.pokemon_go.domain.model.Registration;
import com.interview.pokemon_go.domain.model.UserAccount;

public interface RegisterUserUseCase {

    UserAccount register(Registration registration);
}

package com.interview.pokemon_go.infrastructure.security;

import com.interview.pokemon_go.application.port.out.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder encoder;

    public BCryptPasswordHasherAdapter(PasswordEncoder encoder) {
        this.encoder = Objects.requireNonNull(encoder, "encoder must not be null");
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }
}

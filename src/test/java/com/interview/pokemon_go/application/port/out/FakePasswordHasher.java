package com.interview.pokemon_go.application.port.out;

/**
 * Deterministic stand-in for the BCrypt adapter, so use-case tests stay fast and can tell a hash from
 * the raw password.
 */
public class FakePasswordHasher implements PasswordHasherPort {

    private static final String PREFIX = "hashed:";

    @Override
    public String hash(String rawPassword) {
        return PREFIX + rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordHash.equals(hash(rawPassword));
    }
}

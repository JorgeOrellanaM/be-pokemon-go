package com.interview.pokemon_go.application.port.out;

/**
 * One-way password hashing. The use cases never see how hashes are built, only whether one matches.
 */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}

package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.domain.model.Registration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A registered user. The table is {@code app_user} because {@code user} is reserved in PostgreSQL;
 * the username is a unique business key, never the primary key.
 */
@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    // BCrypt hashes are 60 characters; the margin leaves room for another algorithm's prefix
    private static final int PASSWORD_HASH_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = Registration.MAX_USERNAME_LENGTH)
    private String username;

    @Column(name = "password_hash", nullable = false, length = PASSWORD_HASH_LENGTH)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    UserEntity(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}

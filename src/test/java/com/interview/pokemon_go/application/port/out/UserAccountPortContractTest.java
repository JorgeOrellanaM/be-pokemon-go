package com.interview.pokemon_go.application.port.out;

import com.interview.pokemon_go.domain.exception.UsernameAlreadyExistsException;
import com.interview.pokemon_go.domain.model.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Behaviour every {@link UserAccountPort} implementation must share (LSP), so the in-memory fake used
 * in use-case tests cannot drift from the database adapter. Database-backed runs roll back.
 */
@Transactional
public abstract class UserAccountPortContractTest {

    private static final UserAccount ASH = new UserAccount("ash", "$2a$10$hash");

    private UserAccountPort accounts;

    /**
     * Returns an implementation that holds no users.
     */
    protected abstract UserAccountPort emptyStore();

    @BeforeEach
    void createStore() {
        accounts = emptyStore();
    }

    @Test
    void findsWhatWasSaved() {
        UserAccount saved = accounts.save(ASH);

        assertThat(saved).isEqualTo(ASH);
        assertThat(accounts.findByUsername("ash")).contains(ASH);
        assertThat(accounts.existsByUsername("ash")).isTrue();
    }

    @Test
    void findsNothingForAnUnknownUsername() {
        assertThat(accounts.findByUsername("misty")).isEmpty();
        assertThat(accounts.existsByUsername("misty")).isFalse();
    }

    @Test
    void rejectsASecondAccountWithTheSameUsername() {
        accounts.save(ASH);

        assertThatThrownBy(() -> accounts.save(new UserAccount("ash", "$2a$10$other")))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }
}

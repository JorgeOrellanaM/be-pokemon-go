package com.interview.pokemon_go.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class UserAccountTest {

    @Test
    void requiresAUsernameAndAHash() {
        assertThatNullPointerException().isThrownBy(() -> new UserAccount(null, "hash"));
        assertThatNullPointerException().isThrownBy(() -> new UserAccount("ash", null));
        assertThatIllegalArgumentException().isThrownBy(() -> new UserAccount(" ", "hash"));
        assertThatIllegalArgumentException().isThrownBy(() -> new UserAccount("ash", ""));
    }

    @Test
    void neverPrintsThePasswordHash() {
        assertThat(new UserAccount("ash", "$2a$10$secret").toString()).contains("ash").doesNotContain("secret");
    }

    @Test
    void accessTokenRequiresAValueAndAnExpiry() {
        assertThatNullPointerException().isThrownBy(() -> new AccessToken(null, Instant.EPOCH));
        assertThatNullPointerException().isThrownBy(() -> new AccessToken("token", null));
        assertThatIllegalArgumentException().isThrownBy(() -> new AccessToken(" ", Instant.EPOCH));
    }
}

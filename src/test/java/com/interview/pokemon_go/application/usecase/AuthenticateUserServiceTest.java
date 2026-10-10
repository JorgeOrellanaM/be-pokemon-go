package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.out.FakeAccessTokenIssuer;
import com.interview.pokemon_go.application.port.out.FakePasswordHasher;
import com.interview.pokemon_go.application.port.out.InMemoryUserAccountAdapter;
import com.interview.pokemon_go.domain.exception.InvalidCredentialsException;
import com.interview.pokemon_go.domain.model.AccessToken;
import com.interview.pokemon_go.domain.model.Credentials;
import com.interview.pokemon_go.domain.model.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticateUserServiceTest {

    private final InMemoryUserAccountAdapter accounts = new InMemoryUserAccountAdapter();
    private final FakePasswordHasher hasher = new FakePasswordHasher();
    private final FakeAccessTokenIssuer tokens = new FakeAccessTokenIssuer();
    private final AuthenticateUserService service = new AuthenticateUserService(accounts, hasher, tokens);

    @BeforeEach
    void registerAsh() {
        accounts.save(new UserAccount("ash", hasher.hash("Pokemon123!")));
    }

    @Test
    void issuesATokenForValidCredentials() {
        AccessToken token = service.authenticate(new Credentials("Ash", "Pokemon123!"));

        assertThat(token).isEqualTo(new AccessToken("token-for:ash", FakeAccessTokenIssuer.EXPIRES_AT));
    }

    @Test
    void rejectsAWrongPassword() {
        assertThatThrownBy(() -> service.authenticate(new Credentials("ash", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password.");
    }

    @Test
    void rejectsAnUnknownUserWithTheSameMessage() {
        assertThatThrownBy(() -> service.authenticate(new Credentials("misty", "Pokemon123!")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password.");
    }

    @Test
    void requiresCredentialsAndItsPorts() {
        assertThatNullPointerException().isThrownBy(() -> service.authenticate(null));
        assertThatNullPointerException().isThrownBy(() -> new AuthenticateUserService(null, hasher, tokens));
        assertThatNullPointerException().isThrownBy(() -> new AuthenticateUserService(accounts, null, tokens));
        assertThatNullPointerException().isThrownBy(() -> new AuthenticateUserService(accounts, hasher, null));
    }
}

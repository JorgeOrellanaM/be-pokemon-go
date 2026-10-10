package com.interview.pokemon_go.application.usecase;

import com.interview.pokemon_go.application.port.out.FakePasswordHasher;
import com.interview.pokemon_go.application.port.out.InMemoryUserAccountAdapter;
import com.interview.pokemon_go.domain.exception.UsernameAlreadyExistsException;
import com.interview.pokemon_go.domain.model.Registration;
import com.interview.pokemon_go.domain.model.UserAccount;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegisterUserServiceTest {

    private final InMemoryUserAccountAdapter accounts = new InMemoryUserAccountAdapter();
    private final RegisterUserService service = new RegisterUserService(accounts, new FakePasswordHasher());

    @Test
    void storesTheUserWithAHashedPassword() {
        UserAccount registered = service.register(new Registration("Ash", "Pokemon123!"));

        assertThat(registered).isEqualTo(new UserAccount("ash", "hashed:Pokemon123!"));
        assertThat(accounts.findByUsername("ash")).contains(registered);
    }

    @Test
    void rejectsATakenUsername() {
        service.register(new Registration("ash", "Pokemon123!"));

        assertThatThrownBy(() -> service.register(new Registration("ASH", "Another123!")))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessage("The username 'ash' is already taken");
        assertThat(accounts.findByUsername("ash").orElseThrow().passwordHash()).isEqualTo("hashed:Pokemon123!");
    }

    @Test
    void requiresARegistrationAndItsPorts() {
        assertThatNullPointerException().isThrownBy(() -> service.register(null));
        assertThatNullPointerException().isThrownBy(() -> new RegisterUserService(null, new FakePasswordHasher()));
        assertThatNullPointerException().isThrownBy(() -> new RegisterUserService(accounts, null));
    }
}

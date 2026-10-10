package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.application.port.in.AuthenticateUserUseCase;
import com.interview.pokemon_go.application.port.in.RegisterUserUseCase;
import com.interview.pokemon_go.domain.model.UserAccount;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * User registration and login. Register and login are public; {@code /me} needs a bearer token (see
 * {@code SecurityConfig}).
 */
@RestController
@RequestMapping(AuthController.BASE_PATH)
public class AuthController {

    static final String BASE_PATH = "/api/v1/auth";
    private static final String ME_PATH = "/me";

    private final RegisterUserUseCase registerUser;
    private final AuthenticateUserUseCase authenticateUser;

    public AuthController(RegisterUserUseCase registerUser, AuthenticateUserUseCase authenticateUser) {
        this.registerUser = registerUser;
        this.authenticateUser = authenticateUser;
    }

    /**
     * Creates an account, so the answer is a 201. It points at {@code /me}, the resource that
     * represents the user once they log in.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> register(@RequestBody RegisterRequestDTO request) {
        UserAccount registered = registerUser.register(AuthWebMapper.toRegistration(request));
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path(BASE_PATH + ME_PATH).build().toUri();
        return ResponseEntity.created(location).body(ApiResponseDTO.ok(AuthWebMapper.toUserResponse(registered)));
    }

    @PostMapping("/login")
    public ApiResponseDTO<AccessTokenResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ApiResponseDTO.ok(AuthWebMapper.toTokenResponse(
                authenticateUser.authenticate(AuthWebMapper.toCredentials(request))));
    }

    /**
     * The token was already verified by the resource server; its subject is the username.
     */
    @GetMapping(ME_PATH)
    public ApiResponseDTO<UserResponseDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponseDTO.ok(new UserResponseDTO(jwt.getSubject()));
    }
}

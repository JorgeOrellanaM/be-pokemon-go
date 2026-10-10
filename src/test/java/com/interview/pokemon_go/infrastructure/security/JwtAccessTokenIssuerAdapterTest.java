package com.interview.pokemon_go.infrastructure.security;

import com.interview.pokemon_go.domain.model.AccessToken;
import com.interview.pokemon_go.infrastructure.config.JwtProperties;
import com.interview.pokemon_go.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Round trip through the real Nimbus encoder and decoder built by {@link SecurityConfig}, so the
 * issued token is exactly what the resource server accepts.
 */
class JwtAccessTokenIssuerAdapterTest {

    private static final JwtProperties PROPERTIES =
            new JwtProperties("0123456789abcdef0123456789abcdef", Duration.ofHours(1), "pokemon-go");
    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    private final SecurityConfig config = new SecurityConfig();
    private final JwtAccessTokenIssuerAdapter issuer = new JwtAccessTokenIssuerAdapter(
            config.jwtEncoder(PROPERTIES), PROPERTIES, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void issuesATokenTheResourceServerAccepts() {
        AccessToken token = issuer.issue("ash");

        Jwt jwt = config.jwtDecoder(PROPERTIES).decode(token.value());

        assertThat(jwt.getSubject()).isEqualTo("ash");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("pokemon-go");
        assertThat(jwt.getIssuedAt()).isEqualTo(NOW);
        assertThat(jwt.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
        assertThat(token.expiresAt()).isEqualTo(jwt.getExpiresAt());
    }

    @Test
    void aTokenSignedWithAnotherSecretIsRejected() {
        JwtProperties otherSecret =
                new JwtProperties("another-secret-another-secret-123", Duration.ofHours(1), "pokemon-go");
        JwtDecoder decoder = config.jwtDecoder(otherSecret);

        assertThatThrownBy(() -> decoder.decode(issuer.issue("ash").value())).isInstanceOf(JwtException.class);
    }

    @Test
    void aTokenFromAnotherIssuerIsRejected() {
        JwtProperties otherIssuer =
                new JwtProperties("0123456789abcdef0123456789abcdef", Duration.ofHours(1), "someone-else");
        JwtDecoder decoder = config.jwtDecoder(otherIssuer);

        assertThatThrownBy(() -> decoder.decode(issuer.issue("ash").value())).isInstanceOf(JwtException.class);
    }
}

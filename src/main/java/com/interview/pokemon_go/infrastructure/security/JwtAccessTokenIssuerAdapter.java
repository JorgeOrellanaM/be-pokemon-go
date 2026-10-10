package com.interview.pokemon_go.infrastructure.security;

import com.interview.pokemon_go.application.port.out.AccessTokenIssuerPort;
import com.interview.pokemon_go.domain.model.AccessToken;
import com.interview.pokemon_go.infrastructure.config.JwtProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
public class JwtAccessTokenIssuerAdapter implements AccessTokenIssuerPort {

    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtAccessTokenIssuerAdapter(JwtEncoder encoder, JwtProperties properties, Clock clock) {
        this.encoder = Objects.requireNonNull(encoder, "encoder must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * The username is the token subject; that is all a protected route needs to know who is calling.
     * JWT times have second precision, so they are truncated here to keep {@code expiresAt} identical
     * to the {@code exp} claim.
     */
    @Override
    public AccessToken issue(String username) {
        Instant issuedAt = clock.instant().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plus(properties.ttl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(username)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(token, expiresAt);
    }
}

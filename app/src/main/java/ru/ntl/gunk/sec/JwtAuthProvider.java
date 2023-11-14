package ru.ntl.gunk.sec;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAuthProvider implements AuthenticationProvider {

    private final TokenProvider<DecodedJWT> jwtTokenProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var auth = (JwtAuthentication) authentication;
        var jwt = (DecodedJWT) auth.getCredentials();

        if (jwt == null ||
                jwtTokenProvider.isTokenErased(auth.getPrincipal()) ||
                !jwtTokenProvider.validate(auth.getPrincipal(), jwt) ||
                jwt.getExpiresAt().before(Date.from(Instant.now()))) {
            throw new AuthException("Authentication failed");
        }

        authentication.setAuthenticated(true);
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}

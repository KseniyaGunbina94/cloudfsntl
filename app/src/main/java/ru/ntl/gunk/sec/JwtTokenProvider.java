package ru.ntl.gunk.sec;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.auth0.jwt.algorithms.Algorithm.*;
import static java.time.temporal.ChronoUnit.*;

@Slf4j
@Component
class JwtTokenProvider implements TokenProvider<DecodedJWT> {

    private final String secret;

    private final static ConcurrentHashMap<String, String> TOKEN_STORAGE = new ConcurrentHashMap<>();

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    @Override
    public String generateToken(Authentication auth) {
        String tokenId = UUID.randomUUID().toString();
        String token = JWT.create()
                .withJWTId(tokenId)
                .withIssuer(auth.getName())
                .withExpiresAt(Instant.now().plus(1L, HOURS))
                .sign(HMAC256(secret));
        TOKEN_STORAGE.put(tokenId, token);
        return token;
    }

    @Override
    public DecodedJWT decode(String token) {
        if (Strings.isBlank(token)) {
            log.debug("Empty token");
            return null;
        }
        return JWT.decode(token);
    }

    @Override
    public boolean validate(Object tokenId, DecodedJWT currentToken) {
        var stored = JWT.decode(TOKEN_STORAGE.get((String) tokenId));
        return currentToken.getSignature().equals(stored.getSignature());
    }

    @Override
    public boolean isTokenErased(Object tokenId) {
        return !TOKEN_STORAGE.containsKey((String) tokenId);
    }

    @Override
    public void forceExpire() {
        TOKEN_STORAGE.remove((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}

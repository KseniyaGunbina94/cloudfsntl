package ru.ntl.gunk.sec;

import org.springframework.security.core.Authentication;

public interface TokenProvider<T> {
    String generateToken(Authentication auth);
    T decode(String token);
    boolean isTokenErased(Object tokenId);
    boolean validate(Object tokenId, T currentToken);
    void forceExpire();
}

package ru.ntl.gunk.sec;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import javax.security.auth.Subject;
import java.util.Collections;

public class JwtAuthentication extends AbstractAuthenticationToken {
    private final DecodedJWT jwt;

    public JwtAuthentication(DecodedJWT jwt){
        super(Collections.emptyList());
        this.jwt = jwt;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }

    @Override
    public Object getPrincipal() {
        return jwt.getId();
    }

    @Override
    public boolean implies(Subject subject) {
        return super.implies(subject);
    }
}

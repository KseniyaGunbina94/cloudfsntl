package ru.ntl.gunk.srvs.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.ntl.gunk.dto.LoginRequestDTO;
import ru.ntl.gunk.sec.TokenProvider;
import ru.ntl.gunk.srvs.AuthService;

@Component
@RequiredArgsConstructor
class AuthImpl implements AuthService {

    private final TokenProvider<DecodedJWT> tokenProvider;
    private final AuthenticationManager authenticationManager;
    @Override
    public String login(LoginRequestDTO login) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getLogin(), login.getPassword())
        );
        return tokenProvider.generateToken(authentication);
    }

    @Override
    public void logout() {
        tokenProvider.forceExpire();
    }
}

package ru.ntl.gunk.sec;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class TokenAuthFilter extends OncePerRequestFilter {

    private final TokenProvider<DecodedJWT> tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RequestMatcher tokenAuthMatcher;
    private final SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();
    private final SecurityContextHolderStrategy securityStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final AuthenticationFailureHandler failureHandler;

    @Builder
    public TokenAuthFilter(
            RequestMatcher tokenAuthMatcher,
            AuthenticationManager manager,
            TokenProvider<DecodedJWT> tokenProvider,
            AuthenticationFailureHandler failureHandler
    ) {
        this.tokenAuthMatcher = tokenAuthMatcher;
        this.authenticationManager = manager;
        this.tokenProvider = tokenProvider;
        this.failureHandler = failureHandler;
    }

    private String getTokenFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("auth-token");

        if (StringUtils.hasText(bearerToken)) {
            return bearerToken;
        }
        return null;
    }

    public Authentication attemptAuthentication(HttpServletRequest request)
            throws AuthenticationException {
        String token = getTokenFromRequest(request);
        return authenticationManager.authenticate(new JwtAuthentication(tokenProvider.decode(token)));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!this.tokenAuthMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            try {
                Authentication authenticationResult = attemptAuthentication(request);
                if (authenticationResult == null) {
                    filterChain.doFilter(request, response);
                    return;
                }
                HttpSession session = request.getSession(false);
                if (session != null) {
                    request.changeSessionId();
                }
                successfulAuthentication(request, response, filterChain, authenticationResult);
            }
            catch (AuthenticationException ex){
                this.securityStrategy.clearContext();
                failureHandler.onAuthenticationFailure(request, response, ex);
            }
        }
    }

    private void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                          Authentication authentication) throws ServletException, IOException {
        SecurityContext context = this.securityStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        this.securityStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);
        chain.doFilter(request, response);
    }
}

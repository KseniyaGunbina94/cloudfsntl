package ru.ntl.gunk.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.ntl.gunk.sec.TokenAuthFilter;
import ru.ntl.gunk.sec.TokenProvider;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.*;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider<DecodedJWT> jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final List<AuthenticationProvider> providers;

    private HandlerExceptionResolver resolver;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    public void setResolver(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Autowired
    public void configureGlobal(
            AuthenticationManagerBuilder managerBuilder,
            DaoAuthenticationProvider userPassProvider
    ) {
        providers.forEach(managerBuilder::authenticationProvider);
    }

    @Bean
    public AuthenticationManager manager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager manager
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .userDetailsService(userDetailsService)
                .anonymous(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/cloud/login").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(hndl -> hndl
                        .authenticationEntryPoint(this::handleAuthError))
                .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
                .logout((logout) -> logout
                        .clearAuthentication(true)
                        .permitAll(false));
        RequestMatcher matcher = new AndRequestMatcher(
                new NegatedRequestMatcher(new AntPathRequestMatcher("/cloud/login")),
                AnyRequestMatcher.INSTANCE);

        TokenAuthFilter filter = TokenAuthFilter.builder()
                .tokenAuthMatcher(matcher)
                .manager(manager)
                .tokenProvider(jwtTokenProvider)
                .failureHandler(this::handleAuthError)
                .build();
        http.addFilterBefore(filter, ExceptionTranslationFilter.class);

        return http.build();
    }

    @SneakyThrows
    private void handleAuthError(HttpServletRequest request,
                                 HttpServletResponse response,
                                 AuthenticationException authException) {
        log.warn(authException.getLocalizedMessage(), authException);
        resolver.resolveException(request, response, null, authException);
    }
}

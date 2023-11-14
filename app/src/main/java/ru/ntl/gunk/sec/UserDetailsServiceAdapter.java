package ru.ntl.gunk.sec;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.ntl.gunk.dao.UserRepository;

@Component
@RequiredArgsConstructor
class UserDetailsServiceAdapter implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username).map(UserDetailsAdapter::of)
                .orElseThrow(() -> new AuthException("There is no such user"));
    }
}

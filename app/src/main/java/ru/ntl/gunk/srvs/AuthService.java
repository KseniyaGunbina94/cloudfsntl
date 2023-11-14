package ru.ntl.gunk.srvs;

import ru.ntl.gunk.dto.LoginRequestDTO;

public interface AuthService {
    String login(LoginRequestDTO login);
    void logout();
}

package ru.ntl.gunk.cntrl;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.RequestEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import ru.ntl.gunk.dao.UserRepository;
import ru.ntl.gunk.dao.models.User;
import ru.ntl.gunk.dto.ErrorDTO;
import ru.ntl.gunk.dto.LoginRequestDTO;
import ru.ntl.gunk.dto.LoginResponseDTO;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;
import static ru.ntl.gunk.utils.TestUtils.*;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class SecurityTest {

    @LocalServerPort
    private int serverPort;
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper mapper;

    private RestTemplate restTemplate;
    @LocalServerPort
    public void setRestTemplate(int port){
        this.restTemplate = buildTemplate("http://localhost:" + serverPort + "/cloud");
    }

    @SneakyThrows
    private String requestToken(){
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.of(User.builder()
                .id(0)
                .login("admin")
                .password(passwordEncoder.encode("admin"))
                .build()));
        var loginDto = LoginRequestDTO.builder()
                .login("admin")
                .password("admin")
                .build();
        var response = restTemplate.postForEntity("/login", loginDto, String.class);
        return mapper.readValue(response.getBody(), LoginResponseDTO.class).getAuthToken();
    }

    @SneakyThrows
    @Test
    void login() {
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.of(User.builder()
                .id(0)
                .login("admin")
                .password(passwordEncoder.encode("admin"))
                .build()));
        var loginDto = LoginRequestDTO.builder()
                .login("admin")
                .password("admin")
                .build();
        var response = restTemplate.postForEntity("/login", loginDto, String.class);
        assertEquals(200, response.getStatusCode().value());
        var token = JWT.decode(mapper.readValue(response.getBody(), LoginResponseDTO.class).getAuthToken());
        assertEquals("admin", token.getIssuer());
    }

    @SneakyThrows
    @Test
    void logout(){
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.of(User.builder()
                .id(0)
                .login("admin")
                .password(passwordEncoder.encode("admin"))
                .build()));
        String token = requestToken();
        var request = RequestEntity.post("/logout").header("auth-token", token).build();
        var response = restTemplate.exchange(request, String.class);
        assertEquals(200, response.getStatusCode().value());
        response = restTemplate.exchange(request, String.class);
        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        var respBody = mapper.readValue(response.getBody(), ErrorDTO.class);
        assertNotNull(respBody);
    }

    @Test
    void logoutWithReplacedSign(){
        when(userRepository.findByLogin(anyString())).thenReturn(Optional.of(User.builder()
                .id(0)
                .login("admin")
                .password(passwordEncoder.encode("admin"))
                .build()));
        String token = requestToken();
        String wrongToken = token.replaceAll("\\w+$", "DAJ8yqi-zCTXpnOiIDAPAxCHMm3HiRDDMZ5mg8WJPhS");
        var request = RequestEntity.post("/logout").header("auth-token", wrongToken).build();
        var response = restTemplate.exchange(request, String.class);
        assertEquals(401, response.getStatusCode().value());
    }
}
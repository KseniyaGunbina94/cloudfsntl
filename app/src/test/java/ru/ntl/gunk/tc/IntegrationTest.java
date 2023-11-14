package ru.ntl.gunk.tc;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.ntl.gunk.dao.FileRepository;
import ru.ntl.gunk.dao.UserRepository;
import ru.ntl.gunk.dao.models.User;
import ru.ntl.gunk.dto.ErrorDTO;
import ru.ntl.gunk.dto.FileUploadResponseDTO;
import ru.ntl.gunk.dto.LoginRequestDTO;
import ru.ntl.gunk.dto.LoginResponseDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;
import static ru.ntl.gunk.utils.TestUtils.*;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("tc")
public class IntegrationTest {

    @LocalServerPort
    private int port;

    @ClassRule
    public static PsqlSharedTC postgres = PsqlSharedTC.getInstance();

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FileRepository fileRepository;

    @BeforeEach
    void setup(TestInfo info) {
        userRepository.deleteAll();
        fileRepository.deleteAll();

        if (!info.getTags().contains("login")) {
            userRepository.save(User.builder()
                    .login("admin")
                    .password(encryptedPassword("admin"))
                    .build());
            currentToken = Optional.ofNullable(restTemplate.postForEntity("/login", LoginRequestDTO.builder()
                                    .login("admin")
                                    .password("admin")
                                    .build(),
                            LoginResponseDTO.class).getBody())
                    .map(LoginResponseDTO::getAuthToken)
                    .orElseThrow();
        }
    }

    @PostConstruct
    public void init() {
        this.restTemplate = buildTemplate("http://localhost:" + port + "/cloud");
    }

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;

    private String currentToken;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @Tag("login")
    void loginWithBadCredentials() {
        var resp = restTemplate.postForEntity("/login", LoginRequestDTO.builder().build(), ErrorDTO.class);
        assertEquals(401, resp.getStatusCode().value());
    }

    @Test
    @Tag("login")
    void loginHappyPath() {
        userRepository.save(User.builder()
                .login("admin")
                .password(encryptedPassword("admin"))
                .build());
        var resp = restTemplate.postForEntity("/login", LoginRequestDTO.builder()
                        .login("admin")
                        .password("admin")
                        .build(),
                LoginResponseDTO.class);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertNotNull(resp.getBody().getAuthToken());
    }

    @SneakyThrows
    @Test
    void uploadFile() {
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", getTestFile());

        RequestEntity<LinkedMultiValueMap<String, Object>> req = RequestEntity
                .post("/file?name=test.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("auth-token", currentToken)
                .body(body);

        var resp = restTemplate.exchange(req, FileUploadResponseDTO.class);

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals("File saved with name test.txt", resp.getBody().getMessage());
    }

    @SneakyThrows
    @Test
    void uploadFileNoToken() {
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", getTestFile());

        RequestEntity<LinkedMultiValueMap<String, Object>> req = RequestEntity
                .post("/file?name=test.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body);

        var resp = restTemplate.exchange(req, String.class);
        log.info(resp.getBody());
        assertEquals(401, resp.getStatusCode().value());
    }

    private Resource getTestFile() throws IOException {
        Path testFile = Files.createTempFile("test-file", ".txt");
        System.out.println("Creating and Uploading Test File: " + testFile);
        Files.write(testFile, "Hello World !!, This is a test file.".getBytes());
        return new FileSystemResource(testFile.toFile());
    }
}

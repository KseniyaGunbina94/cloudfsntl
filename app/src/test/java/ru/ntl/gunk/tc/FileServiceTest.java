package ru.ntl.gunk.tc;

import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.ntl.gunk.App;
import ru.ntl.gunk.dto.FileUploadRequestDTO;
import ru.ntl.gunk.srvs.FileService;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@ActiveProfiles({"tc", "tc-auto"})
public class FileServiceTest {

    @ClassRule
    public static PsqlSharedTC tc = PsqlSharedTC.getInstance();

    @Autowired
    private FileService fileService;

    @BeforeAll
    static void beforeAll() {
        tc.start();
    }

    @AfterAll
    static void afterAll() {
        tc.stop();
    }


    @Test
    void uploadFile() {
        var response = fileService.uploadFile(FileUploadRequestDTO.builder()
                .filename("test.txt")
                .file(new MockMultipartFile("test.txt", "Test file content".getBytes()))
                .build());

        assertEquals("File saved with name test.txt", response.getMessage());
    }
}

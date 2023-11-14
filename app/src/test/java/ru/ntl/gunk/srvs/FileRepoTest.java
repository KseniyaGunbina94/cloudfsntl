package ru.ntl.gunk.srvs;

import org.hibernate.engine.jdbc.BlobProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.ntl.gunk.dao.FileRepository;
import ru.ntl.gunk.dao.models.File;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FileRepoTest {

    @Autowired
    private FileRepository fileService;

    @Test
    void saveFile() {
        fileService.save(File.builder()
                .name("test.txt")
                .blob(BlobProxy.generateProxy("Some text to file".getBytes()))
                .hash("some hash")
                .size(17L)
                .build());
    }

    @Test
    void getSlice() {
        for (int i = 0; i < 10; i++){
            fileService.save(File.builder()
                    .name("test"+i+".txt")
                    .blob(BlobProxy.generateProxy(("Some text to"+i+"`th file").getBytes()))
                    .hash("some hash")
                    .size(17L)
                    .build());
        }

        Page<File> files = fileService.findAll(Pageable.ofSize(10));
        assertEquals(10, files.getSize());
    }
}
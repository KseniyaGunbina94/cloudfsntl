package ru.ntl.gunk.cntrl;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.ntl.gunk.dto.FileResponseDTO;
import ru.ntl.gunk.dto.FileUploadRequestDTO;
import ru.ntl.gunk.dto.FileUploadResponseDTO;
import ru.ntl.gunk.dto.ListFilesResponseDTO;
import ru.ntl.gunk.dto.LoginRequestDTO;
import ru.ntl.gunk.dto.LoginResponseDTO;
import ru.ntl.gunk.dto.UpdateFileRequestDTO;
import ru.ntl.gunk.srvs.AuthService;
import ru.ntl.gunk.srvs.FileService;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("/cloud")
public class CloudController {

    @Autowired
    private AuthService authService;
    @Autowired
    private FileService fileService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(LoginResponseDTO.builder()
                .authToken(authService.login(request))
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.ok(null);
    }

    @PostMapping(path = "/file", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDTO> uploadFile(HttpServletRequest request,
                                                            @RequestHeader("auth-token") String authToken,
                                                            @RequestParam("name") String fileName,
                                                            @RequestBody MultipartFile file) {
        return ResponseEntity.ok(
                fileService.uploadFile(FileUploadRequestDTO.builder()
                        .file(file)
                        .filename(fileName)
                        .build())
        );
    }

    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(@RequestHeader("auth-token") String authToken,
                                           @RequestParam String filename) {
        fileService.deleteFile(filename);
        return ResponseEntity.ok(null);
    }

    @GetMapping(value = "/file", produces = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDTO> downloadFile(@RequestHeader("auth-token") String authToken,
                                                        @RequestParam String filename) {
        return ResponseEntity.ok(fileService.downloadFile(filename));
    }

    @PutMapping("/file")
    public ResponseEntity<Void> editFileName(@RequestHeader("auth-token") String authToken,
                                             @RequestParam String filename,
                                             @RequestBody UpdateFileRequestDTO request) {
        fileService.updateFile(filename, request);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/list")
    public ResponseEntity<ListFilesResponseDTO> listFiles(@RequestHeader("auth-token") String authToken,
                                                          @RequestParam int limit) {
        return ResponseEntity.ok(fileService.showFilesSlice(limit));
    }
}

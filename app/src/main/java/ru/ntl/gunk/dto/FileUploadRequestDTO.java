package ru.ntl.gunk.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class FileUploadRequestDTO {
    private String filename;
    private MultipartFile file;
}


package ru.ntl.gunk.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class FileResponseDTO {
    private String hash;
    private Resource file;
}

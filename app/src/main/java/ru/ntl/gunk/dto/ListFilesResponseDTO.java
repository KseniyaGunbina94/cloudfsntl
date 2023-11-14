package ru.ntl.gunk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListFilesResponseDTO {
    public static ListFilesResponseDTO of(List<FileDTO> files){
        return new ListFilesResponseDTO(files);
    }

    private List<FileDTO> files; // Список файлов

    @Data
    @Builder
    public static class FileDTO {
        private String filename;
        private long size;
    }
}

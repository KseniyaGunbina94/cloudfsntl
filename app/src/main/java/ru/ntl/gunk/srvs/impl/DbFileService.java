package ru.ntl.gunk.srvs.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.ntl.gunk.dao.FileRepository;
import ru.ntl.gunk.dao.models.File;
import ru.ntl.gunk.dto.FileResponseDTO;
import ru.ntl.gunk.dto.FileUploadRequestDTO;
import ru.ntl.gunk.dto.FileUploadResponseDTO;
import ru.ntl.gunk.dto.ListFilesResponseDTO;
import ru.ntl.gunk.dto.UpdateFileRequestDTO;
import ru.ntl.gunk.srvs.FileService;

import java.io.ByteArrayInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import static ru.ntl.gunk.dto.ListFilesResponseDTO.*;

@Component
@RequiredArgsConstructor
class DbFileService implements FileService {

    private final FileRepository fileRepository;

    @SneakyThrows
    @Override
    public FileUploadResponseDTO uploadFile(FileUploadRequestDTO dto) {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        var fileBuilder = File.builder()
                .name(dto.getFilename());
        try (DigestInputStream dis = new DigestInputStream(new ByteArrayInputStream(dto.getFile().getBytes()), md5)) {
            fileBuilder.hash(new String(dis.readAllBytes()));
        }
        fileBuilder.blob(BlobProxy.generateProxy(dto.getFile().getBytes()));
        fileBuilder.size(dto.getFile().getSize());
        File file = fileRepository.save(fileBuilder.build());
        return FileUploadResponseDTO.builder()
                .message("File saved with name " + file.getName())
                .build();
    }

    @SneakyThrows
    @Override
    public FileResponseDTO downloadFile(String fileName) {
        return fileRepository.findByName(fileName)
                .map(this::fromEntity)
                .orElseThrow();
    }

    @SneakyThrows
    private FileResponseDTO fromEntity(File file) {
        return FileResponseDTO.builder()
                .file(new InputStreamResource(file.getBlob().getBinaryStream()))
                .hash(file.getHash())
                .build();
    }

    @Override
    public void deleteFile(String fileName) {
        fileRepository.deleteByName(fileName).orElseThrow();
    }

    @Override
    public void updateFile(String fileName, UpdateFileRequestDTO dto) {
        var file = fileRepository.findByName(fileName).orElseThrow();
        file.setName(dto.getName());
        fileRepository.save(file);
    }

    @Override
    public ListFilesResponseDTO showFilesSlice(int limit) {
        return ListFilesResponseDTO.of(fileRepository.findAll(Pageable.ofSize(limit)).map(f -> FileDTO.builder()
                .filename(f.getName())
                .size(f.getSize())
                .build()).getContent());
    }
}

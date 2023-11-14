package ru.ntl.gunk.srvs;

import ru.ntl.gunk.dto.*;

public interface FileService {
    FileUploadResponseDTO uploadFile(FileUploadRequestDTO dto);
    FileResponseDTO downloadFile(String fileName);
    void deleteFile(String fileName);
    void updateFile(String fileName, UpdateFileRequestDTO dto);
    ListFilesResponseDTO showFilesSlice(int limit);
}

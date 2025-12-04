package org.recolnat.collection.manager.service;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface UploadFileService {
    String uploadFile(String fileName, Path directoryPath, MultipartFile file);

    byte[] getFileBytes(Path filePath) throws IOException;

    MediaType getImageMediaType(String filename);
}

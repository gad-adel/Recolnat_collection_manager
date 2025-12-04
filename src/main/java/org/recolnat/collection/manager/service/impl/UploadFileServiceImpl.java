package org.recolnat.collection.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.util.FileUtil;
import org.recolnat.collection.manager.service.UploadFileService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadFileServiceImpl implements UploadFileService {
    @Override
    public String uploadFile(String fileName, Path directoryPath, MultipartFile multipartFile) {
        String url = null;
        try {
            if (fileName != null) {
                FileUtil.createFile(directoryPath.resolve(Path.of(fileName)), multipartFile);
                url = fileName;
            }
        } catch (IOException e) {
            throw new CollectionManagerBusinessException(ErrorCode.ERR_NFE_CODE, "Erreur lors de l'upload du fichier");
        }
        return url;
    }

    @Override
    public byte[] getFileBytes(Path filePath) throws IOException {
        File file = filePath.toFile();
        byte[] byteArray = new byte[(int) file.length()];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            inputStream.read(byteArray);
        }
        return byteArray;
    }

    @Override
    public MediaType getImageMediaType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        String extension = "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            extension = filename.substring(lastDot + 1).toLowerCase();
        }

        String contentType = switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            case "tiff", "tif" -> "image/tiff";
            default -> "application/octet-stream";
        };
        return MediaType.parseMediaType(contentType);
    }
}

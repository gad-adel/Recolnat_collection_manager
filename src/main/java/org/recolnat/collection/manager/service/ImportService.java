package org.recolnat.collection.manager.service;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.ImportCheckResponseDTO;
import io.recolnat.model.ImportPageResponseDTO;
import org.recolnat.collection.manager.common.util.FileUtil;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ImportService {

    ImportCheckDataResponseDTO checkSpecimens(MultipartFile file, UUID institutionId);

    ImportCheckResponseDTO check(UUID institutionId, MultipartFile specimen, MultipartFile determination, MultipartFile publication);

    ImportCheckDataResponseDTO checkDeterminations(MultipartFile file, UUID institutionId);

    ImportCheckDataResponseDTO checkPublications(MultipartFile file, UUID institutionId);

    void run();

    void validate(UUID institutionId, MultipartFile specimen, MultipartFile determination, MultipartFile publication, String importMode);

    ImportPageResponseDTO getAllImports(UUID institutionId, Integer page, Integer size);

    FileUtil.FileResource getImportFile(UUID fileId);
}

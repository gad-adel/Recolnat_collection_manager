package org.recolnat.collection.manager.service;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.ImportCheckResponseDTO;
import io.recolnat.model.ImportPageResponseDTO;
import io.recolnat.model.ImportStructureErrorDTO;
import org.recolnat.collection.manager.common.util.FileUtil;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface ImportService {

    ImportCheckDataResponseDTO checkFileCommonProperties(MultipartFile file, UUID institutionId);

    ImportCheckResponseDTO check(UUID institutionId, MultipartFile specimen, MultipartFile determination, MultipartFile publication);

    void run();

    void validate(UUID institutionId, MultipartFile specimen, MultipartFile determination, MultipartFile publication, String importMode);

    void checkDuplicateColumns(String[] columns, List<ImportStructureErrorDTO> errors);

    void hasAllColumnsInList(String[] columns, List<ImportStructureErrorDTO> errors, Predicate<String> predicate);

    void checkRequiredColumnNames(String[] columns, List<ImportStructureErrorDTO> errors, List<String> requiredFields);

    ImportPageResponseDTO getAllImports(UUID institutionId, Integer page, Integer size);

    FileUtil.FileResource getImportFile(UUID fileId);

    void unpublish(UUID importId);
}

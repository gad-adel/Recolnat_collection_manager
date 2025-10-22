package org.recolnat.collection.manager.web;

import io.recolnat.api.ImportApi;
import io.recolnat.model.ImportCheckResponseDTO;
import io.recolnat.model.ImportPageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.util.FileUtil;
import org.recolnat.collection.manager.service.ImportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImportResource implements ImportApi {
    private final ImportService importService;

    @Override
    public ResponseEntity<ImportPageResponseDTO> getAllImports(UUID institutionId, Integer page, Integer size) {
        var result = importService.getAllImports(institutionId, page, size);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<ImportCheckResponseDTO> check(UUID institutionId, MultipartFile specimen, MultipartFile determination, MultipartFile publication) {
        var result = importService.check(institutionId, specimen, determination, publication);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Void> validate(UUID institutionId, MultipartFile specimen, MultipartFile determination, MultipartFile publication,
                                         String importMode) {
        importService.validate(institutionId, specimen, determination, publication, importMode);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Resource> getImportFile(UUID fileId) {
        var file = importService.getImportFile(fileId);

        if (file == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return new ResponseEntity<>(file.inputStreamResource(), FileUtil.getHttpHeaders(file.filename()), HttpStatus.OK);
    }

    // TODO DTH API annulation d'un import
}

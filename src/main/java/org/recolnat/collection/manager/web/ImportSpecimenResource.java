package org.recolnat.collection.manager.web;

import io.recolnat.api.ImportSpecimenApi;
import io.recolnat.model.ImportCheckDataResponseDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImportSpecimenResource implements ImportSpecimenApi {
    private final ImportService importService;

    @Override
    public ResponseEntity<ImportCheckDataResponseDTO> checkSpecimens(MultipartFile file, UUID institutionId) {
        var result = importService.checkSpecimens(file, institutionId);
        return ResponseEntity.ok(result);
    }

}

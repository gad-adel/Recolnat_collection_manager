package org.recolnat.collection.manager.web;

import io.recolnat.api.ImportIdentificationApi;
import io.recolnat.model.ImportCheckDataResponseDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.service.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImportIdentificationResource implements ImportIdentificationApi {
    private final ImportService importService;

    @Override
    public ResponseEntity<ImportCheckDataResponseDTO> checkIdentifications(MultipartFile file, UUID institutionId) {
        var result = importService.checkDeterminations(file, institutionId);
        return ResponseEntity.ok(result);
    }

}

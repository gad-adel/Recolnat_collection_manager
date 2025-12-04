package org.recolnat.collection.manager.web;

import io.recolnat.api.ImportIdentificationApi;
import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.TemplateStructureDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.service.ImportIdentificationService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImportIdentificationResource implements ImportIdentificationApi {
    private final ImportIdentificationService importIdentificationService;

    @Override
    public ResponseEntity<ImportCheckDataResponseDTO> checkIdentifications(MultipartFile file, UUID institutionId) {
        var result = importIdentificationService.checkDeterminations(file, institutionId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<TemplateStructureDTO>> getIdentificationTemplateStructure() {
        var result = importIdentificationService.getTemplateStructure();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<String> generateIdentificationTemplate(List<String> columns) {
        String content;
        try {
            content = importIdentificationService.generateIdentificationTemplate(columns);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("template-identification.csv").build());

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}

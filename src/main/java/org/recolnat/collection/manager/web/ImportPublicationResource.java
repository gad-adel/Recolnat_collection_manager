package org.recolnat.collection.manager.web;

import io.recolnat.api.ImportPublicationApi;
import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.TemplateStructureDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.service.ImportPublicationService;
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
public class ImportPublicationResource implements ImportPublicationApi {
    private final ImportPublicationService importPublicationService;

    @Override
    public ResponseEntity<ImportCheckDataResponseDTO> checkPublications(MultipartFile file, UUID institutionId) {
        var result = importPublicationService.checkPublications(file, institutionId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<TemplateStructureDTO>> getPublicationTemplateStructure() {
        var result = importPublicationService.getTemplateStructure();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<String> generatePublicationTemplate(List<String> columns) {
        String content;
        try {
            content = importPublicationService.generatePublicationTemplate(columns);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("template-publication.csv").build());

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}

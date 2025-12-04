package org.recolnat.collection.manager.web;

import io.recolnat.api.ImportSpecimenApi;
import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.TemplateStructureDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.service.ImportSpecimenService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImportSpecimenResource implements ImportSpecimenApi {
    private final ImportSpecimenService importSpecimenService;

    @Override
    public ResponseEntity<ImportCheckDataResponseDTO> checkSpecimens(MultipartFile file, UUID institutionId) {
        var result = importSpecimenService.checkSpecimens(file, institutionId);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<List<TemplateStructureDTO>> getSpecimenTemplateStructure() {
        var result = importSpecimenService.getTemplateStructure();
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<String> generateSpecimenTemplate(List<String> columns) {
        String content;
        content = importSpecimenService.generateSpecimenTemplate(columns);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("template-specimen.csv").build());

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}

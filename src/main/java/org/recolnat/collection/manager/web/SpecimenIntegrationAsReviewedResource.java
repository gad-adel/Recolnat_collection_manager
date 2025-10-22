package org.recolnat.collection.manager.web;

import io.recolnat.api.SpecimenIntegrationAsReviewedApi;
import io.recolnat.model.SpecimenIntegrationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.SpecimenMapper;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SpecimenIntegrationAsReviewedResource implements SpecimenIntegrationAsReviewedApi {

    private final SpecimenIntegrationService specimenIntegrationService;
    private final SpecimenMapper specimenMapper;

    @Override
    public ResponseEntity<Void> addSpecimenAsReviewed(SpecimenIntegrationRequestDTO specimenIntegrationRequestDTO) {
        log.info("Your request : {}", specimenIntegrationRequestDTO);

        try {
            // Test d'existence d'un doublon avec le même couple collection/catalogNumber
            if (specimenIntegrationService.exists(specimenIntegrationRequestDTO.getCollectionId(), specimenIntegrationRequestDTO.getCatalogNumber(), null)) {
                throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, "duplicate");
            }
            var specimen = specimenMapper.mapDtoToSpecimen(specimenIntegrationRequestDTO);
            var collectionIdentifier = specimenIntegrationService.addAsReviewed(specimen);
            var uri = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/v1/specimens/" + collectionIdentifier.getSpecimenId())
                    .build().toUri();
            return ResponseEntity.created(uri)
                    .header("specimenId", collectionIdentifier.getSpecimenId().toString())
                    .build();
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

}

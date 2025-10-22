package org.recolnat.collection.manager.web;

import io.recolnat.api.SpecimenManagementApi;
import io.recolnat.model.SpecimenIntegrationRequestDTO;
import jakarta.validation.Valid;
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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SpecimenIntegrationUpdateResource implements SpecimenManagementApi {

    public static final String SPECIMEN_ID = "specimenId";
    public static final String COLLECTION_ID = "collectionId";
    private final SpecimenIntegrationService specimenIntegrationService;

    private final SpecimenMapper specimenMapper;

    @Override
    public ResponseEntity<Void> updateSpecimen(UUID specimenId, SpecimenIntegrationRequestDTO specimenIntegrationRequestDTO) {
        try {
            final var specimen = specimenMapper.mapDtoToSpecimen(specimenIntegrationRequestDTO);

            // Test d'existence d'un doublon avec le même couple collection/catalogNumber
            if (specimenIntegrationService.exists(specimenIntegrationRequestDTO.getCollectionId(), specimenIntegrationRequestDTO.getCatalogNumber(), specimenId)) {
                throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, "duplicate");
            }

            log.info("Specimen mapping result : {}", specimen);
            final var id = specimenIntegrationService.update(specimenId, specimen);

            return ResponseEntity.ok().header(SPECIMEN_ID, id.getSpecimenId().toString()).header(
                    COLLECTION_ID, id.getCollectionId().toString()).build();

        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> updateSpecimenAsDraft(UUID specimenId, SpecimenIntegrationRequestDTO specimenIntegrationRequestDTO) {
        log.info("Your request : {}", specimenIntegrationRequestDTO);
        try {
            // Test d'existence d'un doublon avec le même couple collection/catalogNumber
            if (specimenIntegrationService.exists(specimenIntegrationRequestDTO.getCollectionId(), specimenIntegrationRequestDTO.getCatalogNumber(), specimenId)) {
                throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, "duplicate");
            }

            final var specimen = specimenMapper.mapDtoToSpecimen(specimenIntegrationRequestDTO);
            log.info("Specimen mapping result : {}", specimen);
            final var id = specimenIntegrationService.updateAsDraft(specimenId, specimen);
            return ResponseEntity.ok().header(SPECIMEN_ID, id.getSpecimenId().toString()).header(
                    COLLECTION_ID, id.getCollectionId().toString()).build();

        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> updateSpecimenAsReviewed(UUID specimenId, @Valid SpecimenIntegrationRequestDTO specimenIntegrationRequestDTO) {
        log.info("Your request specimenAsReviewed: {}", specimenIntegrationRequestDTO);
        try {
            // Test d'existence d'un doublon avec le même couple collection/catalogNumber
            if (specimenIntegrationService.exists(specimenIntegrationRequestDTO.getCollectionId(), specimenIntegrationRequestDTO.getCatalogNumber(), specimenId)) {
                throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, "duplicate");
            }

            final var specimen = specimenMapper.mapDtoToSpecimen(specimenIntegrationRequestDTO);
            log.info("Specimen reviewed mapping result : {}", specimen);
            final var id = specimenIntegrationService.updateAsReviewed(specimenId, specimen);
            return ResponseEntity.ok().header(SPECIMEN_ID, id.getSpecimenId().toString()).header(
                    COLLECTION_ID, id.getCollectionId().toString()).build();

        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> deleteSpecimen(UUID specimenId) {
        try {
            specimenIntegrationService.deleteSpecimen(specimenId);
            log.info("Delete specimen {}", specimenId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

}

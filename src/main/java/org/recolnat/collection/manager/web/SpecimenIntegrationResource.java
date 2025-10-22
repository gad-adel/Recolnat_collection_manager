package org.recolnat.collection.manager.web;


import io.recolnat.api.SpecimenIntegrationApi;
import io.recolnat.model.SpecimenIntegrationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.SpecimenMapper;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;


@RestController
@RequiredArgsConstructor
@Slf4j
public class SpecimenIntegrationResource implements SpecimenIntegrationApi, WebCommonApi {
    private final SpecimenIntegrationService specimenIntegrationService;
    private final SpecimenMapper specimenMapper;

    @Override
    public ResponseEntity<Void> addSpecimen(SpecimenIntegrationRequestDTO specimenIntegrationRequestDTO) {
        try {
            log.info("Your request : {}", specimenIntegrationRequestDTO);

            // Test d'existence d'un doublon avec le même couple collection/catalogNumber
            if (specimenIntegrationService.exists(specimenIntegrationRequestDTO.getCollectionId(), specimenIntegrationRequestDTO.getCatalogNumber(), null)) {
                throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, "duplicate");
            }

            final var specimen = specimenMapper.mapDtoToSpecimen(specimenIntegrationRequestDTO);
            final var id = specimenIntegrationService.add(specimen);
            final var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/" + id.getSpecimenId())
                    .build()
                    .toUriString();

            return ResponseEntity.status(CREATED).header(HttpHeaders.LOCATION, uri)
                    .header("specimenId", id.getSpecimenId().toString()).header(
                            "collectionId", id.getCollectionId().toString()).build();

        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }

    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return SpecimenIntegrationApi.super.getRequest();
    }

}

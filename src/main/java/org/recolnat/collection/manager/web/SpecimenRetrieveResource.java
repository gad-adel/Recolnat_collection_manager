package org.recolnat.collection.manager.web;

import io.recolnat.api.RetrieveIntegrationInfosApi;
import io.recolnat.model.AdminSpecimenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.SpecimenDtoMapper;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SpecimenRetrieveResource implements RetrieveIntegrationInfosApi {
    private final SpecimenIntegrationService specimenIntegrationService;
    private final SpecimenDtoMapper specimenDtoMapper;

    @Operation(summary = "Indique s'il existe des spécimens à publier visible de l'utilisateur connecté")
    @GetMapping(value = "/v1/specimens/has-to-publish", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Boolean> hasSpecimentToPublish(
            @Parameter(name = "institutionId", description = "Identification de l'institution à filrer", in = ParameterIn.QUERY)
            @Valid @RequestParam(value = "institutionId", required = false) UUID institutionId
    ) {
        Boolean exists = specimenIntegrationService.hasSpecimentToPublish(institutionId);
        return ResponseEntity.ok(exists);
    }

    @Override
    public ResponseEntity<AdminSpecimenDTO> getSpecimenById(UUID specimenId) {
        log.info("Your request to retrieve specimen info with specimenId: {}", specimenId);
        try {
            final var specimen = specimenIntegrationService.getSpecimenById(specimenId);
            log.info("Your response of the specimen retrieved info : {}", specimen);
            return new ResponseEntity<>(specimenDtoMapper.specimenToAdminSpecimenDto(specimen), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<String>> getCountriesByPrefix(String q, Integer size) {
        try {
            var result = specimenIntegrationService.getCountriesByPrefix(q, size);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<String>> getContinentsByPrefix(String q, Integer size) {
        try {
            var result = specimenIntegrationService.getContinentsByPrefix(q, size);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<String>> getRecordersByPrefix(String q, Integer size) {
        try {
            var result = specimenIntegrationService.getRecordersByPrefix(q, size);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Boolean> exists(UUID collectionId, String catalogNumber, UUID specimenId) {
        boolean isUsed = specimenIntegrationService.exists(collectionId, catalogNumber, specimenId);
        return ResponseEntity.ok(isUsed);
    }

}




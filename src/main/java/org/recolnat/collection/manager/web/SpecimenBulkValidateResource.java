package org.recolnat.collection.manager.web;

import io.recolnat.api.BulkValidateApi;
import io.recolnat.model.CollectionSpecificationIdPairsDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.CollectionIdentifierMapper;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class SpecimenBulkValidateResource implements BulkValidateApi {
    private final SpecimenIntegrationService specimenIntegrationService;
    private final CollectionIdentifierMapper identifierMapper;

    @Override
    public ResponseEntity<Void> bulkValidate(List<CollectionSpecificationIdPairsDTO> idPairsDTOS) {

        try {
            final var identifiers = idPairsDTOS.stream().map(identifierMapper::toCollectionIdentifier).toList();
            final var identifierList = specimenIntegrationService.bulkValidate(identifiers);
            final var uris = identifierList.stream().map(identifier ->
                    ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/collections")
                            .path("/" + identifier.getCollectionId())
                            .path("/specimens")
                            .path("/" + identifier.getSpecimenId())
                            .build()
                            .toUriString()

            ).toList();
            return ResponseEntity.status(OK).header("CollectionIdentifiers", identifierList.toString())
                    .header(HttpHeaders.LOCATION, uris.toString()).build();
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }
}

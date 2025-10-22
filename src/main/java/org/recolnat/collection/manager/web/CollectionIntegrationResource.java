package org.recolnat.collection.manager.web;

import io.recolnat.api.CollectionIntegrationApi;
import io.recolnat.model.CollectionIntegrationRequestDTO;
import io.recolnat.model.CollectionUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.CollectionMapper;
import org.recolnat.collection.manager.service.CollectionIntegrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CollectionIntegrationResource implements CollectionIntegrationApi {

    private final CollectionIntegrationService collectionIntegrationService;
    private final CollectionMapper collectionMapper;

    @Override
    public ResponseEntity<Void> updateCollection(UUID collectionId, CollectionUpdateDTO collectionUpdateDTO) {
        try {
            final var id = collectionIntegrationService.updateCollection(collectionId, collectionUpdateDTO);
            return new ResponseEntity<>(setHeader(id), HttpStatus.CREATED);
        } catch (CollectionManagerBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> addCollection(@Valid CollectionIntegrationRequestDTO collectionIntegrationRequestDTO) {
        try {
            final var col = collectionMapper.toCollectionCreate(collectionIntegrationRequestDTO);
            final var collectionId = collectionIntegrationService.addCollection(col);
            return new ResponseEntity<>(setHeader(collectionId), HttpStatus.CREATED);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> deleteCollection(UUID collectionId) {
        collectionIntegrationService.deleteCollection(collectionId);

        return ResponseEntity.ok().build();
    }

    private HttpHeaders setHeader(UUID collectionId) {
        var headers = new HttpHeaders();
        headers.add("collectionId", collectionId.toString());
        return headers;
    }

}

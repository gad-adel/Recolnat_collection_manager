package org.recolnat.collection.manager.web;

import io.recolnat.api.SpecimenMediaApi;
import io.recolnat.api.SpecimenUpdateMediaApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.service.MediaIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
@Slf4j
public class SpecimenMediaResource implements SpecimenMediaApi, SpecimenUpdateMediaApi {

    private final MediaIntegrationService mediaIntegrationService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return SpecimenMediaApi.super.getRequest();
    }

    @Override
    public ResponseEntity<List<String>> saveMedia(UUID specimenId, List<MultipartFile> fileName) {
        log.info("save Media as Valid");
        try {
            return new ResponseEntity<>(mediaIntegrationService.add(specimenId, fileName), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<String>> saveMediaDraft(UUID specimenId,
                                                       List<MultipartFile> fileName) {
        log.info("save Media as Draft");
        try {
            return new ResponseEntity<>(mediaIntegrationService.addDraft(specimenId, fileName), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<String>> saveMediaReviewed(UUID specimenId,
                                                          List<MultipartFile> fileName) {
        log.info("save Media as Reviewed");
        try {
            return new ResponseEntity<>(mediaIntegrationService.addReviewed(specimenId, fileName), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }
}

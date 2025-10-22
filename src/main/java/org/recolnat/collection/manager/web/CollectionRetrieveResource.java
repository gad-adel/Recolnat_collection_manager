package org.recolnat.collection.manager.web;

import io.recolnat.api.RetrieveCollectionsApi;
import io.recolnat.model.CollectionDashboardPageResponseDTO;
import io.recolnat.model.CollectionDetailDTO;
import io.recolnat.model.CollectionOptionDTO;
import io.recolnat.model.CollectionResponseDTO;
import io.recolnat.model.CollectionWithCodeDTO;
import io.recolnat.model.NominativeCollectionDashboardPageResponseDTO;
import io.recolnat.model.UserCollectionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.Collection;
import org.recolnat.collection.manager.api.domain.CollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.NominativeCollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.Result;
import org.recolnat.collection.manager.api.domain.enums.LanguageEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.CollectionMapper;
import org.recolnat.collection.manager.service.CollectionRetrieveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@Slf4j
public class CollectionRetrieveResource implements RetrieveCollectionsApi {

    private final CollectionRetrieveService collectionRetrieveService;
    private final CollectionMapper collectionMapper;

    @Override
    public ResponseEntity<CollectionDetailDTO> getCollection(UUID collectionId) {
        try {
            final var collectionDetail = collectionRetrieveService.findCollectionDetailById(collectionId);
            return new ResponseEntity<>(collectionDetail, HttpStatus.OK);
        } catch (CollectionManagerBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<CollectionDashboardPageResponseDTO> getCollections(UUID institutionId, Integer page, Integer size, String lng, String searchTerm) {
        try {
            boolean isFr = LanguageEnum.FR.name().equalsIgnoreCase(lng);

            Result<CollectionDashboardProjection> collections = collectionRetrieveService.retreiveCollectionsByInstitution(institutionId, page, size, searchTerm, isFr);
            CollectionDashboardPageResponseDTO dto = new CollectionDashboardPageResponseDTO();
            dto.setData(collectionMapper.collectionsToCollectionsDashboardResponseDTO(collections.getData(), isFr));
            dto.setTotalPages(collections.getTotalPages());
            dto.setNumberOfElements((int) collections.getNumberOfElements());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<NominativeCollectionDashboardPageResponseDTO> getNominativeCollectionsByInstitution(UUID institutionId, Integer page, Integer size,
                                                                                                              String searchTerm) {
        try {
            Result<NominativeCollectionDashboardProjection> collections = collectionRetrieveService.retreiveNominativeCollectionsByInstitution(institutionId, page, size, searchTerm);
            NominativeCollectionDashboardPageResponseDTO dto = new NominativeCollectionDashboardPageResponseDTO();
            dto.setData(collectionMapper.toNominativeCollectionsDashboardResponseDTO(collections.getData()));
            dto.setTotalPages(collections.getTotalPages());
            dto.setNumberOfElements((int) collections.getNumberOfElements());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<CollectionResponseDTO>> fetchAllCollections() {
        try {
            final var retreiveAllCollections = collectionRetrieveService.retreiveAllCollections();
            return new ResponseEntity<>(mapCollectionList(retreiveAllCollections), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    private List<CollectionResponseDTO> mapCollectionList(List<Collection> collectionList) {
        return collectionList.stream()
                .map(collectionMapper::collectionToCollectionResponseDTO).toList();
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return RetrieveCollectionsApi.super.getRequest();
    }

    @Override
    public ResponseEntity<List<CollectionOptionDTO>> getCollectionOptions(String lng, UUID institutionId) {
        try {
            boolean isFr = LanguageEnum.FR.name().equalsIgnoreCase(lng);
            final var result = collectionMapper.collectionToCollectionOptionDTO(collectionRetrieveService.findAllOptions(institutionId), isFr).stream()
                    .sorted(Comparator.comparing(CollectionOptionDTO::getName, String.CASE_INSENSITIVE_ORDER)).toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<CollectionWithCodeDTO>> getCollectionsWithCode(String lng, UUID institutionId) {
        try {
            boolean isFr = LanguageEnum.FR.name().equalsIgnoreCase(lng);
            final var result = collectionMapper.collectionToCollectionWithCodeDTO(collectionRetrieveService.findAllOptions(institutionId), isFr).stream()
                    .sorted(Comparator.comparing(CollectionWithCodeDTO::getName, String.CASE_INSENSITIVE_ORDER)).toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<UserCollectionDTO>> getCollectionVisibles() {
        try {
            final var result = collectionRetrieveService.findUserCollections();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }
}

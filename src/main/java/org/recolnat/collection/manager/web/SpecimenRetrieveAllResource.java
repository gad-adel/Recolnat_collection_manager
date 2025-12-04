package org.recolnat.collection.manager.web;


import io.recolnat.api.SpecimenApi;
import io.recolnat.model.OperationTypeDTO;
import io.recolnat.model.SpecimenIntegrationMergeRequestDTO;
import io.recolnat.model.SpecimenIntegrationPageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.api.domain.AdminSpecimenListFilter;
import org.recolnat.collection.manager.api.domain.SpecimenPage;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.SpecimenDtoMapper;
import org.recolnat.collection.manager.common.mapper.SpecimenMergeMapper;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SpecimenRetrieveAllResource implements SpecimenApi {

    private final SpecimenDtoMapper specimenDtoMapper;
    private final SpecimenIntegrationService specimenIntegrationService;
    private final SpecimenMergeMapper specimenMergeMapper;

    @Override
    public ResponseEntity<SpecimenIntegrationPageResponseDTO> retrieveAllSpecimen(Integer page, Integer size, String q, OperationTypeDTO state,
                                                                                  Boolean currentDetermination, Boolean levelType, String columnSort,
                                                                                  String typeSort, UUID institutionId, UUID collectionId, String collectionCode,
                                                                                  String family, String genus, String specificEpithet, String startDate,
                                                                                  String endDate, String collector, List<String> continent, String country,
                                                                                  String nominativeCollection, UUID importId, String storageName) {
        try {

            AdminSpecimenListFilter filters = AdminSpecimenListFilter.builder()
                    .searchTerm(q)
                    .state(state)
                    .currentDetermination(currentDetermination)
                    .levelType(levelType)
                    .institutionId(institutionId)
                    .collectionId(collectionId)
                    .collectionCode(collectionCode)
                    .family(family)
                    .genus(genus)
                    .specificEpithet(specificEpithet)
                    .startDate(startDate)
                    .endDate(endDate)
                    .collector(collector)
                    .continent(continent)
                    .country(country)
                    .nominativeCollection(nominativeCollection)
                    .importId(importId)
                    .storageName(storageName)
                    .build();

            SpecimenPage allSpecimen = specimenIntegrationService.getAllSpecimen(page, size, columnSort, typeSort, filters);
            var collectSpecDto = allSpecimen.getSpecimen().stream().map(specimenDtoMapper::specimensListToSpecimenListDto).toList();
            SpecimenIntegrationPageResponseDTO specPageDto = new SpecimenIntegrationPageResponseDTO().specimenListResponse(collectSpecDto)
                    .numberOfElements(allSpecimen.getNumberOfElements())
                    .totalPages(allSpecimen.getTotalPages());
            return new ResponseEntity<>(specPageDto, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<UUID>> updateMultipleSpecimen(List<UUID> id,
                                                             SpecimenIntegrationMergeRequestDTO specimenIntegrationMergeRequestDTO) {
        try {
            var specimenMerge = specimenMergeMapper.dtoTo(specimenIntegrationMergeRequestDTO);
            var updateMultipleSpecimen = specimenIntegrationService.updateMultipleSpecimen(id, specimenMerge);
            var allId = updateMultipleSpecimen.stream().map(UUID::toString).toList();
            var header = new HttpHeaders();
            header.addAll("specimenId", allId);
            return new ResponseEntity<>(header, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<String>> getNominativeCollections(String query, Integer size) {
        try {
            var nominativeCollections = specimenIntegrationService.getNominativeCollections(query, size);

            return new ResponseEntity<>(nominativeCollections, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<String>> getStorageNames(String query, Integer size) {
        try {
            var items = specimenIntegrationService.getStorageNames(query, size);

            return new ResponseEntity<>(items, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }
}

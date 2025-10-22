package org.recolnat.collection.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecimenIntegrationRule {
    private final CollectionJPARepository collectionJPARepository;

    private final SpecimenJPARepository specimenJPARepository;

    public CollectionJPA checkCollectionExist(UUID collectionId) {
        return collectionJPARepository.findById(collectionId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        "Collection with id :" + collectionId + SpecimenIntegrationServiceImpl.NOT_FOUND));
    }

    public SpecimenJPA checkSpecimenExist(UUID specimenId) {
        return specimenJPARepository.findById(specimenId)
                .orElseThrow(() -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        "specimen with id :" + specimenId + SpecimenIntegrationServiceImpl.NOT_FOUND));
    }

    public void checkCollectionAndSpecimenExist(UUID collectionId, UUID specimenId) {
        checkCollectionExist(collectionId);
        checkSpecimenExist(specimenId);
    }
}

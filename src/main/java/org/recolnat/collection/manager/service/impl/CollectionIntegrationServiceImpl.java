package org.recolnat.collection.manager.service.impl;


import io.recolnat.model.CollectionUpdateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.CollectionCreate;
import org.recolnat.collection.manager.api.domain.SpecimenIndex;
import org.recolnat.collection.manager.api.domain.enums.OperationTypeEnum;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.mapper.CollectionMapper;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.AuthorisationService;
import org.recolnat.collection.manager.service.CollectionIntegrationService;
import org.recolnat.collection.manager.service.ElasticService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionIntegrationServiceImpl implements CollectionIntegrationService {

    public static final String ERR_CODE_INVALID_REQUEST = "ERR_CODE_INVALID_REQUEST";
    private final CollectionJPARepository collectionJPARepository;
    private final InstitutionRepositoryJPA institutionRepository;
    private final CollectionMapper collectionMapper;
    private final Validator validator;
    private final AuthorisationService authorisationService;
    private final AuthenticationService authenticationService;
    private final SpecimenJPARepository specimenJPARepository;
    private final ElasticService elasticService;


    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public UUID updateCollection(UUID collectionId, CollectionUpdateDTO collectionUpdateDTO) {
        var currentUser = authenticationService.findUserAttributes();

        if (RoleEnum.fromValue(currentUser.getRole()).equals(RoleEnum.ADMIN_COLLECTION) && !currentUser.getCollections().contains(collectionId)) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), "You cannot update this collection");
        }

        var collectionJPA = collectionJPARepository.findById(collectionId).orElseThrow(() -> new CollectionManagerBusinessException(
                HttpStatus.NOT_FOUND, ErrorCode.ERR_NFE_CODE, "collection not found with id :" + collectionId));

        if (RoleEnum.fromValue(currentUser.getRole()).equals(RoleEnum.ADMIN_INSTITUTION) && !collectionJPA.getInstitution().getId()
                .equals(currentUser.getInstitution())) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), "You cannot update this collection");
        }

        boolean collectionCodeChanged = !Objects.equals(collectionJPA.getCollectionCode(), collectionUpdateDTO.getCollectionCode());
        boolean collectionNameEnChanged = !Objects.equals(collectionJPA.getCollectionNameEn(), collectionUpdateDTO.getCollectionNameEn());
        boolean collectionNameFrChanged = !Objects.equals(collectionJPA.getCollectionNameFr(), collectionUpdateDTO.getCollectionNameFr());
        boolean shouldUpdateES = collectionCodeChanged || collectionNameEnChanged || collectionNameFrChanged;

        collectionJPA.setTypeCollection(collectionUpdateDTO.getDomain());
        collectionJPA.setCollectionNameEn(collectionUpdateDTO.getCollectionNameEn());
        collectionJPA.setCollectionNameFr(collectionUpdateDTO.getCollectionNameFr());
        collectionJPA.setDescriptionEn(collectionUpdateDTO.getDescriptionEn());
        collectionJPA.setDescriptionFr(collectionUpdateDTO.getDescriptionFr());
        collectionJPA.setCollectionCode(collectionUpdateDTO.getCollectionCode());

        if (shouldUpdateES) {
            SpecimenIndex.SpecimenIndexBuilder builder = SpecimenIndex.builder();

            if (collectionCodeChanged) {
                builder.collectionCode(collectionUpdateDTO.getCollectionCode());
            }

            if (collectionNameEnChanged) {
                builder.collectionNameEn(collectionUpdateDTO.getCollectionNameEn());
            }

            if (collectionNameFrChanged) {
                builder.collectionNameFr(collectionUpdateDTO.getCollectionNameFr());
            }

            List<String> ids = specimenJPARepository.findAllIdsByCollectionId(collectionId);

            try {
                elasticService.bulkUpdate(builder.build(), ids);
            } catch (IOException e) {
                log.error("Erreur lors de la mise à jour de l'index ES");
                throw new RuntimeException(e);
            }
        }

        return collectionJPARepository.save(collectionJPA).getId();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public UUID addCollection(CollectionCreate collection) {
        validate(collection);

        final var institutionJPA = institutionRepository
                .findInstitutionByInstitutionId(collection.getInstitutionId())
                .orElseThrow(() -> new CollectionManagerBusinessException(
                        HttpStatus.NOT_FOUND, ErrorCode.ERR_NFE_CODE,
                        "institutionId not found with id :" + collection.getInstitutionId()));
        authorisationService.authorize(OperationTypeEnum.ADD_COLLECTION, institutionJPA.getId());
        checkCollectionExist(collection);
        final var collectionJPA = collectionMapper.collectionTocollectionJPA(collection);
        collectionJPA.setInstitutionId(institutionJPA.getId());
        collectionJPA.setInstitution(institutionJPA);
        final var save = collectionJPARepository.save(collectionJPA);
        return save.getId();
    }

    @Override
    @Transactional
    public void deleteCollection(UUID collectionId) {
        var currentUser = authenticationService.findUserAttributes();

        var authorizedRoles = List.of(RoleEnum.ADMIN, RoleEnum.ADMIN_INSTITUTION);

        if (!authorizedRoles.contains(RoleEnum.fromValue(currentUser.getRole()))) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), "collections:error.delete.noRights");
        }

        var collectionJPA = collectionJPARepository.findById(collectionId).orElseThrow(() -> new CollectionManagerBusinessException(
                HttpStatus.NOT_FOUND, ErrorCode.ERR_NFE_CODE, "collection not found with id :" + collectionId));

        if (RoleEnum.fromValue(currentUser.getRole()).equals(RoleEnum.ADMIN_INSTITUTION) && !collectionJPA.getInstitution().getId()
                .equals(currentUser.getInstitution())) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), "collections:error.delete.noRightsOnCollection");
        }

        var hasSpecimen = specimenJPARepository.existsForCollection(collectionId);

        if (hasSpecimen) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), "collections:error.delete.withSpecimen");
        }

        collectionJPARepository.delete(collectionJPA);
    }

    private void validate(CollectionCreate collection) {
        Set<ConstraintViolation<CollectionCreate>> validate = validator.validate(collection);
        if (!validate.isEmpty()) {
            throw new CollectionManagerBusinessException(ERR_CODE_INVALID_REQUEST, validate.stream()
                    .map(ConstraintViolation::getMessage).collect(Collectors.joining("; ")));
        }
    }

    /**
     * Vérifie si une collection de même nom existe dans une institution
     *
     * @param collection objet contenant les informations de la collection à vérifier
     */
    private void checkCollectionExist(CollectionCreate collection) {
        var collectionJPA = collectionJPARepository.findByCollectionNameFrAndInstitution_InstitutionId(collection.getCollectionNameFr(), collection.getInstitutionId());
        if (collectionJPA.isPresent()) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT.value(), ErrorCode.ERR_CODE_CM,
                    "The Collection with name collection: " + collection.getCollectionNameFr() + ", already exists");
        }
    }


}

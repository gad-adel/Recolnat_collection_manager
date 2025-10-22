package org.recolnat.collection.manager.service.impl;


import io.recolnat.model.CollectionDescriptionDTO;
import io.recolnat.model.CollectionDetailDTO;
import io.recolnat.model.DomainSpecimenCountDTO;
import io.recolnat.model.UserCollectionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.Collection;
import org.recolnat.collection.manager.api.domain.CollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.CollectionDescriptionProjection;
import org.recolnat.collection.manager.api.domain.CollectionProjection;
import org.recolnat.collection.manager.api.domain.DomainSpecimenCountProjection;
import org.recolnat.collection.manager.api.domain.NominativeCollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.Result;
import org.recolnat.collection.manager.api.domain.enums.LanguageEnum;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.mapper.CollectionMapper;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.InstitutionJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.CollectionRetrieveService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionRetrieveServiceImpl implements CollectionRetrieveService {

    private final CollectionMapper collectionMapper;
    private final CollectionJPARepository collectionJPARepository;
    private final InstitutionRepositoryJPA institutionRepositoryJPA;
    private final AuthenticationService authenticationService;
    private final SpecimenJPARepository specimenJPARepository;

    @Override
    public List<Collection> retreiveCollectionsByInstitution(Integer institutionId, String lng) {
        var allJpas = collectionJPARepository.findCollectionsByInstitutionId(institutionId).stream()
                .map(collectionMapper::collectionJPAtoCollection).toList();

        return allJpas.stream().map(col -> {
            if (LanguageEnum.FR.name().equalsIgnoreCase(lng)) {
                col.setCollectionName(col.getCollectionNameFr());
                return col;
            }
            col.setCollectionName(col.getCollectionNameEn());
            return col;
        }).toList();
    }

    @Override
    public Result<CollectionDashboardProjection> retreiveCollectionsByInstitution(UUID institutionId, int page, int size, String searchTerm, boolean isFr) {
        Sort sort = Sort.by(isFr ? "collectionNameFr" : "collectionNameEn");
        var data = collectionJPARepository.findCollectionsByInstitutionId(institutionId, "%" + searchTerm + "%", PageRequest.of(page, size, sort));

        var count = collectionJPARepository.countCollectionByInstitutionId(institutionId, "%" + searchTerm + "%");

        return Result.<CollectionDashboardProjection>builder().data(data.stream().toList()).numberOfElements(count)
                .totalPages(data.getTotalPages()).build();
    }

    @Override
    public Result<NominativeCollectionDashboardProjection> retreiveNominativeCollectionsByInstitution(UUID institutionId, int page, int size,
                                                                                                      String searchTerm) {
        Sort sort = Sort.by("nominative_collection");
        var data = specimenJPARepository.findNominativeCollectionsByInstitutionId(institutionId, "%" + searchTerm + "%", PageRequest.of(page, size, sort));

        var count = specimenJPARepository.countNominativeCollectionByInstitutionId(institutionId, "%" + searchTerm + "%");

        return Result.<NominativeCollectionDashboardProjection>builder().data(data.stream().toList()).numberOfElements(count)
                .totalPages(data.getTotalPages()).build();
    }

    @Override
    public List<Collection> retreiveAllCollections() {
        var allCollections = collectionJPARepository.findAll().stream()
                .map(collectionMapper::collectionJPAtoCollection).toList();
        return allCollections.stream().map(col -> {
            col.setTypeCollection(col.getCollectionNameFr());
            return col;
        }).toList();
    }

    @Override
    public CollectionDetailDTO findCollectionDetailById(UUID collectionId) {
        CollectionJPA collectionJPA = collectionJPARepository.findById(collectionId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        String.format("Collection with id : %s not found", collectionId)));

        return collectionMapper.collectionJPAtoCollectionDetail(collectionJPA);
    }

    @Override
    public Collection findCollectionById(UUID collectionId) {
        CollectionJPA collectionJPA = collectionJPARepository.findById(collectionId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        String.format("Collection with id : %s not found", collectionId)));

        return collectionMapper.collectionJPAtoCollection(collectionJPA);
    }

    @Override
    public List<Collection> findCollectionsByIds(List<UUID> collectionIds) {
        return collectionJPARepository.findByIdIn(collectionIds).stream()
                .map(collectionMapper::collectionJPAtoCollection).toList();
    }

    @Override
    public List<CollectionProjection> findAllOptions(UUID institutionId) {
        var currentUser = authenticationService.findUserAttributes();
        RoleEnum userRole = RoleEnum.fromValue(currentUser.getRole());
        boolean isAdmin = RoleEnum.ADMIN.equals(userRole);

        if (isAdmin && institutionId != null) {
            return collectionJPARepository.findAllOptionsByInstitutionId(institutionId);
        } else if (RoleEnum.ADMIN_INSTITUTION.equals(userRole)) {
            Optional<InstitutionJPA> inst = institutionRepositoryJPA.findById(currentUser.getInstitution());
            if (inst.isPresent()) {
                return collectionJPARepository.findAllOptionsByInstitutionId(inst.get().getInstitutionId());
            }
        } else if (List.of(RoleEnum.ADMIN_COLLECTION, RoleEnum.DATA_ENTRY).contains(userRole)) {
            return collectionJPARepository.findAllOptions().stream().filter(c -> currentUser.getCollections().contains(c.getId())).toList();
        }
        return Collections.emptyList();
    }

    @Override
    public List<UserCollectionDTO> findUserCollections() {
        var currentUser = authenticationService.findUserAttributes();
        RoleEnum userRole = RoleEnum.fromValue(currentUser.getRole());
        boolean isAdmin = RoleEnum.ADMIN.equals(userRole);

        if (isAdmin) {
            return Collections.emptyList();
        } else if (RoleEnum.ADMIN_INSTITUTION.equals(userRole)) {
            Optional<InstitutionJPA> inst = institutionRepositoryJPA.findById(currentUser.getInstitution());
            if (inst.isPresent()) {
                return collectionMapper.toUserCollectionDtos(collectionJPARepository.findAllOptionsByInstitutionId(inst.get().getInstitutionId()));
            }
        } else if (List.of(RoleEnum.ADMIN_COLLECTION, RoleEnum.DATA_ENTRY).contains(userRole)) {
            return collectionMapper.toUserCollectionDtos(collectionJPARepository.findAllOptions().stream()
                    .filter(c -> currentUser.getCollections().contains(c.getId())).toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<DomainSpecimenCountDTO> getDomainSpecimenCounts(UUID institutionId) {
        List<DomainSpecimenCountProjection> projections =
                collectionJPARepository.findDomainSpecimenCounts(institutionId);
        return collectionMapper.projectionsToDtos(projections);
    }

    @Override
    public List<CollectionDescriptionDTO> getCollectionsDescriptions(UUID institutionId, String lng) {
        List<CollectionDescriptionProjection> projections =
                collectionJPARepository.getCollectionsDescriptions(institutionId);
        boolean isFr = LanguageEnum.FR.name().equalsIgnoreCase(lng);
        return collectionMapper.toLocalizedCollectionDescriptions(projections, isFr);
    }
}

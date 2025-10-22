/**
 *
 */
package org.recolnat.collection.manager.service.impl;

import io.recolnat.model.InstitutionStatisticsDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.CollectionsInstitution;
import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.api.domain.InstitutionDashboard;
import org.recolnat.collection.manager.api.domain.InstitutionDetail;
import org.recolnat.collection.manager.api.domain.InstitutionProjection;
import org.recolnat.collection.manager.api.domain.InstitutionPublicResult;
import org.recolnat.collection.manager.api.domain.InstitutionStatisticProjection;
import org.recolnat.collection.manager.api.domain.MidsGroup;
import org.recolnat.collection.manager.api.domain.Result;
import org.recolnat.collection.manager.api.domain.enums.LanguageEnum;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.mapper.InstitutionMapper;
import org.recolnat.collection.manager.connector.api.MediathequeService;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.InstitutionJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.repository.jpa.TaxonJPARepository;
import org.recolnat.collection.manager.service.AsyncService;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.CollectionRetrieveService;
import org.recolnat.collection.manager.service.InstitutionService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.recolnat.collection.manager.service.JpaQueryUtils.likeIgnoreCase;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionServiceImpl implements InstitutionService {

    public static final String INSTITUTION_NAME = "name";
    private static final String INSTITUTION_CODE = "code";

    private final CollectionRetrieveService collectionApiClient;
    private final InstitutionRepositoryJPA institutionRepository;
    private final InstitutionMapper institutionMapper;
    private final Validator validator;
    private final MediathequeService mediathequeApiClient;
    private final AuthenticationService authenticationService;
    private final AsyncService asyncService;
    private final SpecimenJPARepository specimenJPARepository;
    private final TaxonJPARepository taxonJPARepository;

    @PersistenceContext
    private EntityManager em;


    @Override
    public Institution getInstitutionById(int id, String lng) {
        InstitutionJPA inst = checkInstExist(id);
        log.debug("Retrieve institution with id : {}", id);
        final var collectionsByInstitution = buildCollection(collectionApiClient.retreiveCollectionsByInstitution(id, lng));

        return Institution.builder()
                .id(inst.getId())
                .institutionId(inst.getInstitutionId())
                .code(inst.getCode())
                .name(inst.getName())
                .institutionId(inst.getInstitutionId())
                .mandatoryDescription(inst.getMandatoryDescription())
                .optionalDescription(inst.getOptionalDescription())
                .partnerType(LanguageEnum.FR.name().equalsIgnoreCase(lng) ? inst.getPartnerType().getPartnerFr() : inst.getPartnerType().getPartnerEn())
                .partnerTypeEn(isNull(inst.getPartnerType()) ? null : inst.getPartnerType().getPartnerEn())
                .partnerTypeFr(isNull(inst.getPartnerType()) ? null : inst.getPartnerType().getPartnerFr())
                .logoUrl(inst.getLogoUrl())
                .collections(collectionsByInstitution)
                .url(inst.getUrl())
                .specimensCount(inst.getSpecimensCount())
                .build();
    }


    @Override
    public Institution getInstitutionPublicByUUID(UUID idUUID, String lng) {
        InstitutionJPA inst = institutionRepository
                .findInstitutionByInstitutionId(idUUID).orElseThrow(
                        () ->
                                new CollectionManagerBusinessException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorCode.ERR_NFE_CODE,
                                        "institutionId not found with idUUID :" + idUUID)
                );
        final var collectionsByInstitution = buildCollection(collectionApiClient.retreiveCollectionsByInstitution(inst.getId(), lng));
        return Institution.builder()
                .id(inst.getId())
                .institutionId(inst.getInstitutionId())
                .code(inst.getCode())
                .name(inst.getName())
                .mandatoryDescription(inst.getMandatoryDescription())
                .optionalDescription(inst.getOptionalDescription())
                .partnerType(LanguageEnum.FR.name().equalsIgnoreCase(lng) ? inst.getPartnerType().getPartnerFr() : inst.getPartnerType().getPartnerEn())
                .partnerTypeEn(isNull(inst.getPartnerType()) ? null : inst.getPartnerType().getPartnerEn())
                .partnerTypeFr(isNull(inst.getPartnerType()) ? null : inst.getPartnerType().getPartnerFr())
                .logoUrl(inst.getLogoUrl())
                .collections(collectionsByInstitution)
                .url(inst.getUrl())
                .specimensCount(inst.getSpecimensCount())
                .build();

    }

    @Override
    public InstitutionDetail getInstitutionByUUID(UUID uuid, String lng) {
        InstitutionJPA inst = institutionRepository
                .findInstitutionByInstitutionId(uuid).orElseThrow(
                        () ->
                                new CollectionManagerBusinessException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorCode.ERR_NFE_CODE,
                                        "institutionId not found with UUID :" + uuid)
                );
        checkAccessToInstitution(uuid);
        return InstitutionDetail.builder()
                .id(inst.getInstitutionId())
                .code(inst.getCode())
                .name(inst.getName())
                .mandatoryDescription(inst.getMandatoryDescription())
                .optionalDescription(inst.getOptionalDescription())
                .partnerType(inst.getPartnerType().toString())
                .logoUrl(inst.getLogoUrl())
                .url(inst.getUrl())
                .specimensCount(inst.getSpecimensCount())
                .build();
    }

    @Override
    public boolean checkAccessToInstitution(UUID uuid) {
        InstitutionJPA inst = institutionRepository
                .findInstitutionByInstitutionId(uuid).orElseThrow(
                        () ->
                                new CollectionManagerBusinessException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorCode.ERR_NFE_CODE,
                                        "institutionId not found with UUID :" + uuid)
                );
        var currentUser = authenticationService.findUserAttributes();
        var isAdmin = RoleEnum.ADMIN.equals(RoleEnum.fromValue(currentUser.getRole()));
        var isAdmInst = RoleEnum.ADMIN_INSTITUTION.equals(RoleEnum.fromValue(currentUser.getRole()));
        var isAdmColl = RoleEnum.ADMIN_COLLECTION.equals(RoleEnum.fromValue(currentUser.getRole()));
        return !isAdmin && !((isAdmInst || isAdmColl) && Objects.equals(currentUser.getInstitution(), inst.getId()));
    }

    protected void checkAccessToInstitution(InstitutionJPA inst) {
        var currentUser = authenticationService.findUserAttributes();
        var isAdmin = RoleEnum.ADMIN.equals(RoleEnum.fromValue(currentUser.getRole()));
        var isAdmInst = RoleEnum.ADMIN_INSTITUTION.equals(RoleEnum.fromValue(currentUser.getRole()));
        if (!isAdmin && !(isAdmInst && Objects.equals(currentUser.getInstitution(), inst.getId()))) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), "Access denied");
        }
    }

    @Override
    public Result<InstitutionDashboard> findAll(int page, int size, String searchTerm, String partnerType) {
        var currentUser = authenticationService.findUserAttributes();
        var isAdmin = RoleEnum.ADMIN.equals(RoleEnum.fromValue(currentUser.getRole()));

        if (!isAdmin) {
            return Result.<InstitutionDashboard>builder().data(Collections.emptyList()).numberOfElements(0).totalPages(0).build();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<InstitutionJPA> root = query.from(InstitutionJPA.class);
        Join<InstitutionJPA, CollectionJPA> collectionJoin = root.join("collections", JoinType.LEFT);
        collectionJoin.on(cb.equal(root.get("id"), collectionJoin.get("institutionId")));
        Join<CollectionJPA, SpecimenJPA> specimenJoin = collectionJoin.join("specimens", JoinType.LEFT);

        query.multiselect(
                root.get("institutionId"),
                root.get("code"),
                root.get("name"),
                root.get("partnerType"),
                cb.count(specimenJoin.get("id"))
        );

        List<Order> sorting = List.of(cb.asc(root.get(INSTITUTION_NAME)), cb.asc(root.get(INSTITUTION_CODE)));

        ArrayList<Predicate> filters = new ArrayList<>(getFilters(cb, root, searchTerm, partnerType));

        query.where(cb.and(filters.toArray(new Predicate[0]))).orderBy(sorting);

        query.groupBy(
                root.get("institutionId"),
                root.get("code"),
                root.get("name"),
                root.get("partnerType")
        );

        TypedQuery<Tuple> tuples = em.createQuery(query);
        tuples.setFirstResult(page * size).setMaxResults(size);
        List<InstitutionDashboard> institutions = getInstitutionFromTuples(tuples);

        Long total = countInstitutions(searchTerm, partnerType);

        var pageable = new PageImpl<>(institutions, PageRequest.of(page, size), total);
        return Result.<InstitutionDashboard>builder().data(institutions).numberOfElements(total).totalPages(pageable.getTotalPages()).build();
    }

    private Long countInstitutions(String searchTerm, String partnerType) {
        CriteriaBuilder cbCount = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQueryCount = cbCount.createQuery(Long.class);
        Root<InstitutionJPA> rootCount = criteriaQueryCount.from(InstitutionJPA.class);

        criteriaQueryCount.select(cbCount.count(rootCount.get("id")));

        ArrayList<Predicate> filters = new ArrayList<>(getFilters(cbCount, rootCount, searchTerm, partnerType));

        criteriaQueryCount.where(cbCount.and(filters.toArray(new Predicate[0])));

        TypedQuery<Long> totalQuery = em.createQuery(criteriaQueryCount);
        return totalQuery.getSingleResult();
    }

    private List<InstitutionDashboard> getInstitutionFromTuples(TypedQuery<Tuple> tuples) {
        List<Tuple> objectsArray = tuples.getResultList();

        List<InstitutionDashboard> institutions = new ArrayList<>();

        objectsArray.forEach(objetArray -> institutions.add(InstitutionDashboard.builder()
                .id((UUID) objetArray.get(0))
                .code((String) objetArray.get(1))
                .name((String) objetArray.get(2))
                .partnerType(objetArray.get(3).toString())
                .specimenCount((Long) objetArray.get(4))
                .build()));

        return institutions;
    }

    private List<Predicate> getFilters(CriteriaBuilder cb, Root<InstitutionJPA> root, String searchTerm, String partnerType) {
        ArrayList<Predicate> filters = new ArrayList<>();

        if (StringUtils.isNotBlank(searchTerm)) {
            filters.add(cb.or(
                            likeIgnoreCase(cb, root.get("name"), searchTerm),
                            likeIgnoreCase(cb, root.get("code"), searchTerm)
                    )
            );
        }

        if (Objects.nonNull(partnerType)) {
            filters.add(cb.equal(root.get("partnerType"), PartnerType.valueOf(partnerType)));
        }

        return filters;
    }

    @Override
    public List<InstitutionProjection> findAllOptions() {
        return institutionRepository.findAllOptions();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public UUID addInstitution(Institution institution) {
        validateInstitution(institution);
        log.debug("Add institution : {}", institution.getName());
        checkExistInstByCode(institution.getCode());
        var newInstitution = buildNewInstitution(institution);
        var institutionJPA = institutionMapper.toInstitutionJPA(newInstitution);
        var save = institutionRepository.save(institutionJPA);
        asyncService.updateCacheStatistique();
        return save.getInstitutionId();
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public UUID updateInstitution(UUID id, Institution institution) {
        validateInstitution(institution);
        var oldInstitutionJPA = checkInstExist(id);
        checkAccessToInstitution(oldInstitutionJPA);
        final var institutionToSave = buildUpdateInstitution(institution, oldInstitutionJPA);
        log.info("Update institution with id : {}", id);
        var institutionJPA = institutionMapper.toInstitutionJPA(institutionToSave);
        return institutionRepository.save(institutionJPA).getInstitutionId();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UUID addLogoIntitution(UUID id, MultipartFile img) {
        var institutionJPA = checkInstExist(id);
        checkAccessToInstitution(institutionJPA);
        log.info("Add logo institution with id : {}", id);
        try {
            var saveLogo = mediathequeApiClient.savePicture(img);
            if (Objects.isNull(saveLogo) ||
                Objects.isNull(saveLogo.getBody())) {
                throw new CollectionManagerBusinessException(ErrorCode.ERR_NFE_CODE, "Media don't found");
            }
            institutionJPA.setLogoUrl(saveLogo.getBody().getMedia().getUrl());
            institutionRepository.save(institutionJPA);
        } catch (IOException e) {
            throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "SERVER_ERROR_CODE", e.getMessage());
        }
        return institutionJPA.getInstitutionId();
    }

    @Override
    public InstitutionPublicResult findAllByPartnerType(int page, int size, PartnerType partnerType) {
        var allInstitution = institutionRepository.findAllByPartnerType(PageRequest.of(page, size, Sort.by(INSTITUTION_NAME).ascending()
                .and(Sort.by(INSTITUTION_CODE)).ascending()), partnerType);
        var allInst = allInstitution.stream()
                //to refactor because # conditions same actions...
                .map(jpa ->
                        Institution.builder()
                                .id(jpa.getId())
                                .institutionId(jpa.getInstitutionId())
                                .code(jpa.getCode())
                                .name(jpa.getName())
                                .createdAt(jpa.getCreatedAt())
                                .createdBy(jpa.getCreatedBy())
                                .logoUrl(jpa.getLogoUrl())
                                .optionalDescription(jpa.getOptionalDescription())
                                .mandatoryDescription(jpa.getMandatoryDescription())
                                .modifiedBy(jpa.getModifiedBy())
                                .modifiedAt(jpa.getModifiedAt())
                                .partnerType(partnerType.name())
                                .assignable(false).build()
                )
                .toList();
        return InstitutionPublicResult.builder().institutions(allInst).totalPages(allInstitution.getTotalPages()).build();

    }


    @Override
    public List<Institution> getInstitutionsByCodes(List<String> codes) {
        var allInstitutions = institutionRepository.findInstitutionsByCodesIgnoreCase(codes);
        return allInstitutions.stream().map(jpa ->
                        Institution.builder()
                                .id(jpa.getId())
                                .institutionId(jpa.getInstitutionId())
                                .code(jpa.getCode())
                                .name(jpa.getName())
                                .logoUrl(jpa.getLogoUrl()).build()
                )
                .toList();

    }

    private void validateInstitution(Institution institution) {
        var violations = validator.validate(institution);
        if (!CollectionUtils.isEmpty(violations)) {
            throw new CollectionManagerBusinessException(
                    HttpStatus.BAD_REQUEST,
                    "ERR_CODE_INVALID_REQUEST",
                    validator.validate(institution).stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(";")));
        }

    }

    private InstitutionJPA checkInstExist(int id) {
        return institutionRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new CollectionManagerBusinessException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorCode.ERR_NFE_CODE,
                                        "institutionId not found with id :" + id));
    }

    private InstitutionJPA checkInstExist(UUID id) {
        return institutionRepository
                .findInstitutionByInstitutionId(id)
                .orElseThrow(
                        () ->
                                new CollectionManagerBusinessException(
                                        HttpStatus.NOT_FOUND,
                                        ErrorCode.ERR_NFE_CODE,
                                        "institutionId not found with id :" + id));
    }

    private void checkExistInstByCode(String code) {
        var findInstitutionByCode = institutionRepository.findInstitutionByCodeIgnoreCase(code);
        if (findInstitutionByCode.isPresent()) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT,
                    "ERR_EXIST_CODE",
                    "the institution with code: " + code + ", already exists");
        }
    }

    private List<CollectionsInstitution> buildCollection(List<org.recolnat.collection.manager.api.domain.Collection> outputCols) {
        return Optional.ofNullable(outputCols).map(Collection::stream).orElseGet(Stream::empty)
                .map(outputCol -> CollectionsInstitution.builder()
                        .collectionCode(outputCol.getCollectionName())
                        .typeCollection(outputCol.getTypeCollection())
                        .build()).toList();
    }

    private Institution buildNewInstitution(Institution institution) {
        var user = authenticationService.getConnected().getUserName();
        return Institution.builder().code(institution.getCode()).name(institution.getName())
                .mandatoryDescription(institution.getMandatoryDescription())
                .optionalDescription(institution.getOptionalDescription())
                .partnerType(institution.getPartnerType())
                .logoUrl(institution.getLogoUrl())
                .createdAt(LocalDateTime.now(ZoneId.of("UTC"))).createdBy(user).institutionId(UUID.randomUUID()).build();
    }

    private Institution buildUpdateInstitution(Institution institution, InstitutionJPA institutionJPA) {
        var user = authenticationService.getConnected().getUserName();
        return Institution.builder().id(institutionJPA.getId()).institutionId(institutionJPA.getInstitutionId())
                .code(institution.getCode()).name(institution.getName())
                .mandatoryDescription(institution.getMandatoryDescription())
                .optionalDescription(institution.getOptionalDescription())
                .partnerType(institution.getPartnerType())
                .logoUrl(institution.getLogoUrl())
                .createdAt(institutionJPA.getCreatedAt())
                .createdBy(institutionJPA.getCreatedBy())
                .modifiedBy(user)
                .modifiedAt(LocalDateTime.now(ZoneId.of("UTC")))
                .url(institution.getUrl())
                .specimensCount(institution.getSpecimensCount())
                .build();
    }

    @Override
    public List<Institution> getInstitutionsByIds(List<UUID> ids) {
        var listInstitutionJpa = institutionRepository.findInstitutionByInstitutionIds(ids);

        return Optional.ofNullable(listInstitutionJpa).map(Collection::stream).orElseGet(Stream::empty)
                .map(institutionJPA -> Institution.builder()
                        .id(institutionJPA.getId())
                        .institutionId(institutionJPA.getInstitutionId())
                        .code(institutionJPA.getCode())
                        .name(institutionJPA.getName())
                        .mandatoryDescription(institutionJPA.getMandatoryDescription())
                        .optionalDescription(institutionJPA.getOptionalDescription())
                        .partnerType(institutionJPA.getPartnerType().getPartnerFr() != null ? institutionJPA.getPartnerType()
                                .getPartnerFr() : institutionJPA.getPartnerType().getPartnerEn())
                        .partnerTypeEn(institutionJPA.getPartnerType().getPartnerFr())
                        .partnerTypeFr(institutionJPA.getPartnerType().getPartnerEn())
                        .logoUrl(institutionJPA.getLogoUrl())
                        .createdAt(institutionJPA.getCreatedAt())
                        .createdBy(institutionJPA.getCreatedBy())
                        .modifiedBy(institutionJPA.getModifiedBy())
                        .modifiedAt(institutionJPA.getModifiedAt())
                        .build()
                ).toList();

    }

    @Override
    public InstitutionStatisticsDTO getInstitutionStatistics(UUID institutionId) {
        InstitutionStatisticProjection projection = institutionRepository.getInstitutionStatistic(institutionId);
        return institutionMapper.toDto(projection);
    }

    public void refreshStatisticView() {
        institutionRepository.refreshMaterializedView();
    }

    @Override
    public List<Long> getInstitutionMids(UUID institutionId) {
        List<MidsGroup> groups = institutionRepository.getInstitutionMids(institutionId);

        Map<Integer, Long> map = groups.stream().collect(Collectors.toMap(MidsGroup::getMids, MidsGroup::getCount));

        return IntStream.range(0, 4).mapToObj(i -> map.getOrDefault(i, 0L)).toList();
    }
}

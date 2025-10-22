package org.recolnat.collection.manager.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import io.recolnat.model.OperationTypeDTO;
import io.recolnat.model.PublicIdentificationDTO;
import io.recolnat.model.PublicMediaDTO;
import io.recolnat.model.PublicSpecimenDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.api.domain.GeologicalContext;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.api.domain.Literature;
import org.recolnat.collection.manager.api.domain.Management;
import org.recolnat.collection.manager.api.domain.Media;
import org.recolnat.collection.manager.api.domain.NormalCheck;
import org.recolnat.collection.manager.api.domain.Other;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.SpecimenMerge;
import org.recolnat.collection.manager.api.domain.SpecimenPage;
import org.recolnat.collection.manager.api.domain.Taxon;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.api.domain.enums.SpecimenStatusEnum;
import org.recolnat.collection.manager.common.check.service.ControlAttribut;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.mapper.IdentificationMapper;
import org.recolnat.collection.manager.common.mapper.SpecimenMapper;
import org.recolnat.collection.manager.common.mapper.TaxonMapper;
import org.recolnat.collection.manager.connector.api.MediathequeService;
import org.recolnat.collection.manager.repository.entity.AbstractEntity;
import org.recolnat.collection.manager.repository.entity.CollectionEventJPA;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.GeologicalContextJPA;
import org.recolnat.collection.manager.repository.entity.IdentificationJPA;
import org.recolnat.collection.manager.repository.entity.InstitutionJPA;
import org.recolnat.collection.manager.repository.entity.LiteratureJPA;
import org.recolnat.collection.manager.repository.entity.LocationJPA;
import org.recolnat.collection.manager.repository.entity.ManagementJPA;
import org.recolnat.collection.manager.repository.entity.MediaJPA;
import org.recolnat.collection.manager.repository.entity.OtherJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.entity.TaxonJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionEventJPARepository;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.repository.jpa.GeologicalContextJPARepository;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.recolnat.collection.manager.repository.jpa.ManagementJPARepository;
import org.recolnat.collection.manager.repository.jpa.OtherJPARepository;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.AsyncService;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.CollectionIdentifier;
import org.recolnat.collection.manager.service.ElasticService;
import org.recolnat.collection.manager.service.MediaComponent;
import org.recolnat.collection.manager.service.MidsService;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.recolnat.collection.manager.common.check.service.ControlAttribut.COLLECTION_WITH_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecimenIntegrationServiceImpl implements SpecimenIntegrationService {

    public static final String ERR_CODE_INVALID_REQUEST = "ERR_CODE_INVALID_REQUEST";
    public static final String NOT_FOUND = " not found";
    public static final String JOIN_COLLECTION = "collection";
    public static final String ROOT_STATE = "state";
    public static final String CATALOG_NUMBER = "catalogNumber";
    public static final String COLLECTION_CODE = "collectionCode";
    public static final String MODIFIED_AT = "modifiedAt";
    private static final String QUERY_NATIVE_SCIENTIF_NAME = "select s1_0.id,s1_0.catalog_number,s1_0.state,s1_0.fk_id_collection,c1_0.collection_name_fr,"
            .concat("i1_0.id,i1_0.current_determination,t1_0.id, t1_0.level_type,t1_0.scientific_name ")
            .concat("from ( select t2_0.id, t2_0.level_type,t2_0.scientific_name,t2_0.fk_id_identification from taxon t2_0 ")
            .concat("where lower(t2_0.scientific_name) like :searchWord ) as t1_0 ")
            .concat("join identification i1_0  on i1_0.id=t1_0.fk_id_identification and i1_0.current_determination ")
            .concat("join specimen s1_0  on s1_0.id=i1_0.fk_id_specimen ")
            .concat("join collection c1_0 on c1_0.id=s1_0.fk_id_collection  and c1_0.institution_id=:institutionId ")
            .concat("offset :pages rows fetch first :size rows only");
    private static final String QUERY_NATIVE_SCIENTIF_NAME_COUNT = "select count(s1_0.id) from (select t2_0.id,t2_0.fk_id_identification from taxon t2_0 "
            .concat("where lower(t2_0.scientific_name) like :searchWord ) as t1_0 ")
            .concat("join identification i1_0  on i1_0.id=t1_0.fk_id_identification and i1_0.current_determination ")
            .concat("join specimen s1_0  on s1_0.id=i1_0.fk_id_specimen ")
            .concat("join collection c1_0 on c1_0.id=s1_0.fk_id_collection  and c1_0.institution_id=:institutionId");
    private static final String LIKE_EXP = "%";
    private final ControlAttribut checkAttribut;
    private final Validator validator;

    private final AuthenticationService authenticationService;
    private final ElasticService elasticService;
    private final AsyncService asyncService;
    private final MediathequeService mediathequeService;
    private final MediaComponent mediaComponent;
    private final MidsService midsService;

    private final CollectionJPARepository collectionJPARepository;
    private final SpecimenJPARepository specimenJPARepository;
    private final CollectionEventJPARepository collectionEventJPARepository;
    private final GeologicalContextJPARepository geologicalContextJPARepository;
    private final OtherJPARepository otherJPARepository;
    private final ManagementJPARepository managementJPARepository;
    private final InstitutionRepositoryJPA institutionRepository;

    private final SpecimenMapper specimenMapper;
    private final ObjectMapper objectMapper;
    private final IdentificationMapper identificationMapper;
    private final TaxonMapper taxonMapper;
    /**
     * cree un set d'Identification DTO
     */
    Function<Tuple, Set<Identification>> getIdentificationList = objetArray -> {
        Set<Identification> identificationsSet = new HashSet<>();
        var getScientificName = objetArray.get(9) != null ? objetArray.get(9).toString() : StringUtils.EMPTY;
        var getScientificNameAuthorship = objetArray.get(12) != null ? objetArray.get(12).toString() : StringUtils.EMPTY;
        var getLevelType = objetArray.get(8) != null ? LevelTypeEnum.valueOf(objetArray.get(8).toString()) : null;
        identificationsSet.add(Identification.builder().id((UUID) objetArray.get(5))
                .currentDetermination((Boolean) objetArray.get(6))
                .taxon(
                        objetArray.get(7) != null ?
                                List.of(
                                        Taxon.builder().id((UUID) objetArray.get(7)).levelType(getLevelType).
                                                scientificName(getScientificName).scientificNameAuthorship(getScientificNameAuthorship).build()
                                )
                                : null
                ).build());
        return identificationsSet;
    };
    Function<Object[], Set<Identification>> getIdentificationSimpleToList = objetArray -> {
        Set<Identification> identificationsSet = new HashSet<>();
        var getScientificName = objetArray[9] != null ? objetArray[9].toString() : StringUtils.EMPTY;
        var getLevelType = objetArray[8] != null ? LevelTypeEnum.valueOf(objetArray[8].toString()) : null;
        identificationsSet.add(Identification.builder().id((UUID) objetArray[5])
                .currentDetermination((Boolean) objetArray[6])
                .taxon(
                        objetArray[7] != null ?
                                List.of(
                                        Taxon.builder().id((UUID) objetArray[7]).levelType(getLevelType).
                                                scientificName(getScientificName).build()
                                )
                                : null
                ).build());
        return identificationsSet;
    };
    @PersistenceContext
    private EntityManager em;

    public static LocationJPA buildLocation(CollectionEvent event) {
        return Optional.ofNullable(event).map(CollectionEvent::getLocation)
                .map(location -> LocationJPA.builder()
                        .continent(location.getContinent())
                        .locationRemarks(location.getLocationRemarks())
                        .country(location.getCountry())
                        .county(location.getCounty())
                        .countryCode(location.getCountryCode())
                        .island(location.getIsland())
                        .islandGroup(location.getIslandGroup())
                        .municipality(location.getMunicipality())
                        .region(location.getRegion())
                        .stateProvince(location.getStateProvince())
                        .waterBody(location.getWaterBody())
                        .locality(location.getLocality())
                        .build()).orElse(null);
    }

    private static boolean isLowerOldMedia(Set<MediaJPA> odlMedias, List<Media> newMedias) {
        return newMedias.size() < odlMedias.size();
    }

    private static boolean urlNewMediaIsNotEmpty(List<Media> newMedias) {
        return newMedias.isEmpty() ||
               newMedias.stream().anyMatch(media -> StringUtils.isBlank(media.getMediaUrl()));
    }

    private static void addParametersToQuery(Query query, String searchTerm, OperationTypeDTO state, Integer institutionId, UUID collectionId,
                                             String collectionCode,
                                             String family, String genus, String specificEpithet,
                                             String startDate, String endDate, String collector, String continent, String country, String nominativeCollection,
                                             String storageName, List<UUID> collectionUuids) {
        if (StringUtils.isNotBlank(searchTerm)) {
            query.setParameter("searchTerm", "%" + searchTerm + "%");
        }

        if (StringUtils.isNotBlank(family)) {
            query.setParameter("family", "%" + family + "%");
        }

        if (StringUtils.isNotBlank(genus)) {
            query.setParameter("genus", "%" + genus + "%");
        }

        if (StringUtils.isNotBlank(specificEpithet)) {
            query.setParameter("specificEpithet", "%" + specificEpithet + "%");
        }

        if (nonNull(state)) {
            query.setParameter(ROOT_STATE, state.getValue());
        }

        if (StringUtils.isNotBlank(collectionCode)) {
            query.setParameter(COLLECTION_CODE, collectionCode);
        }

        if (StringUtils.isNotBlank(country)) {
            query.setParameter("country", "%" + country + "%");
        }

        if (StringUtils.isNotBlank(continent)) {
            query.setParameter("continent", "%" + continent + "%");
        }

        // TODO retiré pour le moment
        // if (StringUtils.isNotBlank(startDate)) {
        //     query.setParameter("startDate", startDate);
        // }
        //
        // if (StringUtils.isNotBlank(endDate)) {
        //     query.setParameter("endDate", endDate);
        // }

        if (StringUtils.isNotBlank(collector)) {
            query.setParameter("collector", "%" + collector + "%");
        }

        if (nonNull(collectionId)) {
            query.setParameter("collectionId", collectionId);
        }

        if (nonNull(institutionId) && isNull(collectionId)) {
            query.setParameter("institutionId", institutionId);
        }

        if (!collectionUuids.isEmpty()) {
            query.setParameter("collectionUuids", collectionUuids);
        }

        if (StringUtils.isNotBlank(nominativeCollection)) {
            query.setParameter("nominativeCollection", "%" + nominativeCollection + "%");
        }

        if (StringUtils.isNotBlank(storageName)) {
            query.setParameter("storageName", storageName);
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = CollectionManagerBusinessException.class)
    public CollectionIdentifier add(Specimen specimenToSave) {
        var collectionId = specimenToSave.getCollectionId();
        checkAttribut.checkSpecimenCreateOrUpdateAsPublished(collectionId);
        var collectionJPA = checkCollectionExist(collectionId);
        validateSpecimen(specimenToSave);
        CollectionIdentifier collectionIdentifier = saveSpecimen(specimenToSave, collectionJPA, SpecimenStatusEnum.VALID);
        specimenToSave.setId(collectionIdentifier.getSpecimenId());
        elasticService.addOrUpdateRefSpecimenElastic(specimenToSave, collectionJPA);
        asyncService.updateCacheStatistique();
        return collectionIdentifier;
    }

    @Override
    @Transactional(readOnly = true)
    public Specimen getSpecimenById(UUID specimenId) {
        final var specimenJPA = checkSpecimenExist(specimenId);
        return specimenMapper.mapJpaToSpecimen(specimenJPA);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecimenPage getAllSpecimen(Integer pages, Integer size, String searchTerm, OperationTypeDTO state, Boolean currentDetermination, Boolean levelType,
                                       String columnSort, String typeSort, UUID institutionUuid, UUID collectionId, String collectionCode, String family,
                                       String genus, String specificEpithet, String startDate, String endDate, String collector, String continent,
                                       String country, String nominativeCollection, String storageName) {
        var currentUser = authenticationService.findUserAttributes();
        Integer institutionId = getInstitutionIdToFilter(institutionUuid, currentUser);

        List<UUID> userCollectionUuids = getUserCollectionUuids(currentUser);

        Page<Specimen> findAll = this.findByListCollectionQueryBuilder(pages, size, searchTerm, state, currentDetermination, levelType, columnSort, typeSort,
                institutionId, collectionId, collectionCode, family, genus, specificEpithet, startDate, endDate, collector, continent, country, nominativeCollection, storageName, userCollectionUuids);

        if (findAll.getTotalElements() == 0) {
            return SpecimenPage.builder().specimen(Collections.emptyList()).numberOfElements(0).totalPages(0).build();
        }
        var totalPages = findAll.getTotalPages();
        if (pages >= totalPages) {
            throw new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.name(), "Page: " + pages + " cannot be requested ");
        }

        var specimens = findAll.getContent();

        return SpecimenPage.builder().specimen(specimens).numberOfElements((int) findAll.getTotalElements()).totalPages(totalPages).build();
    }

    public Page<Specimen> findByListCollectionQueryBuilder(Integer page, Integer size, String searchTerm, OperationTypeDTO state, Boolean currentDetermination,
                                                           Boolean levelType, String columnSort, String typeSort, Integer institutionId, UUID collectionId,
                                                           String collectionCode, String family, String genus, String specificEpithet, String startDate,
                                                           String endDate, String collector, String continent, String country, String nominativeCollection,
                                                           String storageName, List<UUID> collectionUuids) {
        var queryStopWatch = new StopWatch();
        queryStopWatch.start();

        String queryString = """
                    select
                        s1_0.id,
                        s1_0.state,
                        s1_0.fk_id_collection,
                        c1_0.collection_name_fr,
                        s1_0.catalog_number,
                        i1_0.id,
                        i1_0.current_determination,
                        t1_0.id,
                        t1_0.level_type,
                        t1_0.scientific_name,
                        s1_0.modified_at,
                        s1_0.collection_code,
                        t1_0.scientific_name_authorship
                """;
        queryString += "\n" + getFromClause("id, state, fk_id_collection, catalog_number, modified_at, collection_code, fk_colevent_id, fk_management_id",
                state, searchTerm, collectionCode, nominativeCollection);
        queryString += "\n" + getJoinClause(searchTerm, institutionId, collectionId, family, genus, specificEpithet, startDate, endDate, collector,
                continent, country, storageName, collectionUuids);
        queryString += "\n" + getOrderByClause(columnSort, typeSort);

        var query = em.createNativeQuery(queryString, Tuple.class);

        addParametersToQuery(query, searchTerm, state, institutionId, collectionId, collectionCode, family, genus, specificEpithet, startDate, endDate, collector,
                continent, country, nominativeCollection, storageName, collectionUuids);

        query.setFirstResult(page * size).setMaxResults(size);

        List<Tuple> objectsArray = query.getResultList();
//        queryStopWatch.stop();
//        System.out.println("Query Time Elapsed: " + queryStopWatch.getTotalTimeMillis());

        List<Specimen> specimens = new ArrayList<>();
//        var dtoStopWatch = new StopWatch();
//        dtoStopWatch.start();
        buildSpecimenObjects(objectsArray, specimens);
//        dtoStopWatch.stop();
//        System.out.println("DTO Time Elapsed: " + dtoStopWatch.getTotalTimeMillis());

//        var countStopWatch = new StopWatch();
//        countStopWatch.start();
        Long total = countSpecimen(searchTerm, state, institutionId, collectionId, collectionCode, family, genus, specificEpithet, startDate, endDate, collector,
                continent, country, nominativeCollection, storageName, collectionUuids);
//        countStopWatch.stop();
//        System.out.println("Count Time Elapsed: " + countStopWatch.getTotalTimeMillis());

        return new PageImpl<>(specimens, PageRequest.of(page, size), total);
    }

    private String getFromClause(String selectFields, OperationTypeDTO state, String searchTerm, String collectionCode, String nominativeCollection) {
        var andString = "";
        if (nonNull(state)) {
            andString += " and state = :state";
        }
        if (StringUtils.isNotBlank(searchTerm)) {
            andString += " and upper(catalog_number) like upper(:searchTerm)";
        }

        if (StringUtils.isNotBlank(collectionCode)) {
            andString += " and upper(collection_code) like upper(:collectionCode)";
        }

        if (StringUtils.isNotBlank(nominativeCollection)) {
            andString += " and upper(nominative_collection) like upper(:nominativeCollection)";
        }

        return " from (select " + selectFields + " from specimen where 1 = 1 " + andString + ") as s1_0\n";
    }

    private String getJoinClause(String searchTerm, Integer institutionId, UUID collectionId,
                                 String family, String genus, String specificEpithet,
                                 String startDate, String endDate, String collector, String continent, String country,
                                 String storageName, List<UUID> collectionUuids) {
        var joinClause = "";
        joinClause += "\n" + buildCollectionJoin(institutionId, collectionId, collectionUuids);
        joinClause += "\n" + buildIdentificationJoin("i.id, i.fk_id_specimen, current_determination");
        joinClause += "\n" + buildTaxonJoin("id, level_type, scientific_name, scientific_name_authorship, fk_id_identification", searchTerm, family, genus, specificEpithet);

        if (StringUtils.isNotBlank(country) || StringUtils.isNotBlank(continent) || StringUtils.isNotBlank(startDate) || StringUtils.isNotBlank(endDate) ||
            StringUtils.isNotBlank(collector)) {
            joinClause += "\n" + buildCollectionEventJoin(country, continent, collector, startDate, endDate);
        }

        if (StringUtils.isNotBlank(storageName)) {
            joinClause += "\n" + buildManagementJoin();
        }

        return joinClause;
    }

    private String buildCollectionJoin(Integer institutionId, UUID collectionId, List<UUID> collectionUuids) {
        var andString = "";

        if (nonNull(institutionId) && isNull(collectionId)) {
            andString += " and c.institution_id = :institutionId";
        }

        if (nonNull(collectionId)) {
            andString += " and c.id = :collectionId";
        }

        if (isNull(collectionId) && !collectionUuids.isEmpty()) {
            andString += " and c.id in (:collectionUuids)";
        }

        return "join (select id, collection_name_fr from collection c where 1 = 1 " + andString + ") as c1_0 on c1_0.id = s1_0.fk_id_collection";
    }

    private String buildIdentificationJoin(String selectFields) {
        return String.format("""
                left join (
                    select %s
                    from identification i
                    where i.current_determination
                    ) as i1_0 on i1_0.fk_id_specimen = s1_0.id
                """, selectFields);
    }

    private String buildTaxonJoin(String selectFields, String searchTerm, String family, String genus, String specificEpithet) {
        var andString = "";
        // TODO désactivé pour le moment
        // if (StringUtils.isNotBlank(searchTerm)) {
        //     andString += " and upper(scientific_name) like upper(:searchTerm)";
        // }

        if (StringUtils.isNotBlank(family)) {
            andString += " and upper(family) like upper(:family)";
        }

        if (StringUtils.isNotBlank(genus)) {
            andString += " and upper(genus) like upper(:genus)";
        }

        if (StringUtils.isNotBlank(specificEpithet)) {
            andString += " and upper(specific_epithet) like upper(:specificEpithet)";
        }

        var hasParameter = StringUtils.isNotBlank(family) || StringUtils.isNotBlank(genus) || StringUtils.isNotBlank(specificEpithet);

        var joinType = hasParameter ? "join" : "left join";

        return String.format("""
                %s (
                    select %s
                    from taxon t
                    where t.level_type='MASTER'
                    %s
                ) as t1_0 on i1_0.id=t1_0.fk_id_identification
                """, joinType, selectFields, andString);
    }

    private void buildSpecimenObjects(List<Tuple> objectsArray, List<Specimen> specimens) {
        objectsArray.forEach(objetArray -> {
            if (specimens.stream().noneMatch(spec -> spec.getId().equals(objetArray.get(0)))) {
                specimens.add(
                        Specimen.builder()
                                .id((UUID) objetArray.get(0))
                                .state(SpecimenStatusEnum.valueOf(objetArray.get(1).toString()).name())
                                .collectionId(UUID.fromString(objetArray.get(2).toString()))
                                .collectionName(nonNull(objetArray.get(3)) ? objetArray.get(3).toString() : null)
                                .catalogNumber(objetArray.get(4) != null ? objetArray.get(4).toString() : null)
                                .identifications(objetArray.get(5) != null ? getIdentificationList.apply(objetArray) : new HashSet<>())
                                .modifiedAt(objetArray.get(10, Timestamp.class).toLocalDateTime())
                                .collectionCode(nonNull(objetArray.get(11)) ? objetArray.get(11).toString() : null)
                                .build());
            } else {
                if (objetArray.get(5) != null) {
                    Optional<Specimen> specimen = specimens.stream().filter(spec -> spec.getId().equals(objetArray.get(0))).findFirst();
                    specimen.ifPresent(value -> value.getIdentifications().addAll(getIdentificationList.apply(objetArray)));
                }
            }
        });
    }

    private String getOrderByClause(String columnSort, String typeSort) {
        String sorting = " order by s1_0.modified_at desc";
        if (nonNull(columnSort)) {
            final boolean isAscSort = "asc".equalsIgnoreCase(typeSort);
            var fields = switch (columnSort) {
                case JOIN_COLLECTION -> Collections.singletonList("c1_0.collection_name_fr");
                case "name" -> Collections.singletonList("t1_0.scientific_name");
                case CATALOG_NUMBER -> List.of("s1_0.collection_code", "s1_0.catalog_number");
                case MODIFIED_AT -> Collections.singletonList("s1_0.modified_at");
                case ROOT_STATE -> Collections.singletonList("s1_0.state");
                default -> throw new IllegalStateException("Unexpected value: " + columnSort);
            };

            sorting = " order by " + fields.stream().map(f -> f + (isAscSort ? " asc" : " desc")).collect(Collectors.joining(", "));
        }
        return sorting;
    }

    private Long countSpecimen(String searchTerm, OperationTypeDTO state, Integer institutionId, UUID collectionId, String collectionCode, String family,
                               String genus, String specificEpithet, String startDate, String endDate, String collector, String continent, String country,
                               String nominativeCollection, String storageName, List<UUID> collectionUuids) {
        String queryString = "select count(s1_0.id) " + getFromClause("id, fk_id_collection, fk_colevent_id, fk_management_id", state, searchTerm, collectionCode, nominativeCollection);

        if (nonNull(institutionId) || nonNull(collectionId)) {
            queryString += "\n" + buildCollectionJoin(institutionId, collectionId, collectionUuids);
        }

        // TODO count a revoir car le select fait toujours les jointures et donc le compte n'est pas cohérent si on ne précise pas de paramètre
        if (/*StringUtils.isNotBlank(searchTerm) || */StringUtils.isNotBlank(family) || StringUtils.isNotBlank(genus) || StringUtils.isNotBlank(specificEpithet)) {
            queryString += "\n" + buildIdentificationJoin("i.id, i.fk_id_specimen");

            queryString += "\n" + buildTaxonJoin("fk_id_identification", searchTerm, family, genus, specificEpithet);
        }

        if (StringUtils.isNotBlank(country) || StringUtils.isNotBlank(continent) || StringUtils.isNotBlank(startDate) || StringUtils.isNotBlank(endDate) ||
            StringUtils.isNotBlank(collector)) {
            queryString += "\n" + buildCollectionEventJoin(country, continent, collector, startDate, endDate);
        }

        if (StringUtils.isNotBlank(storageName)) {
            queryString += "\n" + buildManagementJoin();
        }

        Query query = em.createNativeQuery(queryString);

        addParametersToQuery(query, searchTerm, state, institutionId, collectionId, collectionCode, family, genus, specificEpithet, startDate, endDate, collector,
                continent, country, nominativeCollection, storageName, collectionUuids);

        var result = query.getSingleResult();

        return (Long) result;
    }

    private String buildCollectionEventJoin(String country, String continent, String collector, String startDate, String endDate) {
        var whereClause = " where 1 = 1 ";
        if (StringUtils.isNotBlank(country)) {
            whereClause += " and upper(country) like upper(:country)";
        }

        if (StringUtils.isNotBlank(continent)) {
            whereClause += " and upper(continent) like upper(:continent)";
        }

        if (StringUtils.isNotBlank(collector)) {
            whereClause += " and upper(recorded_by) like upper(:collector)";
        }
        // TODO retiré pour le moment
        // if (StringUtils.isNotBlank(startDate)) {
        //     whereClause += " and event_date >= :startDate";
        // }
        //
        // if (StringUtils.isNotBlank(endDate)) {
        //     whereClause += " and event_date <= :endDate";
        // }

        return " join (select id from collection_event ce " + whereClause + ") as ce1_0 on ce1_0.id = s1_0.fk_colevent_id";
    }

    private String buildManagementJoin() {
        return " join (select id from management m where upper(storage_name) like upper(:storageName)) as m_0 on m_0.id = s1_0.fk_management_id";
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public CollectionIdentifier addAsDraft(Specimen specimen) {
        var collectionId = specimen.getCollectionId();
        final var colJPA = checkCollectionExist(collectionId);
        checkAttribut.checkUserRightsOnCollection(colJPA);

        checkIsDraft(specimen);
        return saveSpecimen(specimen, colJPA, SpecimenStatusEnum.DRAFT);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public CollectionIdentifier addAsReviewed(Specimen specimen) {
        final var colJPA = checkCollectionExist(specimen.getCollectionId());
        // Tout le monde peut soumettre à publication donc on vérifie juste que l'utilisateur a les droits sur la collection
        checkAttribut.checkUserRightsOnCollection(colJPA);

        return saveSpecimen(specimen, colJPA, SpecimenStatusEnum.REVIEW);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = CollectionManagerBusinessException.class)
    public CollectionIdentifier update(UUID specimenId, Specimen specimen) {
        var collectionId = specimen.getCollectionId();
        checkAttribut.checkSpecimenCreateOrUpdateAsPublished(collectionId);
        final var colJPA = checkCollectionExist(collectionId);
        final var specJPA = checkSpecimenExist(specimenId);

        checkAttribut.checkCollectionUpdate(colJPA, specJPA);

        validateSpecimen(specimen);
        CollectionIdentifier collectionIdentifier = buildAndUpdate(specimen, colJPA, specJPA, SpecimenStatusEnum.VALID);

        specimen.setId(collectionIdentifier.getSpecimenId());
        elasticService.addOrUpdateRefSpecimenElastic(specimen, colJPA);

        return collectionIdentifier;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public CollectionIdentifier updateAsDraft(UUID specimenId, Specimen specimen) {
        var collectionId = specimen.getCollectionId();
        checkAttribut.checkUserRightsOnCollection(collectionId);
        final var colJPA = checkCollectionExist(collectionId);
        final var specJPA = checkSpecimenExist(specimenId);

        checkAttribut.checkCollectionUpdate(colJPA, specJPA);

        CollectionIdentifier collectionIdentifier = buildAndUpdate(specimen, colJPA, specJPA, SpecimenStatusEnum.DRAFT);

        elasticService.deleteSpecimenToRefElastic(collectionIdentifier.getSpecimenId().toString());

        return collectionIdentifier;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public CollectionIdentifier updateAsReviewed(UUID specimenId, Specimen specimen) {
        var collectionId = specimen.getCollectionId();
        checkAttribut.checkUserRightsOnCollection(collectionId);

        final var colJPA = checkCollectionExist(collectionId);
        final var specJPA = checkSpecimenExist(specimenId);

        checkAttribut.checkCollectionUpdate(colJPA, specJPA);

        CollectionIdentifier collectionIdentifier = buildAndUpdate(specimen, colJPA, specJPA, SpecimenStatusEnum.REVIEW);

        elasticService.deleteSpecimenToRefElastic(collectionIdentifier.getSpecimenId().toString());

        return collectionIdentifier;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = CollectionManagerBusinessException.class)
    public List<UUID> updateMultipleSpecimen(List<UUID> specimenIds, SpecimenMerge specimen) {
        final var findUserAttributes = authenticationService.findUserAttributes();
        var listSpecJPAToMerge = specimenIds.stream().map(this::checkSpecimenExist).toList();
        var listState = listSpecJPAToMerge.stream().map(SpecimenJPA::getState).toList();
        var roleUser = RoleEnum.fromValue(findUserAttributes.getRole());
        checkStateRoleSpecimens(listState, roleUser);
        var listSpecimenIdsMergePatch = new ArrayList<UUID>();
        listSpecJPAToMerge.forEach(specJPA -> {
            var collectionId = specJPA.getCollection().getId();
            checkCollectionAndSpecimen(collectionId, specJPA.getId());
            var colJPA = collectionJPARepository.findById(collectionId).orElseThrow(
                    () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                            COLLECTION_WITH_ID + " :" + collectionId + NOT_FOUND));
            var spec = specimenMapper.mapJpaToSpecimen(specJPA);
            Specimen mergePatchToSpecimen;
            try {
                mergePatchToSpecimen = applyMergePatchToSpecimen(specimen, spec);
            } catch (JsonProcessingException | JsonPatchException e) {
                throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "CM_TECH_EXCEPTION",
                        e.getMessage());
            }
            validateSpecimen(mergePatchToSpecimen);
            elasticService.addOrUpdateRefSpecimenElastic(mergePatchToSpecimen, colJPA);
            var specMerge = specimenMapper.mapToSpecimenJpaForUpdate(mergePatchToSpecimen, findUserAttributes.getUi());
            var save = specimenJPARepository.save(specMerge);
            listSpecimenIdsMergePatch.add(save.getId());
        });
        log.info("updateMultipleSpecimen {}", specimenIds);
        return listSpecimenIdsMergePatch;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public List<CollectionIdentifier> bulkValidate(List<CollectionIdentifier> identifiers) {
        // apply authorisation
        if (CollectionUtils.isEmpty(identifiers)) {
            throw new CollectionManagerBusinessException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "no input of collection/specimen ");
        }
        identifiers.forEach(identifier -> checkAttribut.checkUserAuthAttributesForRoleAdminInst(identifier.getCollectionId()));
        final var specs = identifiers.stream().map(collectionIdentifier ->
                checkCollectionAndSpecimen(collectionIdentifier.getCollectionId(), collectionIdentifier.getSpecimenId())).toList();
        final var isRegularState = specs.stream()
                .allMatch(specimenJPA -> nonNull(specimenJPA.getState()) && !specimenJPA.getState().equals(SpecimenStatusEnum.VALID));
        if (!isRegularState) {
            throw new CollectionManagerBusinessException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "specimen state must be any of : REVIEW, DRAFT ");
        }
        final var allToSave = specs.stream().map(specimenJPA -> {
                    specimenJPA.setState(SpecimenStatusEnum.VALID);
                    return specimenJPA;
                }
        ).toList();
        specimenJPARepository.saveAll(allToSave);
        allToSave.forEach(specimenJPA -> {
            var collectionId = specimenJPA.getCollection().getId();
            checkCollectionAndSpecimen(collectionId, specimenJPA.getId());
            var colJPA = collectionJPARepository.findById(collectionId).orElseThrow(
                    () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                            COLLECTION_WITH_ID + " :" + collectionId + NOT_FOUND));
            elasticService.addOrUpdateRefSpecimenElastic(specimenMapper.mapJpaToSpecimenBasic(specimenJPA), colJPA);
        });
        asyncService.updateCacheStatistique();
        return identifiers;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, rollbackFor = CollectionManagerBusinessException.class)
    public void deleteSpecimen(UUID specimenId) {
        final var specimenJPA = checkSpecimenExist(specimenId);
        deletChildrenSpecimen(specimenJPA);
        specimenJPARepository.deleteSpecimen(specimenJPA.getId());
        elasticService.deleteSpecimenToRefElastic(specimenId.toString());
        asyncService.updateCacheStatistique();
    }

    /**
     * dans le cadre de la recherche avec scientifique name, c.a.d la seconde requete, celle ci est une native query.
     * un criteria builder n'est pas employé malgré le fait qu'un subquery, sur du from, soit possible a partir d' hibernate 6.
     * le pb est que d'origine, un choix, apparemment, a été fait sur l'absence de @manyToOne( name="fk_id_identification" ) sur le taxon
     * de ce fait on ne peut réalise de jointure, a partir d une requete criteria builder, partant du taxon
     */
    @Override
    @Transactional(readOnly = true)
    public SpecimenPage searchSpecimen(String searchWord, Integer pages, Integer size) {
        log.info("Search  specimen {}", searchWord);

        Page<SpecimenJPA> searchResult;
        var institutionId = authenticationService.findUserAttributes().getInstitution();
        searchResult = specimenJPARepository.filterByCatalogNumber(searchWord, institutionId, PageRequest.of(pages, size));

        if (searchResult.getTotalElements() != 0) {
            var totalPages = searchResult.getTotalPages();
            if (pages >= totalPages) {
                throw new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.name(), "Page: " + pages + " cannot be requested ");
            }
            var specList = searchResult.stream().map(specimenMapper::mapJpaToSpecimen).toList();
            return SpecimenPage.builder().specimen(specList).totalPages(totalPages).build();

        } else {

            Query cb = em.createNativeQuery(QUERY_NATIVE_SCIENTIF_NAME);
            cb.setParameter("searchWord", searchWord.toLowerCase().concat(LIKE_EXP));
            cb.setParameter("institutionId", institutionId);
            cb.setParameter("pages", pages * size);
            cb.setParameter("size", size);

            @SuppressWarnings("unchecked")
            List<Object[]> objectsArray = cb.getResultList();

            List<Specimen> specimens = new ArrayList<>();

            objectsArray.forEach(objetArray -> specimens.add(
                    Specimen.builder()
                            .id((UUID) objetArray[0])
                            .catalogNumber(objetArray[1] != null ? objetArray[1].toString() : null)
                            .state(SpecimenStatusEnum.valueOf(objetArray[2].toString()).name())
                            .collectionId(((UUID) objetArray[3]))
                            .collectionName(nonNull(objetArray[4]) ? objetArray[4].toString() : null)
                            .identifications(objetArray[5] != null ? getIdentificationSimpleToList.apply(objetArray) : new HashSet<>())
                            .build()));
            if (!specimens.isEmpty()) {

                Query cbcount = em.createNativeQuery(QUERY_NATIVE_SCIENTIF_NAME_COUNT, Integer.class);
                cbcount.setParameter("searchWord", searchWord.toLowerCase().concat(LIKE_EXP));
                cbcount.setParameter("institutionId", institutionId);

                Integer totalSpecimens = (Integer) cbcount.getSingleResult();
                int totalPages = (totalSpecimens + size - 1) / size;

                return SpecimenPage.builder().specimen(specimens).totalPages(totalPages).build();

            } else {
                return SpecimenPage.builder().specimen(Collections.emptyList()).totalPages(0).build();
            }
        }
    }

    @Override
    public PublicSpecimenDTO findDetailSpecimen(UUID specimenId) {
        var specimenJPA = specimenJPARepository.findSpecimenById(specimenId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        "Spécimen non trouvé"));

        Set<PublicIdentificationDTO> identificationsDTO = specimenJPA.getIdentifications().stream()
                .map(id -> identificationMapper.getTaxonListToTaxonSet(id, taxonMapper)).collect(Collectors.toSet());
        var dto = specimenMapper.toPublicDTO(specimenJPA);
        dto.setIdentifications(identificationsDTO.stream().toList());
        // Tri des médias avec l'image de couverture en premier
        dto.setMedias(dto.getMedias().stream().sorted(Comparator.comparing(PublicMediaDTO::getIsCover).reversed()).toList());
        return dto;
    }

    @Override
    public boolean pingElastic() {
        return elasticService.ping();
    }

    @Override
    public boolean indexElasticExist(String indexName) {
        return elasticService.verifyIndexExist(indexName);
    }

    private void checkStateRoleSpecimens(List<SpecimenStatusEnum> listState, RoleEnum roleUser) {

        switch (roleUser) {
            case DATA_ENTRY:
                if (listState.contains(SpecimenStatusEnum.VALID) || listState.contains(SpecimenStatusEnum.REVIEW)) {
                    throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                            "this user can't update a specimen with status valid or reviewed {}");
                }
                break;
            case ADMIN_COLLECTION:
                if (listState.contains(SpecimenStatusEnum.VALID)) {
                    throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                            "this user can't update a specimen with status valid {}");
                }
                break;
            case ADMIN_INSTITUTION:
                if (listState.contains(SpecimenStatusEnum.REVIEW)) {
                    throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                            "this user can't update a specimen with status reviewed {}");
                }
                break;
            default:
                throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                        "You can't duplicate a specimen with this role: " + roleUser.name());
        }
    }

    private Specimen applyMergePatchToSpecimen(SpecimenMerge specimen, Specimen target)
            throws JsonPatchException, JsonProcessingException {
        JsonNode convertValue = objectMapper.convertValue(specimen, JsonNode.class);
        var jsonMergePatch = JsonMergePatch.fromJson(convertValue);
        JsonNode patched = jsonMergePatch.apply(objectMapper.convertValue(target, JsonNode.class));
        return objectMapper.treeToValue(patched, Specimen.class);
    }

    private CollectionIdentifier buildAndUpdate(Specimen specimen, CollectionJPA colJPA, SpecimenJPA specJPA,
                                                SpecimenStatusEnum status) {

        final var uid = authenticationService.findUserAttributes().getUi();
        specimen.setCollectionId(colJPA.getId());
        final var updatedSpecimen = buildSpecimen(specimen, status);

        // map to jpa
        final var specJPAtoSave = specimenMapper.mapToSpecimenJpaForUpdate(updatedSpecimen, uid);

        // offside effect fix it or builder pattern
        specJPAtoSave.setId(specJPA.getId());

        // Le code de collection n'est pas modifiable depuis l'IHM
        specJPAtoSave.setCollectionCode(specJPA.getCollectionCode());
        specJPAtoSave.setIdentifications(buildFromIden(specJPA, specimen.getIdentifications()));
        specJPAtoSave.setMedias(buildMedia(specimen.getMedias()));
        specJPAtoSave.setOther(buildOther(specimen.getOther()));
        specJPAtoSave.setManagement(buildManagement(specimen.getManagement()));
        specJPAtoSave.setGeologicalContext(buildGeoContext(specimen.getGeologicalContext()));
        specJPAtoSave.setCollectionEvent(buildCollectionEvent(specimen.getCollectionEvent()));
        specJPAtoSave.setLiteratures(buildLiterature(specimen.getLiteratures()));
        var newMedias = specimen.getMedias();
        var odlMedias = specJPA.getMedias();
        if (checkDeleteMedia(odlMedias, newMedias)) {
            remove(odlMedias, newMedias);
        }

        specJPAtoSave.setCollection(colJPA);
        mediaComponent.ensureSingleCoverIsSet(specJPAtoSave.getMedias());
        final var savedSpec = saveSpecimenJPAAndUpdateMids(specJPAtoSave);

        return CollectionIdentifier.builder().specimenId(savedSpec.getId()).collectionId(colJPA.getId()).build();
    }


    public SpecimenJPA saveSpecimenJPAAndUpdateMids(SpecimenJPA specimenJPA) {
        specimenJPA.setMids(midsService.processMids(specimenMapper.mapJpaToSpecimen(specimenJPA)).level());
        return specimenJPARepository.save(specimenJPA);
    }


    private Set<LiteratureJPA> buildLiterature(Set<Literature> literatures) {
        log.info("buildLiterature");
        return Optional.ofNullable(literatures).stream().flatMap(Collection::stream)
                .map(literature -> LiteratureJPA.builder()
                        .id(literature.getId())
                        .identifier(literature.getIdentifier())
                        .authors(literature.getAuthors())
                        .date(literature.getDate() == null ? null : LocalDate.of(Integer.parseInt(literature.getDate()), 1, 1))
                        .title(literature.getTitle())
                        .review(literature.getReview())
                        .volume(literature.getVolume())
                        .number(literature.getNumber())
                        .pages(literature.getPages())
                        .publisher(literature.getPublisher())
                        .publicationPlace(literature.getPublicationPlace())
                        .editors(literature.getEditors())
                        .bookTitle(literature.getBookTitle())
                        .pageNumber(literature.getPageNumber())
                        .citation(literature.getCitation())
                        .language(literature.getLanguage())
                        .keywords(literature.getKeywords())
                        .description(literature.getDescription())
                        .url(literature.getUrl())
                        .remarks(literature.getRemarks())
                        .build())
                .collect(Collectors.toUnmodifiableSet());
    }

    private CollectionEventJPA buildCollectionEvent(CollectionEvent event) {
        if (nonNull(event)) {
            return CollectionEventJPA.builder().id(event.getId()).maximumDepthInMeters(event.getMaximumDepthInMeters())
                    .minimumDepthInMeters(event.getMinimumDepthInMeters())
                    .eventDate(event.getEventDate())
                    .interpretedDate(event.getInterpretedDate())
                    .eventRemarks(event.getEventRemarks())
                    .decimalLatitude(event.getDecimalLatitude())
                    .decimalLongitude(event.getDecimalLongitude())
                    .fieldNotes(event.getFieldNotes())
                    .fieldNumber(event.getFieldNumber())
                    .geodeticDatum(event.getGeodeticDatum())
                    .sensitiveLocation(event.getSensitiveLocation())
                    .recordedBy(event.getRecordedBy())
                    .interpretedAltitude(event.getInterpretedAltitude())
                    .location(buildLocation(event))
                    .habitat(event.getHabitat())
                    .minimumElevationInMeters(event.getMinimumElevationInMeters())
                    .maximumElevationInMeters(event.getMaximumElevationInMeters())
                    .verbatimLocality(event.getVerbatimLocality())
                    .interpretedDepth(event.getInterpretedDepth())
                    .georeferenceSources(event.getGeoreferenceSources())
                    .build();
        }
        return null;
    }

    private GeologicalContextJPA buildGeoContext(GeologicalContext geo) {
        if (nonNull(geo)) {
            return GeologicalContextJPA.builder().id(geo.getId())
                    .verbatimEpoch(geo.getVerbatimEpoch())
                    .ageAbsolute(geo.getAgeAbsolute())
                    .range(geo.getRange())
                    .earliestAgeOrLowestStage(geo.getEarliestAgeOrLowestStage())
                    .latestAgeOrHighestStage(geo.getLatestAgeOrHighestStage())
                    .earliestEpochOrLowestSeries(geo.getEarliestEpochOrLowestSeries())
                    .latestEpochOrHighestSeries(geo.getLatestEpochOrHighestSeries())
                    .earliestPeriodOrLowestSystem(geo.getEarliestPeriodOrLowestSystem())
                    .latestPeriodOrHighestSystem(geo.getLatestPeriodOrHighestSystem())
                    .earliestEraOrLowestErathem(geo.getEarliestEraOrLowestErathem())
                    .latestEraOrHighestErathem(geo.getLatestEraOrHighestErathem())
                    .earliestEonOrLowestEonothem(geo.getEarliestEonOrLowestEonothem())
                    .latestEonOrHighestEonothem(geo.getLatestEonOrHighestEonothem())
                    .lowestBiostratigraphicZone(geo.getLowestBiostratigraphicZone())
                    .highestBiostratigraphicZone(geo.getHighestBiostratigraphicZone())
                    .group(geo.getGroup())
                    .formation(geo.getFormation())
                    .member(geo.getMember())
                    .bed(geo.getBed())
                    .otherLithostratigraphicTerms(geo.getOtherLithostratigraphicTerms())
                    .build();
        }
        return null;
    }

    private OtherJPA buildOther(Other other1) {
        if (nonNull(other1)) {
            return OtherJPA.builder().id(other1.getId()).linkOther(other1.getLinkOther()).linkBold(other1.getLinkBold())
                    .linkGerBank(other1.getLinkGerBank()).computerizationProgram(other1.getComputerizationProgram())
                    .financialAid(other1.getFinancialAid()).remarks(other1.getRemarks()).build();
        }
        return null;
    }

    private ManagementJPA buildManagement(Management management) {
        if (nonNull(management)) {
            return ManagementJPA.builder().id(management.getId()).storageName(management.getStorageName()).build();
        }
        return null;
    }

    private Set<MediaJPA> buildMedia(List<Media> medias) {
        return Optional.ofNullable(medias).stream().flatMap(Collection::stream)
                .map(media -> MediaJPA.builder()
                        .id(media.getId())
                        .contributor(media.getContributor())
                        .creator(media.getCreator())
                        .description(media.getDescription())
                        .license(media.getLicense())
                        .source(media.getSource())
                        .mediaUrl(media.getMediaUrl())
                        .mediaName(media.getMediaName())
                        .isCover(media.getIsCover())
                        .build())
                .collect(Collectors.toSet());
    }

    public CollectionIdentifier saveSpecimen(Specimen specimenToSave, CollectionJPA colJPA,
                                             SpecimenStatusEnum status) {
        var uid = authenticationService.findUserAttributes().getUi();
        if (log.isInfoEnabled()) {
            log.info("Specimen to save : {}", specimenToSave);
        }
        final var spec = buildSpecimen(specimenToSave, status);
        final var specJPA = specimenMapper.mapToSpecimenJpa(spec, uid);

        specJPA.setCollection(colJPA);
        mediaComponent.ensureSingleCoverIsSet(specJPA.getMedias());
        final var savedSpec = saveSpecimenJPAAndUpdateMids(specJPA);

        return CollectionIdentifier.builder().collectionId(colJPA.getId()).specimenId(savedSpec.getId()).build();
    }


    private Specimen buildSpecimen(Specimen specimenToSave, SpecimenStatusEnum status) {
        return Specimen.builder()
                .catalogNumber(specimenToSave.getCatalogNumber())
                .collectionId(specimenToSave.getCollectionId())
                .recordNumber(specimenToSave.getRecordNumber())
                .basisOfRecord(specimenToSave.getBasisOfRecord())
                .preparations(specimenToSave.getPreparations())
                .individualCount(specimenToSave.getIndividualCount())
                .lifeStage(specimenToSave.getLifeStage())
                .occurrenceRemarks(specimenToSave.getOccurrenceRemarks())
                .donor(specimenToSave.getDonor())
                .legalStatus(specimenToSave.getLegalStatus())
                .sex(specimenToSave.getSex())
                .state(status.name())
                .createdAt(specimenToSave.getCreatedAt())
                .createdBy(specimenToSave.getCreatedBy())
                .geologicalContext(specimenToSave.getGeologicalContext())
                .collectionEvent(specimenToSave.getCollectionEvent())
                .literatures(specimenToSave.getLiteratures())
                .identifications(specimenToSave.getIdentifications())
                .medias(specimenToSave.getMedias())
                .nominativeCollection(specimenToSave.getNominativeCollection())
                .other(specimenToSave.getOther())
                .management(specimenToSave.getManagement())
                .build();
    }

    Set<IdentificationJPA> buildFromIden(SpecimenJPA specimenJPA, Set<Identification> identifications) {
        if (CollectionUtils.isEmpty(identifications)) {
            return Collections.emptySet();
        }
        final var identificationIds = specimenJPA.getIdentifications().stream().map(AbstractEntity::getId)
                .collect(Collectors.toUnmodifiableSet());

        return identifications.stream().map(identification -> {
            if (nonNull(identification.getId()) && (!identificationIds.contains(identification.getId()))) {
                final var e = new CollectionManagerBusinessException(ErrorCode.ERR_CODE_CM,
                        "Identification with id :" + identification.getId() + NOT_FOUND);
                log.error(e.getMessage(), e);
                throw e;
            }
            return identification;
        }).map(identification -> IdentificationJPA.builder().id(identification.getId())
                .currentDetermination(identification.getCurrentDetermination()).dateIdentified(identification.getDateIdentified())
                .dateIdentifiedEnd(identification.getDateIdentifiedEnd())
                .dateIdentifiedFormat(identification.getDateIdentifiedFormat())
                .identifiedByID(identification.getIdentifiedByID())
                .identificationRemarks(identification.getIdentificationRemarks())
                .currentDetermination(identification.getCurrentDetermination())
                .errorMessage(identification.getErrorMessage()).typeStatus(identification.getTypeStatus())
                .verbatimIdentification(identification.getVerbatimIdentification())
                .identificationVerificationStatus(identification.getIdentificationVerificationStatus())
                .taxon(buildListTaxon(identification.getTaxon())).build()).collect(Collectors.toUnmodifiableSet());
    }

    private List<TaxonJPA> buildListTaxon(List<Taxon> taxons) {
        if (!CollectionUtils.isEmpty(taxons)) {
            return taxons.stream().map(taxon -> TaxonJPA.builder().id(taxon.getId())
                    .scientificName(taxon.getScientificName())
                    .scientificNameAuthorship(taxon.getScientificNameAuthorship())
                    .kingdom(taxon.getKingdom())
                    .phylum(taxon.getPhylum())
                    .taxonClass(taxon.getTaxonClass())
                    .taxonOrder(taxon.getTaxonOrder())
                    // Un taxon de type MASTER ne doit pas avoir d'identifiant de référentiel
                    .referentialTaxonId(LevelTypeEnum.MASTER.equals(taxon.getLevelType()) ? null : taxon.getReferentialTaxonId())
                    .subOrder(taxon.getSubOrder())
                    .family(taxon.getFamily())
                    .subFamily(taxon.getSubFamily())
                    .genus(taxon.getGenus())
                    .subGenus(taxon.getSubGenus())
                    .specificEpithet(taxon.getSpecificEpithet())
                    .infraspecificEpithet(taxon.getInfraspecificEpithet())
                    .vernacularName(taxon.getVernacularName())
                    .taxonRemarks(taxon.getTaxonRemarks())
                    .referentialName(taxon.getReferentialName())
                    .referentialVersion(taxon.getReferentialVersion())
                    .levelType(taxon.getLevelType()).build()).collect(Collectors.toUnmodifiableList());
        }
        return Collections.emptyList();
    }

    private void validateSpecimen(Specimen specimenToSave) {
        var violations = validator.validate(specimenToSave, NormalCheck.class);

        if (!CollectionUtils.isEmpty(violations)) {
            throw new CollectionManagerBusinessException(ERR_CODE_INVALID_REQUEST,
                    validator.validate(specimenToSave, NormalCheck.class).stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(";")));
        }

        if (specimenToSave.getIdentifications().stream()
                    .filter(identification -> nonNull(identification.getCurrentDetermination()) && identification.getCurrentDetermination())
                    .count() > 1) {
            var exception = new CollectionManagerBusinessException(
                    ERR_CODE_INVALID_REQUEST, "At most one active identification");
            log.error(exception.getMessage(), exception);
            throw exception;
        }

        if (specimenToSave.getIdentifications().size() > 1 && specimenToSave.getIdentifications().stream()
                .allMatch(identification -> nonNull(identification.getCurrentDetermination()) && identification.getCurrentDetermination())) {
            var exception = new CollectionManagerBusinessException(
                    ERR_CODE_INVALID_REQUEST, "At most one active identification");
            log.error(exception.getMessage(), exception);
            throw exception;
        }

        specimenToSave.getIdentifications().forEach(identification -> {
            log.info("identification : {}", identification);
            if (identification.getTaxon().stream().filter(taxon -> LevelTypeEnum.MASTER.equals(taxon.getLevelType())).count() != 1) {
                //exception
                var exception = new CollectionManagerBusinessException(
                        ERR_CODE_INVALID_REQUEST, "You must have only one master taxon type per identification");
                log.error(exception.getMessage(), exception);
                throw exception;
            }
        });
    }

    private void checkIsDraft(Specimen specimen) {
        if (!specimen.isDraftValid()) {
            throw new CollectionManagerBusinessException("ERR_CODE_MISSING_FIELDS", "There are missing fields");
        }
    }

    public CollectionJPA checkCollectionExist(UUID collectionId) {
        return collectionJPARepository.findById(collectionId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        "Collection with id :" + collectionId + NOT_FOUND));
    }

    public SpecimenJPA checkSpecimenExist(UUID specimenId) {
        return specimenJPARepository.findById(specimenId).orElseThrow(
                () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                        "Specimen with id :" + specimenId + NOT_FOUND));
    }

    private boolean checkDeleteMedia(Set<MediaJPA> odlMedias, List<Media> newMedias) {
        if (!odlMedias.isEmpty() && isLowerOldMedia(odlMedias, newMedias)) {
            return urlNewMediaIsNotEmpty(newMedias);
        }
        return false;
    }

    public void remove(Set<MediaJPA> odlMedias, List<Media> newMedias) {
        List<String> odlMediasList = odlMedias.stream().map(MediaJPA::getMediaUrl).toList();
        List<String> newMediasList = newMedias.stream().map(Media::getMediaUrl).toList();
        odlMediasList.stream()
                .filter(element -> !newMediasList.contains(element))
                .filter(Objects::nonNull)
                .forEach(deleteUrl -> {
                    var deletePicture = mediathequeService.deletePicture(getUidMedia(deleteUrl));
                    log.info("Delete url {}, request: {}", deleteUrl, deletePicture.getStatusCode().value());
                });
    }

    private String getUidMedia(String uriMedia) {
        String[] split = uriMedia.split("/");
        return Arrays.stream(split)
                .reduce((first, second) -> second).orElseThrow(() ->
                        new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.name(), "UriMedia not found {}"));
    }

    private SpecimenJPA checkCollectionAndSpecimen(UUID collectionId, UUID specimenId) {
        return specimenJPARepository.findByIdAndCollectionId(specimenId, collectionId)
                .orElseThrow(() -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.name(), "Unrelated collection:" + collectionId + " and specimen : " + specimenId));
    }


    private void deletChildrenSpecimen(SpecimenJPA specimenJPA) {
        if (nonNull(specimenJPA.getCollectionEvent())) {
            collectionEventJPARepository.removeById(specimenJPA.getCollectionEvent().getId());
        }
        if (nonNull(specimenJPA.getGeologicalContext())) {
            geologicalContextJPARepository.removeById(specimenJPA.getGeologicalContext().getId());
        }
        if (nonNull(specimenJPA.getOther())) {
            otherJPARepository.removeById(specimenJPA.getOther().getId());
        }
        if (nonNull(specimenJPA.getManagement())) {
            managementJPARepository.removeById(specimenJPA.getManagement().getId());
        }
    }

    @Override
    public List<String> getCountriesByPrefix(String query, Integer size) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return collectionEventJPARepository.findCountriesStartingWith(query + '%', PageRequest.of(0, size));
    }

    @Override
    public List<String> getContinentsByPrefix(String query, Integer size) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return collectionEventJPARepository.findContinentsStartingWith(query + '%', PageRequest.of(0, size));
    }

    @Override
    public List<String> getRecordersByPrefix(String query, Integer size) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return collectionEventJPARepository.findRecordersStartingWith(query + '%', PageRequest.of(0, size));
    }

    @Override
    public List<String> getNominativeCollections(String query, Integer size) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return specimenJPARepository.findNominativeCollectionsStartingWith(query + '%', PageRequest.of(0, size));
    }

    @Override
    public List<String> getNominativeCollectionsByInstitutionId(UUID institutionId) {
        return specimenJPARepository.findDistinctNominativeCollectionsByInstitutionId(institutionId);
    }

    @Override
    public List<String> getStorageNames(String query, Integer size) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyList();
        }
        return specimenJPARepository.findStorageNamesContains('%' + query + '%', PageRequest.of(0, size));
    }

    @Override
    public boolean exists(UUID collectionId, String catalogNumber, UUID specimenId) {
        return specimenJPARepository.alreadyExists(collectionId, catalogNumber, specimenId != null ? specimenId.toString() : null);
    }

    @Override
    public Boolean hasSpecimentToPublish(UUID institutionUuid) {
        var currentUser = authenticationService.findUserAttributes();
        Integer institutionId = getInstitutionIdToFilter(institutionUuid, currentUser);

        List<UUID> userCollectionUuids = getUserCollectionUuids(currentUser);

        return countSpecimen(null, OperationTypeDTO.REVIEW, institutionId, null, null, null, null, null, null, null, null, null, null, null, null, userCollectionUuids) > 0;
    }

    /**
     * Récupère les identifiants des collections associées à un utilisateur (ADMIN_COLLECTION et DATA_ENTRY)
     *
     * @param currentUser utilisateur
     * @return une liste d'UUID
     */
    private List<UUID> getUserCollectionUuids(UserAttributes currentUser) {
        if (List.of(RoleEnum.ADMIN_COLLECTION, RoleEnum.DATA_ENTRY).contains(RoleEnum.fromValue(currentUser.getRole()))) {
            return currentUser.getCollections();
        }
        return Collections.emptyList();
    }

    /**
     * Retourne l'identifiant numérique de l'institution à prendre en compte pour le filtre en fonction de l'utilisateur.
     *
     * <ul>
     *     <li>L'institution passée en paramètre si présente et que l'utilisateur est admin</li>
     *     <li>L'institution de l'utilisateur si il n'est pas admin</li>
     *     <li>null sinon</li>
     * </ul>
     *
     * @param institutionUuid UUID de l'institution passé en paramètre (ADMIN)
     * @param currentUser     utilisateur
     * @return un identifiant numérique
     */
    private Integer getInstitutionIdToFilter(UUID institutionUuid, UserAttributes currentUser) {
        var isAdmin = RoleEnum.ADMIN.equals(RoleEnum.fromValue(currentUser.getRole()));

        // Seul l'administrateur a le droit de spécifier une institution
        if (isAdmin && institutionUuid != null) {
            InstitutionJPA inst = institutionRepository
                    .findInstitutionByInstitutionId(institutionUuid).orElseThrow(
                            () ->
                                    new CollectionManagerBusinessException(
                                            HttpStatus.NOT_FOUND,
                                            ErrorCode.ERR_NFE_CODE,
                                            "institutionId not found with UUID :" + institutionUuid)
                    );
            return inst.getId();
        } else if (!isAdmin) {
            return authenticationService.findUserAttributes().getInstitution();
        }
        return null;
    }
}


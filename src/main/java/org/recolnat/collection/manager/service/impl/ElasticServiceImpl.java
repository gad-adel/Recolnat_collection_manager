package org.recolnat.collection.manager.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.api.domain.Location;
import org.recolnat.collection.manager.api.domain.Media;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.SpecimenIndex;
import org.recolnat.collection.manager.api.domain.Taxon;
import org.recolnat.collection.manager.api.domain.enums.LanguageEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.mapper.SpecimenMapper;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.ElasticService;
import org.recolnat.collection.manager.service.InstitutionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.recolnat.collection.manager.common.check.service.ControlAttribut.COLLECTION_WITH_ID;
import static org.recolnat.collection.manager.service.impl.SpecimenIntegrationServiceImpl.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticServiceImpl implements ElasticService {

    public static final String ERROR_MSG = "Error while performing the operation";
    public static final String ERROR_ELASTIC = "error elastic";

    private final ElasticsearchClient elasticsearchClient;
    private final InstitutionService institutionService;
    private final SpecimenJPARepository specimenJPARepository;
    private final SpecimenMapper specimenMapper;
    private final CollectionJPARepository collectionJPARepository;
    private final InstitutionRepositoryJPA institutionRepositoryJPA;

    @Value("${index.specimen}")
    String indexSpecimen;


    @Override
    public void addOrUpdateRefSpecimenElastic(Specimen specimen, CollectionJPA collection, Institution institution) {
        addOrUpdateSpecimen(specimen, collection, institution);
    }

    @Override
    public void addOrUpdateRefSpecimenElastic(Specimen specimen, CollectionJPA collection) {
        Institution institution = institutionService.getInstitutionById(collection.getInstitutionId(), LanguageEnum.FR.name());
        addOrUpdateSpecimen(specimen, collection, institution);
    }

    private void addOrUpdateSpecimen(Specimen specimen, CollectionJPA collection, Institution institution) {
        try {
            SpecimenIndex specimenIndex = specimenIndexFromSpecimen(specimen, collection, institution);
            IndexResponse response = elasticsearchClient.index(i -> i
                    .index(indexSpecimen)
                    .id(specimenIndex.getId())
                    .document(specimenIndex)
            );

            var operationName = response.result().name();
            switch (operationName) {
                case "Created" -> {
                    if (log.isDebugEnabled()) {
                        log.debug("SpecimenIndex Document {} has been successfully created.", specimen.getId());
                    }
                }
                case "Updated" -> {
                    if (log.isDebugEnabled()) {
                        log.debug("SpecimenIndex Document  {} has been successfully updated.", specimen.getId());
                    }
                }
                case "NoOp" -> {
                    if (log.isErrorEnabled()) {
                        log.error("SpecimenIndex Document {} without upgrade.", specimen.getId());
                    }
                }
                default -> {
                    log.error(ERROR_MSG);
                    throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ERROR_ELASTIC, response.result().toString());
                }
            }

        } catch (Exception e) {
            log.error(ERROR_MSG + e.getMessage());
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, String.format(ERROR_ELASTIC + " on %s ", specimen.getId()), e.getMessage());
        }
    }

    @Override
    public void updatePartialSpecimenToRefElastic(SpecimenIndex specimenIndex) {
        try {
            UpdateResponse<SpecimenIndex> response = elasticsearchClient.update(i -> i
                    .index(indexSpecimen)
                    .id(specimenIndex.getId())
                    .doc(specimenIndex), SpecimenIndex.class);
            switch (response.result().name()) {
                case "Created" -> log.info("SpecimenIndex Document has been successfully created.");
                case "Updated" -> log.info("SpecimenIndex Document has been successfully updated.");
                case "NoOp" -> {
                    if (log.isErrorEnabled()) {
                        log.error("SpecimenIndex Document {} without upgrade.", specimenIndex.getId());
                    }
                }
                default -> {
                    log.error(ERROR_MSG);
                    throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ERROR_ELASTIC, response.result().toString());
                }
            }
        } catch (Exception e) {
            log.error(ERROR_MSG + e.getMessage());
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ERROR_ELASTIC, e.getMessage());
        }
    }

    @Override
    public void deleteSpecimenToRefElastic(String uuid) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d.index(indexSpecimen).id(uuid));

            DeleteResponse deleteResponse = elasticsearchClient.delete(request);
            if (Objects.nonNull(deleteResponse.result()) && !deleteResponse.result().name().equals("NotFound")) {
                log.debug("Specimen with id {} has been deleted.", deleteResponse.id());
            } else {
                log.info("Specimen with id {} does not exist.", deleteResponse.id());
            }
        } catch (Exception e) {
            log.error("Error while deleting the operation. {}", e.getMessage());
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ERROR_ELASTIC, e.getMessage());
        }

    }

    @Override
    public boolean ping() {
        try {
            return elasticsearchClient.ping().value();
        } catch (ElasticsearchException | IOException e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ERROR_ELASTIC, e.getMessage());
        }
    }

    @Override
    public boolean verifyIndexExist(final String indiceName) {
        try {
            return elasticsearchClient.indices().exists(ExistsRequest.of(e -> e.index(indiceName))).value();
        } catch (ElasticsearchException | IOException e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ERROR_ELASTIC, e.getMessage());
        }
    }

    @Override
    public boolean specimenExistInIndex(final String uuid) {
        try {
            SearchResponse<SpecimenIndex> response = elasticsearchClient.search(s -> s
                    .index(indexSpecimen).query(q -> q.match(t -> t.field("id").query(uuid))), SpecimenIndex.class);
            TotalHits total = response.hits().total();
            return total != null && total.value() > 0L;

        } catch (ElasticsearchException | IOException e) {
            log.error("Error during search specimen operation." + e.getMessage());
            return false;
        }
    }

    @Override
    public void bulkUpdate(SpecimenIndex specimen, List<String> ids) throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();

        for (String id : ids) {
            br.operations(op -> op
                    .update(idx -> idx
                            .index(indexSpecimen)
                            .action(
                                    objectObjectBuilder -> objectObjectBuilder.doc(specimen).docAsUpsert(true)
                            ).id(id)
                    )
            );
        }

        if (!ids.isEmpty()) {
            elasticsearchClient.bulk(br.build());
        }
    }

    /**
     * transformation du pojo Specimen en pojo SpecimenIndex  pour envoi vers Elastic
     *
     * @param specimen      spécimen à transformer
     * @param collectionJPA collection du spécimen
     * @param institution   institution du spécimen
     * @return le spécimen formaté pour ElasticSearch
     */
    protected SpecimenIndex specimenIndexFromSpecimen(Specimen specimen, CollectionJPA collectionJPA, Institution institution) {
        List<String> scientificNames = new ArrayList<>();
        List<String> scientificNameAuthorships = new ArrayList<>();
        List<String> genus = new ArrayList<>();
        List<String> vernacularNames = new ArrayList<>();
        List<String> families = new ArrayList<>();
        List<String> specificEpithets = new ArrayList<>();
        List<String> identificationByIds = new ArrayList<>();
        List<String> typesStatus = new ArrayList<>();

        specimen.getIdentifications().stream().sorted((o1, o2) -> Boolean.compare(o2.getCurrentDetermination(), o1.getCurrentDetermination())).forEach(i -> {
            i.getTaxon().stream().sorted(Comparator.comparing(Taxon::getLevelType)).forEach(t -> {
                scientificNames.add(t.getScientificName());
                if (t.getScientificNameAuthorship() != null) {
                    scientificNameAuthorships.add(t.getScientificNameAuthorship());
                }
                genus.add(t.getGenus());
                vernacularNames.add(t.getVernacularName());
                families.add(t.getFamily());
                specificEpithets.add(t.getSpecificEpithet());
            });
            if (i.getIdentifiedByID() != null) {
                identificationByIds.add(i.getIdentifiedByID());
            }
            if (i.getTypeStatus() != null) {
                typesStatus.add(i.getTypeStatus());
            }
        });

        SpecimenIndex.SpecimenIndexBuilder specimenIndexBuilder = SpecimenIndex.builder();

        specimenIndexBuilder
                .id(specimen.getId().toString())
                .catalogNumber(specimen.getCatalogNumber())
                .collectionCode(collectionJPA.getCollectionCode() != null ? collectionJPA.getCollectionCode() : specimen.getCollectionCode())
                .nominativeCollection(specimen.getNominativeCollection())
                .institutionId(institution.getInstitutionId())
                .institutionName(institution.getName())
                .institutionLogoUrl(institution.getLogoUrl())
                .collectionId(collectionJPA.getId().toString())
                .domain(collectionJPA.getTypeCollection())
                .collectionNameFr(collectionJPA.getCollectionNameFr())
                .collectionNameEn(collectionJPA.getCollectionNameEn());

        CollectionEvent collectionEvent = specimen.getCollectionEvent();
        if (collectionEvent != null) {
            Location location = collectionEvent.getLocation();
            if (location != null) {
                specimenIndexBuilder
                        .continent(location.getContinent())
                        .region(location.getRegion())
                        .locality(location.getLocality())
                        .island(location.getIsland())
                        .islandGroup(location.getIslandGroup())
                        .country(location.getCountry())
                        .county(location.getCounty())
                        .waterBody(location.getWaterBody())
                        .municipality(location.getMunicipality());
            }

            var latitude = getFloatValue(collectionEvent.getDecimalLatitude());
            var longitude = getFloatValue(collectionEvent.getDecimalLongitude());
            specimenIndexBuilder
                    .collectionDate(collectionEvent.getEventDate())
                    .fieldNumber(collectionEvent.getFieldNumber())
                    .recordedBy(collectionEvent.getRecordedBy())
                    .decimalLatitude(latitude)
                    .decimalLongitude(longitude);
            if (latitude != null && longitude != null) {
                specimenIndexBuilder.geojson(new Float[]{longitude, latitude});
            }
        }

        var mediaUrl = Optional.ofNullable(specimen.getMedias())
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .filter(m -> Boolean.TRUE.equals(m.getIsCover()))
                        .findFirst()
                        .orElse(list.get(0))
                )
                .map(Media::getMediaUrl)
                .filter(StringUtils::isNotEmpty)
                .orElse(null);

        specimenIndexBuilder
                .scientificNames(scientificNames.toArray(new String[0]))
                .scientificNameAuthorships(scientificNameAuthorships.toArray(new String[0]))
                .genus(genus.toArray(new String[0]))
                .family(families.toArray(new String[0])).specificEpithet(specificEpithets.toArray(new String[0]))
                .vernacularName(vernacularNames.toArray(new String[0]))
                .institutionLogoUrl(institution.getLogoUrl())
                .identificationByIds(identificationByIds.toArray(new String[0]))
                .typesStatus(typesStatus.toArray(new String[0]))
                .mediaUrl(mediaUrl);

        return specimenIndexBuilder.build();
    }

    @Override
    public void updateSpecimenFromImport(UUID id, UUID institutionId) {
        var specimens = specimenJPARepository.findSpecimenByImportId(id);
        log.info("Mise à jour de {} specimens dans l'index ES pour l'import {}", specimens.size(), id);
        long start = System.nanoTime();

        var institutionJPA = institutionRepositoryJPA.findInstitutionByInstitutionId(institutionId).orElseThrow();

        Institution institution = institutionService.getInstitutionById(institutionJPA.getId(), LanguageEnum.FR.name());

        specimens.forEach(specimen -> {
            var specimenJPAOpt = specimenJPARepository.findSpecimenById(specimen.getId());
            if (specimenJPAOpt.isPresent()) {
                var specimenJPA = specimenJPAOpt.get();
                var collectionId = specimenJPA.getCollection().getId();
                var colJPA = collectionJPARepository.findById(collectionId).orElseThrow(
                        () -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), ErrorCode.ERR_CODE_CM,
                                COLLECTION_WITH_ID + " :" + collectionId + NOT_FOUND));

                addOrUpdateRefSpecimenElastic(specimenMapper.mapJpaToSpecimenBasic(specimenJPA), colJPA, institution);
            }
        });
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        log.info("Mise à jour terminée en {} ms", timeElapsed / 1000000);
    }

    private Float getFloatValue(Double doubleValue) {
        return doubleValue != null ? doubleValue.floatValue() : null;
    }

}

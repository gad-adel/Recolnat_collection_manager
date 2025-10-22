package org.recolnat.collection.manager.service.imports;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.api.domain.ImportColumn;
import org.recolnat.collection.manager.api.domain.Location;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.Taxon;
import org.recolnat.collection.manager.api.domain.enums.SpecimenStatusEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportModeEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.SpecimenUpdateModeEnum;
import org.recolnat.collection.manager.api.domain.imports.ImportSpecimenProcessorResult;
import org.recolnat.collection.manager.common.mapper.SpecimenMapper;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.MidsService;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.recolnat.collection.manager.web.dto.MidsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum.BOOLEAN;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum.DATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum.DOUBLE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum.INTERVAL;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportSpecimenProcessor {

    private final ImportHelper importHelper;
    private final List<ImportColumn> specimenColumns = List.of(
            new ImportColumn("catalog_number", CATALOG_NUMBER.getColumnName()),
            new ImportColumn("collection_code", COLLECTION_CODE.getColumnName()),
            new ImportColumn("nominative_collection", NOMINATIVE_COLLECTION.getColumnName()),
            new ImportColumn("record_number", RECORD_NUMBER.getColumnName()),
            new ImportColumn("basis_of_record", BASIS_OF_RECORD.getColumnName()),
            new ImportColumn("preparations", PREPARATIONS.getColumnName()),
            new ImportColumn("sex", SEX.getColumnName()),
            new ImportColumn("life_stage", LIFE_STAGE.getColumnName()),
            new ImportColumn("individual_count", INDIVIDUAL_COUNT.getColumnName()),
            new ImportColumn("occurrence_remarks", OCCURRENCE_REMARKS.getColumnName()),
            new ImportColumn("legal_status", LEGAL_STATUS.getColumnName()),
            new ImportColumn("donor", DONOR.getColumnName())
    );
    private final List<ImportColumn> collectionEventColumns = List.of(
            new ImportColumn("event_date", "COLLECTE", INTERVAL),
            new ImportColumn("interpreted_date", INTERPRETED_DATE.getColumnName(), BOOLEAN),
            new ImportColumn("recorded_by", RECORDED_BY.getColumnName()),
            new ImportColumn("field_number", FIELD_NUMBER.getColumnName()),
            new ImportColumn("field_notes", FIELD_NOTES.getColumnName()),
            new ImportColumn("event_remarks", EVENT_REMARKS.getColumnName()),
            new ImportColumn("verbatim_locality", VERBATIM_LOCALITY.getColumnName()),
            new ImportColumn("sensitive_location", SENSITIVE_LOCATION.getColumnName(), BOOLEAN),
            new ImportColumn("decimal_latitude", DECIMAL_LATITUDE.getColumnName(), DOUBLE),
            new ImportColumn("decimal_longitude", DECIMAL_LONGITUDE.getColumnName(), DOUBLE),
            new ImportColumn("geodetic_datum", GEODETIC_DATUM.getColumnName()),
            new ImportColumn("georeference_sources", GEOREFERENCE_SOURCES.getColumnName()),
            new ImportColumn("minimum_elevation_in_meters", MINIMUM_ELEVATION_IN_METERS.getColumnName(), DOUBLE),
            new ImportColumn("maximum_elevation_in_meters", MAXIMUM_ELEVATION_IN_METERS.getColumnName(), DOUBLE),
            new ImportColumn("interpreted_altitude", INTERPRETED_ALTITUDE.getColumnName(), BOOLEAN),
            new ImportColumn("minimum_depth_in_meters", MINIMUM_DEPTH_IN_METERS.getColumnName(), DOUBLE),
            new ImportColumn("maximum_depth_in_meters", MAXIMUM_DEPTH_IN_METERS.getColumnName(), DOUBLE),
            new ImportColumn("interpreted_depth", INTERPRETED_DEPTH.getColumnName(), BOOLEAN),
            new ImportColumn("locality", LOCALITY.getColumnName()),
            new ImportColumn("municipality", MUNICIPALITY.getColumnName()),
            new ImportColumn("county", COUNTY.getColumnName()),
            new ImportColumn("region", REGION.getColumnName()),
            new ImportColumn("state_province", STATE_PROVINCE.getColumnName()),
            new ImportColumn("country", COUNTRY.getColumnName()),
            new ImportColumn("country_code", COUNTRY_CODE.getColumnName()),
            new ImportColumn("continent", CONTINENT.getColumnName()),
            new ImportColumn("island", ISLAND.getColumnName()),
            new ImportColumn("island_group", ISLAND_GROUP.getColumnName()),
            new ImportColumn("water_body", WATER_BODY.getColumnName()),
            new ImportColumn("habitat", HABITAT.getColumnName()),
            new ImportColumn("location_remarks", LOCATION_REMARKS.getColumnName())
    );
    private final List<ImportColumn> geologicalContextColumns = List.of(
            new ImportColumn("verbatim_epoch", VERBATIM_EPOCH.getColumnName()),
            new ImportColumn("age_absolute", AGE_ABSOLUTE.getColumnName()),
            new ImportColumn("earliest_age_or_lowest_stage", EARLIEST_AGE_OR_LOWEST_STAGE.getColumnName()),
            new ImportColumn("latest_age_or_highest_stage", LATEST_AGE_OR_HIGHEST_STAGE.getColumnName()),
            new ImportColumn("earliest_epoch_or_lowest_series", EARLIEST_EPOCH_OR_LOWEST_SERIES.getColumnName()),
            new ImportColumn("latest_epoch_or_highest_series", LATEST_EPOCH_OR_HIGHEST_SERIES.getColumnName()),
            new ImportColumn("earliest_period_or_lowest_system", EARLIEST_PERIOD_OR_LOWEST_SYSTEM.getColumnName()),
            new ImportColumn("latest_period_or_highest_system", LATEST_PERIOD_OR_HIGHEST_SYSTEM.getColumnName()),
            new ImportColumn("earliest_era_or_lowest_erathem", EARLIEST_ERA_OR_LOWEST_ERATHEM.getColumnName()),
            new ImportColumn("latest_era_or_highest_erathem", LATEST_ERA_OR_HIGHEST_ERATHEM.getColumnName()),
            new ImportColumn("earliest_eon_or_lowest_eonothem", EARLIEST_EON_OR_LOWEST_EONOTHEM.getColumnName()),
            new ImportColumn("latest_eon_or_highest_eonothem", LATEST_EON_OR_HIGHEST_EONOTHEM.getColumnName()),
            new ImportColumn("lowest_biostratigraphic_zone", LOWEST_BIOSTRATIGRAPHIC_ZONE.getColumnName()),
            new ImportColumn("highest_biostratigraphic_zone", HIGHEST_BIOSTRATIGRAPHIC_ZONE.getColumnName()),
            new ImportColumn("geo_group", GEO_GROUP.getColumnName()),
            new ImportColumn("formation", FORMATION.getColumnName()),
            new ImportColumn("member", MEMBER.getColumnName()),
            new ImportColumn("bed", BED.getColumnName()),
            new ImportColumn("other_lithostratigraphic_terms", OTHER_LITHOSTRATIGRAPHIC_TERMS.getColumnName())
    );

    private final List<ImportColumn> identificationColumns = List.of(
            new ImportColumn("verbatim_identification", VERBATIM_IDENTIFICATION.getColumnName()),
            new ImportColumn("identification_verification_status", IDENTIFICATION_VERIFICATION_STATUS.getColumnName(), BOOLEAN),
            new ImportColumn("identification_remarks", IDENTIFICATION_REMARKS.getColumnName()),
            new ImportColumn("type_status", TYPE_STATUS.getColumnName()),
            new ImportColumn("identified_byid", IDENTIFIED_BYID.getColumnName()),
            new ImportColumn("date_identified", "DETERMINATION", DATE)
    );

    private final List<ImportColumn> taxonColumns = List.of(
            new ImportColumn("scientific_name", SCIENTIFIC_NAME.getColumnName()),
            new ImportColumn("scientific_name_authorship", SCIENTIFIC_NAME_AUTHORSHIP.getColumnName()),
            new ImportColumn("vernacular_name", VERNACULAR_NAME.getColumnName()),
            new ImportColumn("family", FAMILY.getColumnName()),
            new ImportColumn("sub_family", SUB_FAMILY.getColumnName()),
            new ImportColumn("genus", GENUS.getColumnName()),
            new ImportColumn("sub_genus", SUB_GENUS.getColumnName()),
            new ImportColumn("specific_epithet", SPECIFIC_EPITHET.getColumnName()),
            new ImportColumn("infraspecific_epithet", INFRASPECIFIC_EPITHET.getColumnName()),
            new ImportColumn("kingdom", KINGDOM.getColumnName()),
            new ImportColumn("phylum", PHYLUM.getColumnName()),
            new ImportColumn("taxon_order", TAXON_ORDER.getColumnName()),
            new ImportColumn("taxon_class", TAXON_CLASS.getColumnName()),
            new ImportColumn("sub_order", SUB_ORDER.getColumnName()),
            new ImportColumn("taxon_remarks", TAXON_REMARKS.getColumnName())
    );

    private final CollectionJPARepository collectionJPARepository;
    private final SpecimenJPARepository specimenJPARepository;
    private final MidsService midsService;
    private final SpecimenIntegrationService specimenIntegrationService;
    private final SpecimenMapper specimenMapper;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public ImportSpecimenProcessorResult handleFile(File file, String userName, Map<UUID, Map<String, UUID>> collectionCache, UUID institutionId,
                                                    ImportModeEnum mode, UUID importId) throws Exception {
        long begin = System.nanoTime();
        List<String[]> lines;
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            lines = importHelper.extractData(inputStream);
        }
        if (log.isInfoEnabled()) {
            log.info("Traitement du fichier : {}", file.getName());
            log.info("Nombre de lignes à traiter : {}", lines.size() - 1);
        }
        int addedSpecimenCount = 0;
        int createdIdentification = 0;
        int createdGeologicalContext = 0;
        int createdTaxon = 0;
        int createdCollectionEvent = 0;

        int updatedSpecimenCount = 0;
        int updatedGeologicalContext = 0;
        int updatedCollectionEvent = 0;

        var columnNames = lines.get(0);
        var columnNamesMap = importHelper.buildColumnNamesMap(columnNames);

        var specimenFieldsForUpdate = getSpecimenColumns(columnNamesMap, false);
        var specimenFieldsForInsert = getSpecimenColumns(columnNamesMap, true);
        var specimenParametersForUpdate = getSpecimenParameters(columnNamesMap, false);
        var specimenParametersForInsert = getSpecimenParameters(columnNamesMap, true);

        var collectionEventFields = getCollectionEventColumns(columnNamesMap);
        var collectionEventParameters = getCollectionEventParameters(columnNamesMap);

        var geologicalContextFields = getGeologicalContextColumns(columnNamesMap);
        var geologicalContextParameters = getGeologicalContextParameters(columnNamesMap);

        var identificationFields = getIdentificationColumns(columnNamesMap);
        var identificationParameters = getIdentificationParameters(columnNamesMap);

        var taxonFields = getTaxonColumns(columnNamesMap);
        var taxonParameters = getTaxonParameters(columnNamesMap);

        specimenFieldsForInsert.add("fk_colevent_id");
        specimenParametersForInsert.add(":fk_colevent_id");

        specimenFieldsForInsert.add("fk_geo_id");
        specimenParametersForInsert.add(":fk_geo_id");

        var i = 0;
        for (String[] line : lines) {
            if (i == 0) {
                i++;
                continue;
            }

            String catalogNumber = line[columnNamesMap.get(CATALOG_NUMBER.getColumnName())];
            String collectionName = line[columnNamesMap.get(COLLECTION_NAME.getColumnName())];

            var specimens = specimenJPARepository.findSpecimens(institutionId, collectionName, catalogNumber);

            if (specimens.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Création d'un spécimen avec les données : {}", (Object) line);
                }
                UUID collectionEventId = null;
                UUID geologicalContextId = null;

                if (!collectionEventFields.isEmpty()) {
                    collectionEventId = createCollectionEvent(line, collectionEventFields, collectionEventParameters, columnNamesMap);
                    createdCollectionEvent++;
                }

                if (!geologicalContextFields.isEmpty()) {
                    geologicalContextId = createGeologicalContext(line, geologicalContextFields, geologicalContextParameters, columnNamesMap);
                    createdGeologicalContext++;
                }

                UUID collectionId = getCollectionIdFromLine(line, collectionCache, institutionId, columnNamesMap);
                if (collectionId == null) {
                    throw new RuntimeException("Collection non trouvée pour %s, %s, %s".formatted(institutionId, catalogNumber, collectionName));
                }
                var query = em.createNativeQuery(format("insert into specimen(%s) values (%s) returning id", String.join(", ", specimenFieldsForInsert), String.join(", ", specimenParametersForInsert)));

                Specimen specimen = buildSpecimenFromLine(line, columnNamesMap);
                MidsDTO mids = midsService.processMids(specimen);

                setParametersForInsert(query, line, userName, columnNamesMap, collectionEventId, geologicalContextId, collectionId, mids.level());
                UUID specimenId = (UUID) query.getSingleResult();
                addedSpecimenCount++;

                var insertUpdateQuery = em.createNativeQuery("insert into specimen_update(fk_specimen_id, fk_import_id, mode) values (:fk_specimen_id, :fk_import_id, :mode)");
                insertUpdateQuery.setParameter("fk_specimen_id", specimenId);
                insertUpdateQuery.setParameter("fk_import_id", importId);
                insertUpdateQuery.setParameter("mode", SpecimenUpdateModeEnum.CREATED.name());
                insertUpdateQuery.executeUpdate();

                if (!identificationFields.isEmpty() || !taxonFields.isEmpty()) {
                    var identificationId = createIdentification(line, columnNamesMap, specimenId, identificationFields, identificationParameters);
                    createdIdentification++;

                    if (!taxonFields.isEmpty()) {
                        createTaxon(line, columnNamesMap, identificationId, taxonFields, taxonParameters);
                        createdTaxon++;
                    }
                }
            } else if (specimens.size() == 1) {
                if (mode.equals(ImportModeEnum.IGNORE)) {
                    // Mode = IGNORE, on ne traite pas le specimen
                    continue;
                } else if (mode.equals(ImportModeEnum.REPLACE)) {
                    var specimen = specimens.get(0);

                    if (log.isInfoEnabled()) {
                        log.info("Mise à jour d'un spécimen avec les données : {}", (Object) line);
                    }

                    List<String> valuesToUpdate = buildValuesToUpdate(specimenFieldsForUpdate, specimenParametersForUpdate);

                    var queryString = format("update specimen set %s where id = '%s'", String.join(", ", valuesToUpdate), specimen.getId());
                    var query = em.createNativeQuery(queryString);

                    MidsDTO mids = midsService.processMids(specimenMapper.mapJpaToSpecimen(specimen));

                    setParametersForUpdate(query, line, columnNamesMap, specimenFieldsForUpdate, mids.level());

                    var updated = query.executeUpdate();
                    updatedSpecimenCount += updated;

                    SpecimenJPA updatedSpecimen = em.find(SpecimenJPA.class, specimen.getId());
                    em.refresh(updatedSpecimen);
                    specimenIntegrationService.saveSpecimenJPAAndUpdateMids(updatedSpecimen);

                    var insertUpdateQuery = em.createNativeQuery("insert into specimen_update(fk_specimen_id, fk_import_id, mode) values (:fk_specimen_id, :fk_import_id, :mode)");
                    insertUpdateQuery.setParameter("fk_specimen_id", specimen.getId());
                    insertUpdateQuery.setParameter("fk_import_id", importId);
                    insertUpdateQuery.setParameter("mode", SpecimenUpdateModeEnum.UPDATED.name());
                    insertUpdateQuery.executeUpdate();

                    if (specimen.getCollectionEvent() == null) {
                        addCollectionEvent(line, collectionEventFields, collectionEventParameters, columnNamesMap, specimen);
                        createdCollectionEvent++;
                    } else if (!collectionEventFields.isEmpty()) {
                        updateCollectionEvent(specimen.getCollectionEvent().getId(), line, columnNamesMap, collectionEventFields, collectionEventParameters);
                        updatedCollectionEvent++;
                    }

                    if (specimen.getGeologicalContext() == null) {
                        addGeologicalContext(line, geologicalContextFields, geologicalContextParameters, columnNamesMap, specimen);
                        createdGeologicalContext++;
                    } else if (!geologicalContextFields.isEmpty()) {
                        updateGeologicalContext(specimen.getGeologicalContext()
                                .getId(), line, columnNamesMap, geologicalContextFields, geologicalContextParameters);
                        updatedGeologicalContext++;
                    }

                    // Pas de mise à jour des tables identification et taxon
                }
            } else {
                if (log.isErrorEnabled()) {
                    log.error("Spécimen en doublons [institutionId={}, catalogNumber={}, collectionName={}] : {} spécimens trouvés", institutionId, catalogNumber, collectionName, specimens.size());
                }
                throw new Exception("Specimen en doublon");
            }
            i++;
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - begin;
        if (log.isInfoEnabled()) {
            log.info("Lignes traitées en : {} ms", timeElapsed / 1_000_000);
            log.info("Fin du traitement du fichier : {}", file.getName());
            log.info("inserted into specimen : {}", addedSpecimenCount);
            log.info("inserted into identification  : {}", createdIdentification);
            log.info("inserted into geological_context : {}", createdGeologicalContext);
            log.info("inserted into taxon : {}", createdTaxon);
            log.info("inserted into collection_event : {}", createdCollectionEvent);
            log.info("total insert : {}", addedSpecimenCount + createdIdentification + createdGeologicalContext + createdTaxon + createdCollectionEvent);
            log.info("updated specimen : {}", updatedSpecimenCount);
            log.info("updated geological_context : {}", updatedGeologicalContext);
            log.info("updated collection_event : {}", updatedCollectionEvent);
            log.info("total updated : {}", updatedSpecimenCount + updatedGeologicalContext + updatedCollectionEvent);
        }

        return new ImportSpecimenProcessorResult(addedSpecimenCount, updatedSpecimenCount);
    }

    private Specimen buildSpecimenFromLine(String[] line, Map<String, Integer> columnNamesMap) {
        String catalogNumber = importHelper.getValueFromCell(line, columnNamesMap, CATALOG_NUMBER.getColumnName());
        String collectionName = importHelper.getValueFromCell(line, columnNamesMap, COLLECTION_NAME.getColumnName());
        String scientificName = importHelper.getValueFromCell(line, columnNamesMap, SCIENTIFIC_NAME.getColumnName());
        String verbatimLocality = importHelper.getValueFromCell(line, columnNamesMap, VERBATIM_LOCALITY.getColumnName());
        String locality = importHelper.getValueFromCell(line, columnNamesMap, LOCALITY.getColumnName());
        String county = importHelper.getValueFromCell(line, columnNamesMap, COUNTY.getColumnName());
        String municipality = importHelper.getValueFromCell(line, columnNamesMap, MUNICIPALITY.getColumnName());
        String island = importHelper.getValueFromCell(line, columnNamesMap, ISLAND.getColumnName());
        String continent = importHelper.getValueFromCell(line, columnNamesMap, CONTINENT.getColumnName());
        String country = importHelper.getValueFromCell(line, columnNamesMap, COUNTRY.getColumnName());
        String countryCode = importHelper.getValueFromCell(line, columnNamesMap, COUNTRY_CODE.getColumnName());
        String islandGroup = importHelper.getValueFromCell(line, columnNamesMap, ISLAND_GROUP.getColumnName());
        String stateProvince = importHelper.getValueFromCell(line, columnNamesMap, STATE_PROVINCE.getColumnName());
        String waterBody = importHelper.getValueFromCell(line, columnNamesMap, WATER_BODY.getColumnName());
        String latitude = importHelper.getValueFromCell(line, columnNamesMap, DECIMAL_LATITUDE.getColumnName());
        String longitude = importHelper.getValueFromCell(line, columnNamesMap, DECIMAL_LONGITUDE.getColumnName());
        String recordedBy = importHelper.getValueFromCell(line, columnNamesMap, RECORDED_BY.getColumnName());
        String eventDate = importHelper.extractIntervalValue(line, columnNamesMap, "COLLECTE");
        String fieldNumber = importHelper.getValueFromCell(line, columnNamesMap, FIELD_NUMBER.getColumnName());

        return Specimen.builder()
                .catalogNumber(catalogNumber)
                .collectionId(collectionName == null ? null : new UUID(0, 1)) // Pas forcément pertinent de mettre une vraie valeur sachant qu'on teste juste si la valeur est non vide
                .institutionId("never empty") // Pas forcément pertinent de mettre une vraie valeur sachant qu'on teste juste si la valeur est non vide
                .identifications(
                        Set.of(
                                Identification.builder()
                                        .taxon(
                                                List.of(
                                                        Taxon.builder().scientificName(scientificName).build()
                                                )
                                        )
                                        .build()
                        )
                )
                .collectionEvent(
                        CollectionEvent.builder()
                                .verbatimLocality(verbatimLocality)
                                .location(
                                        Location.builder()
                                                .locality(locality)
                                                .county(county)
                                                .municipality(municipality)
                                                .island(island)
                                                .continent(continent)
                                                .country(country)
                                                .countryCode(countryCode)
                                                .islandGroup(islandGroup)
                                                .stateProvince(stateProvince)
                                                .waterBody(waterBody)
                                                .build()
                                )
                                .decimalLatitude(StringUtils.isNotBlank(latitude) ? Double.valueOf(latitude) : null)
                                .decimalLongitude(StringUtils.isNotBlank(longitude) ? Double.valueOf(longitude) : null)
                                .recordedBy(recordedBy)
                                .eventDate(eventDate)
                                .fieldNumber(fieldNumber)
                                .build()
                )
                .build();
    }

    private void updateGeologicalContext(UUID id, String[] line, Map<String, Integer> columnNamesMap, List<String> geologicalContextFields,
                                         List<String> geologicalContextParameters) {
        if (!geologicalContextFields.isEmpty()) {
            var valuesToUpdate = buildValuesToUpdate(geologicalContextFields, geologicalContextParameters);
            var queryString = format("update geological_context set %s where id = '%s'", String.join(", ", valuesToUpdate), id);
            if (log.isInfoEnabled()) {
                log.info("Requete à exec : {}", queryString);
            }
            var query = em.createNativeQuery(queryString);

            geologicalContextColumns.forEach(field -> {
                var exists = geologicalContextFields.contains(field.dbFieldName());
                if (exists) {
                    importHelper.setParameter(line, columnNamesMap, query, field);
                }
            });

            query.executeUpdate();
        }
    }

    private void addGeologicalContext(String[] line, List<String> geologicalContextFields, List<String> geologicalContextParameters,
                                      Map<String, Integer> columnNamesMap, SpecimenJPA specimen) {
        if (!geologicalContextFields.isEmpty()) {
            var geoId = createGeologicalContext(line, geologicalContextFields, geologicalContextParameters, columnNamesMap);
            var q = em.createNativeQuery("update specimen set fk_geo_id = :geoId where id = :id");
            q.setParameter("geoId", geoId);
            q.setParameter("id", specimen.getId());
            q.executeUpdate();
        }
    }

    private void addCollectionEvent(String[] line, List<String> collectionEventFields, List<String> collectionEventParameters,
                                    Map<String, Integer> columnNamesMap,
                                    SpecimenJPA specimen) {
        if (!collectionEventFields.isEmpty()) {
            var collectionEventId = createCollectionEvent(line, collectionEventFields, collectionEventParameters, columnNamesMap);
            var q = em.createNativeQuery("update specimen set fk_colevent_id = :coleventId where id = :id");
            q.setParameter("coleventId", collectionEventId);
            q.setParameter("id", specimen.getId());
            q.executeUpdate();
        }
    }

    private List<String> buildValuesToUpdate(List<String> specimenFieldsForUpdate, List<String> specimenParametersForUpdate) {
        List<String> valuesToUpdate = new ArrayList<>();
        for (int i = 0; i < specimenFieldsForUpdate.size(); i++) {
            var field = specimenFieldsForUpdate.get(i);
            var parameter = specimenParametersForUpdate.get(i);

            valuesToUpdate.add("%s=%s".formatted(field, parameter));
        }
        return valuesToUpdate;
    }

    private void setParametersForUpdate(Query query, String[] line, Map<String, Integer> columnNamesMap,
                                        List<String> specimenFieldsForUpdate, Integer mids) {
        query.setParameter("mids", mids);
        specimenColumns.forEach(field -> {
            var exists = specimenFieldsForUpdate.contains(field.dbFieldName());
            if (exists) {
                importHelper.setParameter(line, columnNamesMap, query, field);
            }
        });
    }


    private List<String> getTaxonParameters(Map<String, Integer> columnNamesMap) {
        var parameters = new ArrayList<String>();
        taxonColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                parameters.add(":" + field.dbFieldName());
            }
        });
        return parameters;
    }

    private List<String> getTaxonColumns(Map<String, Integer> columnNamesMap) {
        var fields = new ArrayList<String>();
        taxonColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                fields.add(field.dbFieldName());
            }
        });
        return fields;
    }

    private List<String> getIdentificationParameters(Map<String, Integer> columnNamesMap) {
        var parameters = new ArrayList<String>();
        identificationColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                parameters.add(":" + field.dbFieldName());
            }
        });
        return parameters;
    }

    private List<String> getIdentificationColumns(Map<String, Integer> columnNamesMap) {
        var fields = new ArrayList<String>();
        identificationColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                fields.add(field.dbFieldName());
            }
        });
        return fields;
    }

    private List<String> getGeologicalContextParameters(Map<String, Integer> columnNamesMap) {
        var parameters = new ArrayList<String>();
        geologicalContextColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                parameters.add(":" + field.dbFieldName());
            }
        });
        return parameters;
    }

    private List<String> getGeologicalContextColumns(Map<String, Integer> columnNamesMap) {
        var fields = new ArrayList<String>();
        geologicalContextColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                fields.add(field.dbFieldName());
            }
        });
        return fields;
    }

    private List<String> getCollectionEventParameters(Map<String, Integer> columnNamesMap) {
        var parameters = new ArrayList<String>();
        collectionEventColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                parameters.add(":" + field.dbFieldName());
            }
        });
        return parameters;
    }

    private List<String> getCollectionEventColumns(Map<String, Integer> columnNamesMap) {
        var fields = new ArrayList<String>();
        collectionEventColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                fields.add(field.dbFieldName());
            }
        });
        return fields;
    }

    private void createTaxon(String[] line, Map<String, Integer> columnNamesMap, UUID identificationId, List<String> fields, List<String> parameters) {
        String queryString = format("insert into taxon(id, fk_id_identification, level_type, %s) values (:id, :fk_id_identification, :level_type, %s) returning id", String.join(", ", fields), String.join(", ", parameters));
        var query = em.createNativeQuery(queryString);

        setTaxonParameters(line, fields, columnNamesMap, query, identificationId);

        try {
            query.getSingleResult();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Erreur lors de la création d'un taxon", e);
            }
        }
    }

    private void setTaxonParameters(String[] line, List<String> fields, Map<String, Integer> columnNamesMap, Query query, UUID identificationId) {
        query.setParameter("id", UUID.randomUUID());
        query.setParameter("fk_id_identification", identificationId);
        query.setParameter("level_type", "MASTER");

        taxonColumns.forEach(field -> {
            var exists = fields.contains(field.dbFieldName());
            if (exists) {
                importHelper.setParameter(line, columnNamesMap, query, field);
            }
        });
    }

    private UUID createIdentification(String[] line, Map<String, Integer> columnNamesMap, UUID specimenId, List<String> fields, List<String> parameters) {
        String additionnalFields = fields.isEmpty() ? "" : ", " + String.join(", ", fields);
        String additionalParameters = fields.isEmpty() ? "" : ", " + String.join(", ", parameters);
        String queryString = format("insert into identification(id, fk_id_specimen, current_determination %s) values (:id, :fk_id_specimen, :current_determination %s) returning id", additionnalFields, additionalParameters);
        var query = em.createNativeQuery(queryString);

        setIdentificationParameters(line, fields, columnNamesMap, query, specimenId);

        try {
            return (UUID) query.getSingleResult();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Erreur lors de la création d'une identification", e);
            }
            return null;
        }
    }

    private void setIdentificationParameters(String[] line, List<String> fields, Map<String, Integer> columnNamesMap, Query query, UUID specimenId) {
        query.setParameter("id", UUID.randomUUID());
        query.setParameter("fk_id_specimen", specimenId);
        query.setParameter("current_determination", true);

        identificationColumns.forEach(field -> {
            var exists = fields.contains(field.dbFieldName());
            if (exists) {
                importHelper.setParameter(line, columnNamesMap, query, field);
            }
        });
    }

    private UUID createGeologicalContext(String[] line, List<String> fields, List<String> parameters, Map<String, Integer> columnNamesMap) {
        var query = em.createNativeQuery(format("insert into geological_context(id, %s) values (:id, %s) returning id", String.join(", ", fields), String.join(", ", parameters)));

        setGeologicalContextParameters(line, fields, columnNamesMap, query);

        try {
            return (UUID) query.getSingleResult();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Erreur lors de la création d'un collevent", e);
            }
            return null;
        }
    }

    private UUID createCollectionEvent(String[] line, List<String> fields, List<String> parameters, Map<String, Integer> columnNamesMap) {
        var query = em.createNativeQuery(format("insert into collection_event(id, %s) values (:id, %s) returning id", String.join(", ", fields), String.join(", ", parameters)));

        setCollectionEventParameters(line, fields, columnNamesMap, query);

        try {
            return (UUID) query.getSingleResult();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Erreur lors de la création d'un collevent", e);
            }
            return null;
        }
    }

    private void updateCollectionEvent(UUID id, String[] line, Map<String, Integer> columnNamesMap, List<String> collectionEventFields,
                                       List<String> collectionEventParameters) {
        if (!collectionEventFields.isEmpty()) {
            var valuesToUpdate = buildValuesToUpdate(collectionEventFields, collectionEventParameters);
            var queryString = format("update collection_event set %s where id = '%s'", String.join(", ", valuesToUpdate), id);
            var query = em.createNativeQuery(queryString);

            collectionEventColumns.forEach(field -> {
                var exists = collectionEventFields.contains(field.dbFieldName());
                if (exists) {
                    importHelper.setParameter(line, columnNamesMap, query, field);
                }
            });

            query.executeUpdate();
        }
    }

    private void setGeologicalContextParameters(String[] line, List<String> fields, Map<String, Integer> columnNamesMap, Query query) {
        query.setParameter("id", UUID.randomUUID());

        geologicalContextColumns.forEach(field -> {
            var exists = fields.contains(field.dbFieldName());
            if (exists) {
                importHelper.setParameter(line, columnNamesMap, query, field);
            }
        });
    }

    private void setCollectionEventParameters(String[] line, List<String> fields, Map<String, Integer> columnNamesMap, Query query) {
        query.setParameter("id", UUID.randomUUID());

        collectionEventColumns.forEach(field -> {
            var exists = fields.contains(field.dbFieldName());
            if (exists) {
                importHelper.setParameter(line, columnNamesMap, query, field);
            }
        });
    }


    private List<String> getSpecimenColumns(Map<String, Integer> columnNamesMap, boolean forInsert) {
        List<String> fields = new ArrayList<>();

        if (forInsert) {
            fields.add("id");
            fields.add("state");
            fields.add("fk_id_collection");
            fields.add("created_at");
            fields.add("created_by");
        }
        fields.add("mids");

        specimenColumns.forEach(field -> {
            var exists = columnNamesMap.containsKey(field.columnName());
            if (exists) {
                fields.add(field.dbFieldName());
            }
        });

        return fields;
    }

    private List<String> getSpecimenParameters(Map<String, Integer> columnNamesMap, boolean forInsert) {
        List<String> parameters = new ArrayList<>();

        if (forInsert) {
            parameters.add(":id");
            parameters.add(":state");
            parameters.add(":collectionId");
            parameters.add(":createdAt");
            parameters.add(":createdBy");
        }

        parameters.add(":mids");

        specimenColumns.forEach(field -> {
            var exists = columnNamesMap.containsKey(field.columnName());
            if (exists) {
                parameters.add(":" + field.dbFieldName());
            }
        });

        return parameters;
    }

    private void setParametersForInsert(Query query, String[] line, String userName, Map<String, Integer> columnNamesMap, UUID collectionEventId,
                                        UUID geologicalContextId, UUID collectionId, Integer mids) {
        query.setParameter("id", UUID.randomUUID());
        query.setParameter("state", SpecimenStatusEnum.VALID.name());
        query.setParameter("collectionId", collectionId);
        query.setParameter("createdAt", LocalDateTime.now());
        query.setParameter("createdBy", userName);
        query.setParameter("mids", mids);

        query.setParameter("fk_colevent_id", collectionEventId);
        query.setParameter("fk_geo_id", geologicalContextId);

        specimenColumns.forEach(field -> {
            var exists = columnNamesMap.containsKey(field.columnName());
            if (exists) {
                importHelper.setParameter(line, columnNamesMap, query, field);
            }
        });
    }

    /**
     * Récupère l'identifiant de la collection d'une ligne.
     *
     * @param line            ligne à traiter
     * @param collectionCache map stockant les collections déjà recherchées
     * @param institutionId   identifiant de l'institution concernée par l'import
     * @param columnNamesMap  map contenant les colonnes du fichier
     * @return un UUID
     */
    private UUID getCollectionIdFromLine(String[] line, Map<UUID, Map<String, UUID>> collectionCache, UUID institutionId, Map<String, Integer> columnNamesMap) {
        var collectionName = line[columnNamesMap.get(COLLECTION_NAME.getColumnName())];
        if (collectionCache.get(institutionId).containsKey(collectionName)) {
            return collectionCache.get(institutionId).get(collectionName);
        } else {
            UUID collectionId = collectionJPARepository.findCollectionIdByInstitutionIdAndCollectionName(institutionId, collectionName);
            collectionCache.get(institutionId).put(collectionName, collectionId);
            return collectionId;
        }
    }
}

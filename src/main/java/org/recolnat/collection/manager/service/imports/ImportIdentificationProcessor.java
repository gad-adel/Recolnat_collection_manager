package org.recolnat.collection.manager.service.imports;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.ImportColumn;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum.BOOLEAN;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum.DATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum.REQUIRED_BOOLEAN;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportIdentificationColumnEnum.CURRENT_DETERMINATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.FAMILY;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.GENUS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.IDENTIFICATION_REMARKS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.IDENTIFICATION_VERIFICATION_STATUS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.IDENTIFIED_BYID;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.INFRASPECIFIC_EPITHET;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.KINGDOM;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.PHYLUM;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.SCIENTIFIC_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.SCIENTIFIC_NAME_AUTHORSHIP;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.SPECIFIC_EPITHET;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.SUB_FAMILY;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.SUB_GENUS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.SUB_ORDER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.TAXON_CLASS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.TAXON_ORDER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.TAXON_REMARKS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.TYPE_STATUS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.VERBATIM_IDENTIFICATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.VERNACULAR_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportIdentificationProcessor {

    private final List<ImportColumn> identificationColumns = List.of(
            new ImportColumn("verbatim_identification", VERBATIM_IDENTIFICATION.getColumnName()),
            new ImportColumn("identification_verification_status", IDENTIFICATION_VERIFICATION_STATUS.getColumnName(), BOOLEAN),
            new ImportColumn("identification_remarks", IDENTIFICATION_REMARKS.getColumnName()),
            new ImportColumn("type_status", TYPE_STATUS.getColumnName()),
            new ImportColumn("identified_byid", IDENTIFIED_BYID.getColumnName()),
            new ImportColumn("date_identified", "DETERMINATION", DATE),
            new ImportColumn("current_determination", CURRENT_DETERMINATION.getColumnName(), REQUIRED_BOOLEAN)
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

    private final ImportHelper importHelper;

    private final SpecimenJPARepository specimenJPARepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public long handleFile(File file, UUID institutionId) throws Exception {
        long begin = System.nanoTime();
        List<String[]> lines;
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            lines = importHelper.extractData(inputStream);
        }

        if (log.isInfoEnabled()) {
            log.info("Traitement du fichier : {}", file.getName());
            log.info("Nombre de lignes à traiter : {}", lines.size() - 1);
        }
        int createdIdentification = 0;
        int createdTaxon = 0;

        var columnNames = lines.get(0);
        var columnNamesMap = importHelper.buildColumnNamesMap(columnNames);

        var identificationFields = getIdentificationColumns(columnNamesMap);
        var identificationParameters = getIdentificationParameters(columnNamesMap);

        var taxonFields = getTaxonColumns(columnNamesMap);
        var taxonParameters = getTaxonParameters(columnNamesMap);

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
                if (log.isErrorEnabled()) {
                    log.error("Specimen non existant [institutionId={}, catalogNumber={}, collectionName={}]", institutionId, catalogNumber, collectionName);
                }
                throw new Exception("Specimen non existant");
            } else if (specimens.size() == 1) {
                var specimenId = specimens.get(0).getId();
                boolean isCurrentDetermination = columnNamesMap.containsKey(CURRENT_DETERMINATION.getColumnName()) && importHelper.extractBooleanValue(line, columnNamesMap, CURRENT_DETERMINATION.getColumnName());
                // Si la détermination est flaggé courante on met à jour les autres déterminations du spécimen à current_determination = false
                if (isCurrentDetermination) {
                    var query = em.createNativeQuery("update identification i set current_determination = false where i.fk_id_specimen = :specimen_id");
                    query.setParameter("specimen_id", specimenId);
                    query.executeUpdate();
                }

                var identificationId = createIdentification(line, columnNamesMap, specimenId, identificationFields, identificationParameters);
                createdIdentification++;

                if (!taxonFields.isEmpty()) {
                    createTaxon(line, columnNamesMap, identificationId, taxonFields, taxonParameters);
                    createdTaxon++;
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
            log.info("inserted into identification : {}", createdIdentification);
            log.info("inserted into taxon : {}", createdTaxon);
        }

        return createdIdentification;
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

    private UUID createIdentification(String[] line, Map<String, Integer> columnNamesMap, UUID specimenId, List<String> fields, List<String> parameters) {
        String queryString = format("insert into identification(id, fk_id_specimen, %s) values (:id, :fk_id_specimen,%s) returning id", String.join(", ", fields), String.join(", ", parameters));
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

        identificationColumns.forEach(field -> {
            var exists = fields.contains(field.dbFieldName());
            if (exists) {
                importHelper.setParameter(line, columnNamesMap, query, field);
            }
        });
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
}

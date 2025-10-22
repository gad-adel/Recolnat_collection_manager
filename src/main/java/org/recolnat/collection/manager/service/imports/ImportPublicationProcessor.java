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
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum.YEAR;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.AUTHORS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.BOOK_TITLE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.CITATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.DATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.DESCRIPTION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.EDITORS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.IDENTIFIER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.KEYWORDS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.LANGUAGE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.PAGES;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.PAGE_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.PUBLICATION_PLACE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.PUBLISHER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.REMARKS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.REVIEW;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.TITLE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.URL;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.VOLUME;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportPublicationProcessor {

    private final List<ImportColumn> literatureColumns = List.of(
            new ImportColumn("identifier", IDENTIFIER.getColumnName()),
            new ImportColumn("url", URL.getColumnName()),
            new ImportColumn("citation", CITATION.getColumnName()),
            new ImportColumn("title", TITLE.getColumnName()),
            new ImportColumn("authors", AUTHORS.getColumnName()),
            new ImportColumn("date", DATE.getColumnName(), YEAR),
            new ImportColumn("language", LANGUAGE.getColumnName()),
            new ImportColumn("keywords", KEYWORDS.getColumnName()),
            new ImportColumn("description", DESCRIPTION.getColumnName()),
            new ImportColumn("remarks", REMARKS.getColumnName()),
            new ImportColumn("review", REVIEW.getColumnName()),
            new ImportColumn("volume", VOLUME.getColumnName()),
            new ImportColumn("number", NUMBER.getColumnName()),
            new ImportColumn("pages", PAGES.getColumnName()),
            new ImportColumn("book_title", BOOK_TITLE.getColumnName()),
            new ImportColumn("publisher", PUBLISHER.getColumnName()),
            new ImportColumn("publication_place", PUBLICATION_PLACE.getColumnName()),
            new ImportColumn("editors", EDITORS.getColumnName()),
            new ImportColumn("page_number", PAGE_NUMBER.getColumnName())
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

        log.info("Traitement du fichier : {}", file.getName());
        log.info("Nombre de lignes à traiter : {}", lines.size() - 1);
        int createdLiterature = 0;

        var columnNames = lines.get(0);
        var columnNamesMap = importHelper.buildColumnNamesMap(columnNames);

        var literatureFields = getIdentificationColumns(columnNamesMap);
        var literatureParameters = getIdentificationParameters(columnNamesMap);

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
                log.error("Specimen non existant [institutionId={}, catalogNumber={}, collectionName={}]", institutionId, catalogNumber, collectionName);
                throw new Exception("Specimen non existant");
            } else if (specimens.size() == 1) {
                var specimenId = specimens.get(0).getId();
                createLiterature(line, columnNamesMap, specimenId, literatureFields, literatureParameters);
                createdLiterature++;
            } else {
                log.error("Spécimen en doublons [institutionId={}, catalogNumber={}, collectionName={}] : {} spécimens trouvés", institutionId, catalogNumber, collectionName, specimens.size());
                throw new Exception("Specimen en doublon");
            }

            i++;
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - begin;
        log.info("Lignes traitées en : {} ms", timeElapsed / 1000000);

        log.info("Fin du traitement du fichier : {}", file.getName());
        log.info("inserted into literature : {}", createdLiterature);
        return createdLiterature;
    }

    private List<String> getIdentificationColumns(Map<String, Integer> columnNamesMap) {
        var fields = new ArrayList<String>();
        literatureColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                fields.add(field.dbFieldName());
            }
        });
        return fields;
    }

    private List<String> getIdentificationParameters(Map<String, Integer> columnNamesMap) {
        var parameters = new ArrayList<String>();
        literatureColumns.forEach(field -> {
            var exists = importHelper.existsInMap(columnNamesMap, field);
            if (exists) {
                parameters.add(":" + field.dbFieldName());
            }
        });
        return parameters;
    }

    private UUID createLiterature(String[] line, Map<String, Integer> columnNamesMap, UUID specimenId, List<String> fields, List<String> parameters) {
        String queryString = format("insert into literature(id, fk_id_specimen, %s) values (:id, :fk_id_specimen,%s) returning id", String.join(", ", fields), String.join(", ", parameters));
        var query = em.createNativeQuery(queryString);

        setLiteratureParameters(line, fields, columnNamesMap, query, specimenId);

        try {
            return (UUID) query.getSingleResult();
        } catch (Exception e) {
            log.error("Erreur lors de la création d'une literature", e);
            return null;
        }
    }

    private void setLiteratureParameters(String[] line, List<String> fields, Map<String, Integer> columnNamesMap, Query query, UUID specimenId) {
        query.setParameter("id", UUID.randomUUID());
        query.setParameter("fk_id_specimen", specimenId);

        literatureColumns.forEach(field -> {
            var exists = fields.contains(field.dbFieldName());
            if (exists) {
                importHelper.setParameter(line, columnNamesMap, query, field);
            }
        });
    }
}

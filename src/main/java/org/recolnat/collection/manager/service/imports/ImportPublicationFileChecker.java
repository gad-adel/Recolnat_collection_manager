package org.recolnat.collection.manager.service.imports;

import io.recolnat.model.ImportCheckPublicationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum;
import org.recolnat.collection.manager.api.domain.imports.ImportCheckSpecimen;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.InstitutionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.BLANK_LINE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.COLLECTION_NOT_EXISTS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_CITATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.SPECIMEN_NOT_EXISTS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.CITATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.DATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.COLLECTION_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportPublicationFileChecker {

    public static final Map<String, ImportFieldFormatEnum> FIELDS_TO_CHECK = Map.ofEntries(
            Map.entry(DATE.getColumnName(), ImportFieldFormatEnum.YEAR)
    );
    public static final String ACCESS_DENIED = "Access denied";
    private static final Map<String, Long> TIMES = new HashMap<>();


    private final ImportHelper importHelper;
    private final InstitutionService institutionService;
    private final ImportCommonFileChecker importCommonFileChecker;
    private final SpecimenJPARepository specimenJPARepository;

    private static void addTimes(String key, long timeElapsed) {
        TIMES.replace(key, TIMES.get(key) + timeElapsed);
    }

    private static void logTimes(long timeElapsed) {
        if (log.isInfoEnabled()) {
            log.info("checkFileData : {} ms", timeElapsed / 1_000_000);
            log.info("advancedChecks total : {} ms", TIMES.get("advancedChecks") / 1_000_000);
            log.info("\tblockingChecks total : {} ms", TIMES.get("blockingChecks") / 1_000_000);
            log.info("\t\tcheckUserRights total : {} ms", TIMES.get("checkUserRights") / 1_000_000);
            log.info("\t\tcheckIfCollectionExists total : {} ms", TIMES.get("checkIfCollectionExists") / 1_000_000);
            log.info("\t\tcheckRequiredValues total : {} ms", TIMES.get("checkRequiredValues") / 1_000_000);
            log.info("\t\tcheckFieldsFormat total : {} ms", TIMES.get("checkFieldsFormat") / 1_000_000);
            log.info("\t\tcheckDateField total : {} ms", TIMES.get("checkDateField") / 1_000_000);
            log.info("\tnonBlockingChecks total : {} ms", TIMES.get("nonBlockingChecks") / 1_000_000);
        }
    }

    private static void initTimes() {
        TIMES.put("blockingChecks", 0L);
        TIMES.put("nonBlockingChecks", 0L);
        TIMES.put("checkUserRights", 0L);
        TIMES.put("checkIfCollectionExists", 0L);
        TIMES.put("checkFieldsFormat", 0L);
        TIMES.put("checkRequiredValues", 0L);
        TIMES.put("advancedChecks", 0L);
        TIMES.put("checkDateField", 0L);
    }

    public ImportCheckPublicationResponseDTO checkFileData(MultipartFile determinationFile, UUID institutionId, ImportCheckSpecimen specimensChecks) {
        initTimes();
        long start = System.nanoTime();
        var response = new ImportCheckPublicationResponseDTO();

        checkUserRigthsOnInstitution(institutionId);

        var determinations = importHelper.extractDataWithOpenCsv(determinationFile);
        // On enlève la/les lignes qui ne sont pas des lignes de données
        response.setLines(determinations.size() - 1);

        Map<ImportErrorEnum, Integer[]> blockingErrors = new EnumMap<>(ImportErrorEnum.class);
        Map<ImportErrorEnum, Integer[]> nonBlockingErrors = new EnumMap<>(ImportErrorEnum.class);

        advancedChecks(determinations, response, institutionId, blockingErrors, nonBlockingErrors, specimensChecks);

        long finish = System.nanoTime();
        long timeElapsed = finish - start;

        logTimes(timeElapsed);
        return response;
    }

    /**
     * Vérification que l'utilisateur courant a bien les droits de faire un import sur une institution
     *
     * @param institutionId identifiant de l'institution à vérifier
     */
    private void checkUserRigthsOnInstitution(UUID institutionId) {
        if (institutionService.checkAccessToInstitution(institutionId)) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ACCESS_DENIED);
        }
    }

    private void advancedChecks(List<String[]> lines, ImportCheckPublicationResponseDTO response, UUID institutionId,
                                Map<ImportErrorEnum, Integer[]> blockingErrors,
                                Map<ImportErrorEnum, Integer[]> nonBlockingErrors, ImportCheckSpecimen specimensChecks) {
        long start = System.nanoTime();

        Map<String, Boolean> collectionsCache = new HashMap<>();
        Map<String, Boolean> rightsCache = new HashMap<>();

        var columnNames = lines.get(0);
        var columnNamesMap = importHelper.buildColumnNamesMap(columnNames);
        var specimenIdentifiers = specimensChecks != null ? specimensChecks.getIdentifiers() : new HashSet<String>();

        var i = 0;
        for (String[] line : lines) {
            if (i == 0) {
                i++;
                continue;
            }
            var values = Arrays.copyOf(line, columnNames.length);

            nonBlockingChecks(i, values, columnNamesMap, nonBlockingErrors);
            blockingChecks(institutionId, i, values, columnNamesMap, blockingErrors, collectionsCache, rightsCache, specimenIdentifiers);
            i++;
        }

        response.setBlockingErrors(importHelper.mapAsListOfErrors(blockingErrors));
        response.setNonBlockingErrors(importHelper.mapAsListOfErrors(nonBlockingErrors));
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes("advancedChecks", timeElapsed);
    }

    private void blockingChecks(UUID institutionId, int lineIndex, String[] values, Map<String, Integer> columnNames,
                                Map<ImportErrorEnum, Integer[]> blockingErrors,
                                Map<String, Boolean> collectionsCache, Map<String, Boolean> rightsCache, Set<String> specimenIdentifiers) {
        long start = System.nanoTime();
        // Ligne vides
        if (Arrays.stream(values).allMatch(StringUtils::isBlank)) {
            importHelper.addErrorInMap(BLANK_LINE, lineIndex, blockingErrors);
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            addTimes("blockingChecks", timeElapsed);
            return;
        }
        // Lignes avec informations obligatoires absentes
        checkRequiredValues(lineIndex, values, columnNames, blockingErrors);
        // Champs avec type non conforme (si numérique ou date)
        importCommonFileChecker.checkFieldsFormat(lineIndex, values, columnNames, blockingErrors, FIELDS_TO_CHECK, TIMES);
        // Collection inexistante dans Recolnat
        if (importCommonFileChecker.checkIfCollectionExists(values, columnNames, institutionId, collectionsCache, TIMES)) {
            // Utilisateur non autorisé à insérer des données dans cette collection
            importCommonFileChecker.checkUserRights(lineIndex, values, columnNames, blockingErrors, institutionId, rightsCache, TIMES);

            var collectionName = values[columnNames.get(COLLECTION_NAME.getColumnName())];
            var catalogNumber = values[columnNames.get(CATALOG_NUMBER.getColumnName())];

            String key = collectionName + "|" + catalogNumber;

            if (!specimenIdentifiers.contains(key) && !specimenJPARepository.countSpecimens(institutionId, collectionName, catalogNumber)) {
                importHelper.addErrorInMap(SPECIMEN_NOT_EXISTS, lineIndex, blockingErrors);
            }
        } else {
            importHelper.addErrorInMap(COLLECTION_NOT_EXISTS, lineIndex, blockingErrors);
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes("blockingChecks", timeElapsed);
    }

    public void checkRequiredValues(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors) {
        long start = System.nanoTime();

        boolean isBlankLine = Arrays.stream(values).allMatch(StringUtils::isBlank);

        if (isBlankLine) {
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            addTimes("checkRequiredValues", timeElapsed);
            return;
        }

        var value = values[columnNames.get(COLLECTION_NAME.getColumnName())];

        if (StringUtils.isBlank(value)) {
            importHelper.addErrorInMap(REQUIRED_COLLECTION_NAME, lineIndex, blockingErrors);
        }

        value = values[columnNames.get(CATALOG_NUMBER.getColumnName())];
        if (StringUtils.isBlank(value)) {
            importHelper.addErrorInMap(REQUIRED_CATALOG_NUMBER, lineIndex, blockingErrors);
        }

        value = values[columnNames.get(CITATION.getColumnName())];
        if (StringUtils.isBlank(value)) {
            importHelper.addErrorInMap(REQUIRED_CITATION, lineIndex, blockingErrors);
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes("checkRequiredValues", timeElapsed);
    }

    private void nonBlockingChecks(int lineIndex, String[] values, Map<String, Integer> columnNames,
                                   Map<ImportErrorEnum, Integer[]> nonBlockingErrors) {
        long start = System.nanoTime();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes("nonBlockingChecks", timeElapsed);
        // TODO DTH valeur saisie ne correspond pas à une valeur du référentiel (cas des listes fermées)
    }
}

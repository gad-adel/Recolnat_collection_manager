package org.recolnat.collection.manager.service.imports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recolnat.model.ImportCheckSpecimenResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum;
import org.recolnat.collection.manager.api.domain.imports.ImportCheckSpecimen;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.util.DateUtil;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.InstitutionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.BLANK_LINE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.COLLECTION_DATE_NOT_SAME_FORMAT;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.COLLECTION_NOT_EXISTS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.DUPLICATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.INCORRECT_COLLECTION_DATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.INCORRECT_DEPTH;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.NEGATIVE_DEPTH;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.DECIMAL_LATITUDE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.DECIMAL_LONGITUDE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.MAXIMUM_DEPTH_IN_METERS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.MAXIMUM_ELEVATION_IN_METERS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.MINIMUM_DEPTH_IN_METERS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.MINIMUM_ELEVATION_IN_METERS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportSpecimenFileChecker {

    public static final Map<String, ImportFieldFormatEnum> FIELDS_TO_CHECK = Map.ofEntries(
            Map.entry("COLLECTE_DEBUT", ImportFieldFormatEnum.DATE_AS_STRING),
            Map.entry("COLLECTE_FIN", ImportFieldFormatEnum.DATE_AS_STRING),
            Map.entry(DECIMAL_LATITUDE.getColumnName(), ImportFieldFormatEnum.DOUBLE),
            Map.entry(DECIMAL_LONGITUDE.getColumnName(), ImportFieldFormatEnum.DOUBLE),
            Map.entry(MINIMUM_ELEVATION_IN_METERS.getColumnName(), ImportFieldFormatEnum.DOUBLE),
            Map.entry(MAXIMUM_ELEVATION_IN_METERS.getColumnName(), ImportFieldFormatEnum.DOUBLE),
            Map.entry(MINIMUM_DEPTH_IN_METERS.getColumnName(), ImportFieldFormatEnum.DOUBLE),
            Map.entry(MAXIMUM_DEPTH_IN_METERS.getColumnName(), ImportFieldFormatEnum.DOUBLE),
            Map.entry("DETERMINATION", ImportFieldFormatEnum.DATE_AS_STRING)
    );
    public static final String ACCESS_DENIED = "Access denied";
    private static final int BATCH_SIZE = 1000;
    private static final Map<String, Long> TIMES = new HashMap<>();

    private final SpecimenJPARepository specimenJPARepository;

    private final ImportHelper importHelper;
    private final InstitutionService institutionService;
    private final ImportCommonFileChecker importCommonFileChecker;


    private static void addTimes(String key, long timeElapsed) {
        TIMES.replace(key, TIMES.get(key) + timeElapsed);
    }

    private static void logTimes(long timeElapsed) {
        if (log.isInfoEnabled()) {
            log.info("checkFileData : {} ms", timeElapsed / 1_000_000);
            log.info("advancedChecks total : {} ms", TIMES.get("advancedChecks") / 1_000_000);
            log.info("\tblockingChecks total : {} ms", TIMES.get("blockingChecks") / 1_000_000);
            log.info("\t\tcheckDuplicates total : {} ms", TIMES.get("checkDuplicates") / 1_000_000);
            log.info("\t\tcheckUserRights total : {} ms", TIMES.get("checkUserRights") / 1_000_000);
            log.info("\t\tcheckIfCollectionExists total : {} ms", TIMES.get("checkIfCollectionExists") / 1_000_000);
            log.info("\t\tcheckRequiredValues total : {} ms", TIMES.get("checkRequiredValues") / 1_000_000);
            log.info("\t\tcheckFieldsFormat total : {} ms", TIMES.get("checkFieldsFormat") / 1_000_000);
            log.info("\t\tcheckDateCoherence total : {} ms", TIMES.get("checkDateCoherence") / 1_000_000);
            log.info("\t\tcheckElevationCoherence total : {} ms", TIMES.get("checkElevationCoherence") / 1_000_000);
            log.info("\t\tcheckDepthCoherence total : {} ms", TIMES.get("checkDepthCoherence") / 1_000_000);
            log.info("\t\tcheckDateField total : {} ms", TIMES.get("checkDateField") / 1_000_000);
            log.info("\tnonBlockingChecks total : {} ms", TIMES.get("nonBlockingChecks") / 1_000_000);
            log.info("\tisInRecolnat total : {} ms", TIMES.get("isInRecolnat") / 1_000_000);
        }
    }

    private static void initTimes() {
        TIMES.put("blockingChecks", 0L);
        TIMES.put("nonBlockingChecks", 0L);
        TIMES.put("isInRecolnat", 0L);
        TIMES.put("checkDuplicates", 0L);
        TIMES.put("checkUserRights", 0L);
        TIMES.put("checkIfCollectionExists", 0L);
        TIMES.put("checkFieldsFormat", 0L);
        TIMES.put("checkRequiredValues", 0L);
        TIMES.put("advancedChecks", 0L);
        TIMES.put("checkDateCoherence", 0L);
        TIMES.put("checkDateField", 0L);
        TIMES.put("checkDepthCoherence", 0L);
        TIMES.put("checkElevationCoherence", 0L);
    }

    public ImportCheckSpecimen checkFileData(MultipartFile specimenFile, UUID institutionId) {
        initTimes();

        long start = System.nanoTime();
        var response = new ImportCheckSpecimenResponseDTO();

        checkUserRigthsOnInstitution(institutionId);

        var specimens = importHelper.extractDataWithOpenCsv(specimenFile);
        // On enlève la/les lignes qui ne sont pas des lignes de données
        response.setLines(specimens.size() - 1);

        Map<ImportErrorEnum, Integer[]> blockingErrors = new EnumMap<>(ImportErrorEnum.class);
        Map<ImportErrorEnum, Integer[]> nonBlockingErrors = new EnumMap<>(ImportErrorEnum.class);
        Set<String> specimenIdentifiers = new HashSet<>();

        advancedChecks(specimens, response, institutionId, blockingErrors, nonBlockingErrors, specimenIdentifiers);

        long finish = System.nanoTime();
        long timeElapsed = finish - start;

        logTimes(timeElapsed);

        return ImportCheckSpecimen.builder().response(response).identifiers(specimenIdentifiers).build();
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

    private void advancedChecks(List<String[]> lines, ImportCheckSpecimenResponseDTO response, UUID institutionId,
                                Map<ImportErrorEnum, Integer[]> blockingErrors,
                                Map<ImportErrorEnum, Integer[]> nonBlockingErrors, Set<String> specimenIdentifiers) {
        long start = System.nanoTime();

        Map<String, Boolean> collectionsCache = new HashMap<>();
        Map<String, Boolean> rightsCache = new HashMap<>();
        int specimensInRecolnat = 0;

        var columnNames = lines.get(0);
        var columnNamesMap = importHelper.buildColumnNamesMap(columnNames);

        ObjectMapper objectMapper = new ObjectMapper();

        List<List<String>> identifiers = new ArrayList<>();

        var i = 0;
        for (String[] line : lines) {
            if (i == 0) {
                i++;
                continue;
            }
            var values = Arrays.copyOf(line, columnNames.length);

            nonBlockingChecks(i, values, columnNamesMap, nonBlockingErrors);
            blockingChecks(institutionId, i, values, columnNamesMap, blockingErrors, specimenIdentifiers, collectionsCache, rightsCache);

            if (blockingErrors.isEmpty()) {
                identifiers.add(List.of(values[columnNamesMap.get(COLLECTION_NAME.getColumnName())], values[columnNamesMap.get(CATALOG_NUMBER.getColumnName())]));
                // Quand la liste atteind la taille de 1000 élément on fait le test en base
                if (identifiers.size() == BATCH_SIZE) {
                    specimensInRecolnat += isInRecolnat(identifiers, institutionId, objectMapper);
                    identifiers.clear();
                }
            }
            i++;
        }

        // Si la liste des identifiants n'est pas vide on fait le test
        if (!identifiers.isEmpty()) {
            specimensInRecolnat += isInRecolnat(identifiers, institutionId, objectMapper);
        }

        response.setBlockingErrors(importHelper.mapAsListOfErrors(blockingErrors));
        response.setNonBlockingErrors(importHelper.mapAsListOfErrors(nonBlockingErrors));
        response.setSpecimensInRecolnat(specimensInRecolnat);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes("advancedChecks", timeElapsed);
    }

    private int isInRecolnat(List<List<String>> identifiers, UUID institutionId, ObjectMapper objectMapper) {
        long start = System.nanoTime();

        try {
            String jsonArray = objectMapper.writeValueAsString(identifiers);
            var count = specimenJPARepository.countSpecimen(institutionId, jsonArray);
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            addTimes("isInRecolnat", timeElapsed);
            return count;
        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la conversion des tuples en json");
            return 0;
        }
    }

    private void blockingChecks(UUID institutionId, int lineIndex, String[] values, Map<String, Integer> columnNames,
                                Map<ImportErrorEnum, Integer[]> blockingErrors,
                                Set<String> specimenIdentifiers, Map<String, Boolean> collectionsCache, Map<String, Boolean> rightsCache) {
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
        importCommonFileChecker.checkRequiredValues(lineIndex, values, columnNames, blockingErrors, TIMES);
        // Champs avec type non conforme (si numérique ou date)
        importCommonFileChecker.checkFieldsFormat(lineIndex, values, columnNames, blockingErrors, FIELDS_TO_CHECK, TIMES);
        // Collection inexistante dans Recolnat
        if (importCommonFileChecker.checkIfCollectionExists(values, columnNames, institutionId, collectionsCache, TIMES)) {
            // Utilisateur non autorisé à insérer des données dans cette collection
            importCommonFileChecker.checkUserRights(lineIndex, values, columnNames, blockingErrors, institutionId, rightsCache, TIMES);
        } else {
            importHelper.addErrorInMap(COLLECTION_NOT_EXISTS, lineIndex, blockingErrors);
        }

        checkDateCoherence(lineIndex, values, columnNames, blockingErrors);
        checkElevationCoherence(lineIndex, values, columnNames, blockingErrors);
        checkDepthCoherence(lineIndex, values, columnNames, blockingErrors);

        // Doublons spécimens détectés (combinaison collection + numéro inventaire)
        if (checkDuplicates(values, columnNames, specimenIdentifiers)) {
            importHelper.addErrorInMap(DUPLICATE, lineIndex, blockingErrors);
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes("blockingChecks", timeElapsed);
    }

    private void checkDepthCoherence(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors) {
        long begin = System.nanoTime();

        var indexMin = columnNames.get(MINIMUM_DEPTH_IN_METERS.getColumnName());
        var indexMax = columnNames.get(MAXIMUM_DEPTH_IN_METERS.getColumnName());

        checkMinMaxValues(lineIndex, values, blockingErrors, indexMin, indexMax, INCORRECT_DEPTH, true, NEGATIVE_DEPTH);

        long finish = System.nanoTime();
        long timeElapsed = finish - begin;
        addTimes("checkDepthCoherence", timeElapsed);
    }

    private void checkElevationCoherence(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors) {
        long begin = System.nanoTime();

        var indexMin = columnNames.get(MINIMUM_ELEVATION_IN_METERS.getColumnName());
        var indexMax = columnNames.get(MAXIMUM_ELEVATION_IN_METERS.getColumnName());

        checkElevationMinMaxValues(lineIndex, values, blockingErrors, indexMin, indexMax);

        long finish = System.nanoTime();
        long timeElapsed = finish - begin;
        addTimes("checkElevationCoherence", timeElapsed);
    }

    private void checkElevationMinMaxValues(int lineIndex, String[] values, Map<ImportErrorEnum, Integer[]> blockingErrors, Integer indexMin,
                                            Integer indexMax) {
        checkMinMaxValues(lineIndex, values, blockingErrors, indexMin, indexMax, ImportErrorEnum.INCORRECT_ELEVATION, false, null);
    }

    private void checkMinMaxValues(int lineIndex, String[] values, Map<ImportErrorEnum, Integer[]> blockingErrors, Integer indexMin,
                                   Integer indexMax, ImportErrorEnum incorrectErrorEnum, boolean errorOnNegativeValues, ImportErrorEnum negativeErrorEnum) {
        if (indexMin != null && indexMax != null) {
            Double minValue = StringUtils.isNotBlank(values[indexMin]) ? importHelper.getDoubleValue(values[indexMin]) : null;
            Double maxValue = StringUtils.isNotBlank(values[indexMax]) ? importHelper.getDoubleValue(values[indexMax]) : null;

            if (errorOnNegativeValues && (minValue != null && minValue < 0 || maxValue != null && maxValue < 0)) {
                importHelper.addErrorInMap(negativeErrorEnum, lineIndex, blockingErrors);
            }

            if (minValue != null && maxValue != null && minValue > maxValue) {
                importHelper.addErrorInMap(incorrectErrorEnum, lineIndex, blockingErrors);
            }
        }

    }

    /**
     * Vérification de la cohérence des dates (début antérieur à fin)
     *
     * @param lineIndex      index de la ligne
     * @param values         tableau contenant les champs de la ligne
     * @param columnNames    map contenant les colonnes du fichier
     * @param blockingErrors map contenant les erreurs bloquantes
     */
    private void checkDateCoherence(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors) {
        long begin = System.nanoTime();
        var indexesDayStart = columnNames.get(ImportSpecimenColumnEnum.EVENT_DATE_START_DAY.getColumnName());
        var indexesMonthStart = columnNames.get(ImportSpecimenColumnEnum.EVENT_DATE_START_MONTH.getColumnName());
        var indexesYearStart = columnNames.get(ImportSpecimenColumnEnum.EVENT_DATE_START_YEAR.getColumnName());

        var indexesDayEnd = columnNames.get(ImportSpecimenColumnEnum.EVENT_DATE_END_DAY.getColumnName());
        var indexesMonthEnd = columnNames.get(ImportSpecimenColumnEnum.EVENT_DATE_END_MONTH.getColumnName());
        var indexesYearEnd = columnNames.get(ImportSpecimenColumnEnum.EVENT_DATE_END_YEAR.getColumnName());

        if (indexesDayStart != null && indexesDayEnd != null) {
            var dayStart = values[indexesDayStart];
            var monthStart = values[indexesMonthStart];
            var yearStart = values[indexesYearStart];

            var dayEnd = values[indexesDayEnd];
            var monthEnd = values[indexesMonthEnd];
            var yearEnd = values[indexesYearEnd];

            var start = Stream.of(yearStart, monthStart, dayStart).filter(StringUtils::isNotBlank)
                    .map(v -> StringUtils.leftPad(v, 2, '0'))
                    .collect(Collectors.joining("-"));
            var end = Stream.of(yearEnd, monthEnd, dayEnd).filter(StringUtils::isNotBlank)
                    .map(v -> StringUtils.leftPad(v, 2, '0'))
                    .collect(Collectors.joining("-"));

            if (start.isEmpty() && end.isEmpty()) {
                return;
            }

            if (!start.isEmpty() && !end.isEmpty() && start.length() != end.length()) {
                importHelper.addErrorInMap(COLLECTION_DATE_NOT_SAME_FORMAT, lineIndex, blockingErrors);
            }

            var startDate = DateUtil.getLocaleDate(start);
            var endDate = DateUtil.getLocaleDate(end);

            if (startDate == null && endDate != null || (startDate != null && endDate != null && startDate.isAfter(endDate))) {
                importHelper.addErrorInMap(INCORRECT_COLLECTION_DATE, lineIndex, blockingErrors);
            }
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - begin;
        addTimes("checkDateCoherence", timeElapsed);
    }

    /**
     * Vérifie si le spécimen identifié sur une ligne est déjà présent dans le fichier
     *
     * @param values              tableau contenant les champs de la ligne
     * @param columnNames         map contenant les colonnes du fichier
     * @param specimenIdentifiers set contenant la liste des identifiants des spécimens déjà identifiés dans le fichier
     * @return si le spécimen a déjà été détecté dans le fichier
     */
    private boolean checkDuplicates(String[] values, Map<String, Integer> columnNames, Set<String> specimenIdentifiers) {
        long start = System.nanoTime();
        var collectionName = values[columnNames.get(COLLECTION_NAME.getColumnName())];
        var catalogNumber = values[columnNames.get(CATALOG_NUMBER.getColumnName())];

        String key = collectionName + "|" + catalogNumber;
        if (specimenIdentifiers.contains(key)) {

            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            addTimes("checkDuplicates", timeElapsed);
            return true;
        }

        specimenIdentifiers.add(key);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes("checkDuplicates", timeElapsed);
        return false;
    }

    /**
     * Vérifie la validité des valeurs issues de référentiels
     *
     * @param lineIndex         index de la ligne traiteée
     * @param values            tableau contenant les champs de la ligne
     * @param columnNames       map contenant les colonnes du fichier
     * @param nonBlockingErrors map stockant les erreurs non bloquantes
     */
    private void nonBlockingChecks(int lineIndex, String[] values, Map<String, Integer> columnNames,
                                   Map<ImportErrorEnum, Integer[]> nonBlockingErrors) {
        long start = System.nanoTime();
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes("nonBlockingChecks", timeElapsed);
        // TODO DTH valeur saisie ne correspond pas à une valeur du référentiel (cas des listes fermées)
    }

}

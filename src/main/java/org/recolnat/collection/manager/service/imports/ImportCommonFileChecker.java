package org.recolnat.collection.manager.service.imports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum;
import org.recolnat.collection.manager.common.check.service.ControlAttribut;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_SCIENTIFIC_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.USER_RIGHTS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.SCIENTIFIC_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportCommonFileChecker {

    private final DateTimeFormatter patternDays = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter patternMonths = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter patternYears = DateTimeFormatter.ofPattern("yyyy");

    private final ImportHelper importHelper;
    private final CollectionJPARepository collectionJPARepository;
    private final ControlAttribut checkAttribut;

    private static void addTimes(Map<String, Long> times, String key, long timeElapsed) {
        times.replace(key, times.get(key) + timeElapsed);
    }

    /**
     * Verfie la présence des valeurs obligatoires sur une ligne
     *
     * @param lineIndex      index de la ligne traitée
     * @param values         tableau contenant les champs de la ligne
     * @param columnNames    map contenant les colonnes du fichier
     * @param blockingErrors map stockant les erreurs bloquantes
     */
    public void checkRequiredValues(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors,
                                    Map<String, Long> times) {
        long start = System.nanoTime();

        boolean isBlankLine = Arrays.stream(values).allMatch(StringUtils::isBlank);

        if (isBlankLine) {
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            addTimes(times, "checkRequiredValues", timeElapsed);
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

        value = values[columnNames.get(SCIENTIFIC_NAME.getColumnName())];
        if (StringUtils.isBlank(value)) {
            importHelper.addErrorInMap(REQUIRED_SCIENTIFIC_NAME, lineIndex, blockingErrors);
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes(times, "checkRequiredValues", timeElapsed);
    }

    public void checkFieldsFormat(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors,
                                  Map<String, ImportFieldFormatEnum> fieldsToCheck, Map<String, Long> times) {
        long start = System.nanoTime();

        fieldsToCheck.forEach((key, format) -> {
            if (format == ImportFieldFormatEnum.DATE_AS_STRING) {
                checkDateField(lineIndex, values, columnNames, blockingErrors, key, times);
            }

            if (format == ImportFieldFormatEnum.DOUBLE) {
                checkDoubleField(lineIndex, values, columnNames, blockingErrors, key);
            }

            if (format == ImportFieldFormatEnum.YEAR) {
                checkYearField(lineIndex, values, columnNames, blockingErrors, key);
            }
        });

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes(times, "checkFieldsFormat", timeElapsed);
    }

    private void checkDateField(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors, String key,
                                Map<String, Long> times) {
        long start = System.nanoTime();

        String value = importHelper.extractDateValue(values, columnNames, key);
        if (StringUtils.isNotBlank(value) && !checkDateValueFormat(value)) {
            log.info("Wrong value {}:{} => {}", key, lineIndex, value);
            importHelper.addErrorInMap(ImportErrorEnum.valueOf("DATE_FORMAT_" + key), lineIndex, blockingErrors);
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes(times, "checkDateField", timeElapsed);
    }

    private void checkDoubleField(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors,
                                  String key) {
        var columnIndex = columnNames.get(key);
        String value = null;
        if (columnIndex != null) {
            value = values[columnIndex];
        }

        if (StringUtils.isNotBlank(value) && !checkDoubleValueFormat(value)) {
            log.info("Wrong value {}:{} => {}", key, lineIndex, value);
            importHelper.addErrorInMap(ImportErrorEnum.valueOf("FLOAT_FORMAT_" + key), lineIndex, blockingErrors);
        }
    }

    private void checkYearField(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors,
                                String key) {
        var columnIndex = columnNames.get(key);
        String value = null;
        if (columnIndex != null) {
            value = values[columnIndex];
        }

        try {
            if (StringUtils.isNotBlank(value)) {
                Year.parse(value, patternYears);
            }
        } catch (DateTimeParseException e) {
            log.info("Wrong value {}:{} => {}", key, lineIndex, value);
            // TODO DTH revoir la gestion des clés qui utilise le nom et donc qui peut changer
            importHelper.addErrorInMap(ImportErrorEnum.valueOf("YEAR_FORMAT_" + key), lineIndex, blockingErrors);
        }
    }

    private boolean checkDoubleValueFormat(String value) {
        return importHelper.getDoubleValue(value) != null;
    }

    private boolean checkDateValueFormat(String value) {
        var isInterval = value.contains("/");

        if (isInterval) {
            var parts = value.split("/");
            if (parts.length != 2) {
                return false;
            }
            return checkDateFormat(parts[0]) && checkDateFormat(parts[1]);
        } else {
            return checkDateFormat(value);
        }
    }

    /**
     * Vérifie le format d'un champ date
     *
     * @param date valeur à vérifier
     * @return si la valeur est correcte
     */
    private boolean checkDateFormat(String date) {
        var length = date.length();
        if ((length != 4) && (length != 7) && (length != 10)) {
            return false;
        }

        try {
            if (length == 4) {
                Year.parse(date, patternYears);
                return true;
            }
            if (length == 7) {
                YearMonth.parse(date, patternMonths);
                return true;
            }
            LocalDate.parse(date, patternDays);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Vérifie que l'utilisateur courant a bien les droits d'importer un spécimen dans la collection identifiée sur la ligne
     *
     * @param lineIndex      index de la ligne
     * @param values         tableau contenant les champs de la ligne
     * @param columnNames    map contenant les colonnes du fichier
     * @param blockingErrors map contenant les erreurs bloquantes
     * @param rightsCache    map contenant les droits sur les collections
     */
    public void checkUserRights(int lineIndex, String[] values, Map<String, Integer> columnNames, Map<ImportErrorEnum, Integer[]> blockingErrors,
                                UUID institutionId, Map<String, Boolean> rightsCache, Map<String, Long> times) {
        long start = System.nanoTime();
        var collectionName = values[columnNames.get(COLLECTION_NAME.getColumnName())];

        if (rightsCache.get(collectionName) == null) {
            try {
                log.info("Calcul des droits sur la collection {}", collectionName);
                checkAttribut.checkUserRightsOnCollectionByCollectionName(collectionName, institutionId);
                rightsCache.put(collectionName, true);
            } catch (Exception e) {
                importHelper.addErrorInMap(USER_RIGHTS, lineIndex, blockingErrors);
                rightsCache.put(collectionName, false);
            }
        } else if (Boolean.FALSE.equals(rightsCache.get(collectionName))) {
            log.info("Droit déjà calculé, récupération dans le cache");
            importHelper.addErrorInMap(USER_RIGHTS, lineIndex, blockingErrors);
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        addTimes(times, "checkUserRights", timeElapsed);
    }

    /**
     * Vérifie si la collection identifiée sur une ligne existe dans Recolnat
     *
     * @param values           tableau contenant les champs de la ligne
     * @param columnNames      map contenant les colonnes du fichier
     * @param collectionsCache map répertoriant les collections existantes en bdd
     * @return si la collection existe
     */
    public boolean checkIfCollectionExists(String[] values, Map<String, Integer> columnNames, UUID institutionId, Map<String, Boolean> collectionsCache,
                                           Map<String, Long> times) {
        long start = System.nanoTime();

        var collectionName = values[columnNames.get(COLLECTION_NAME.getColumnName())];
        if (collectionsCache.get(collectionName) != null) {
            var exists = collectionsCache.get(collectionName);
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            addTimes(times, "checkIfCollectionExists", timeElapsed);
            return exists;
        } else {
            log.info("Valeur non présente dans le cache, recherche en base, {}", collectionName);
            var exists = collectionJPARepository.existsByInstitutionInstitutionIdAndCollectionNameFr(institutionId, collectionName);
            log.info("La collection {} existe en base, {}", collectionName, exists);
            collectionsCache.put(collectionName, exists);
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            addTimes(times, "checkIfCollectionExists", timeElapsed);
            return exists;
        }
    }

}

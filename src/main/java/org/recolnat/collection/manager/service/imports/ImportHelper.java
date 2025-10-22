package org.recolnat.collection.manager.service.imports;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import io.recolnat.model.ImportErrorDTO;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.ImportColumn;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum;
import org.recolnat.collection.manager.common.util.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportHelper {

    public static final char DELIMITER = ';';
    private static final List<String> TRUE_VALUES = List.of("TRUE", "VRAI", "OUI", "1", "X");

    public List<String[]> extractData(InputStream inputStream) {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(DELIMITER)
                .withIgnoreQuotations(false) // Permet de bien parser les ";" a l'intérieur des champs
                .build();

        try {
            try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                try (CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(0).withCSVParser(parser).build()) {
                    var lines = csvReader.readAll();

                    // On retire le caractère BOM si il est présent
                    lines.get(0)[0] = lines.get(0)[0].replace("\uFEFF", "");

                    return lines;
                } catch (CsvException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier", e);
            return Collections.emptyList();
        }
    }

    public List<String[]> extractDataWithOpenCsv(MultipartFile file) {
        try {
            var lines = extractData(file.getInputStream());

            // On retire le caractère BOM si il est présent
            lines.get(0)[0] = lines.get(0)[0].replace("\uFEFF", "");

            return lines;
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier", e);
            return Collections.emptyList();
        }
    }

    public Map<String, Integer> buildColumnNamesMap(String... columnNames) {
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            map.put(columnName, i);
        }
        return map;
    }

    public String extractDateValue(String[] values, Map<String, Integer> columnNames, String key) {
        // Vérification des champs date sur 3 champs
        var indexesDay = columnNames.get(key + "_JOUR");
        var indexesMonth = columnNames.get(key + "_MOIS");
        var indexesYear = columnNames.get(key + "_ANNEE");
        if (indexesDay != null && indexesMonth != null && indexesYear != null) {
            var day = values[indexesDay];
            var month = values[indexesMonth];
            var year = values[indexesYear];

            return Stream.of(year, month, day).filter(StringUtils::isNotBlank)
                    .map(v -> StringUtils.leftPad(v, 2, '0'))
                    .collect(Collectors.joining("-"));
        }
        return null;
    }

    public String extractIntervalValue(String[] line, Map<String, Integer> columnNames, String columnName) {
        var startFieldKey = columnName + "_DEBUT";
        var endFieldKey = columnName + "_FIN";

        var startValue = extractDateValue(line, columnNames, startFieldKey);
        var endValue = extractDateValue(line, columnNames, endFieldKey);

        if (startValue == null || endValue == null) {
            return null;
        }

        return Stream.of(startValue, endValue).filter(StringUtils::isNotBlank).collect(Collectors.joining("/"));
    }

    public boolean extractBooleanValue(String[] line, Map<String, Integer> columnNamesMap, String columnName) {
        var value = getValueFromCell(line, columnNamesMap, columnName);
        return TRUE_VALUES.contains(value.toUpperCase(Locale.ROOT));
    }

    public boolean existsInMap(Map<String, Integer> columnNamesMap, ImportColumn field) {
        if (field.format() != null) {
            return switch (field.format()) {
                case INTERVAL, DATE_AS_STRING, DATE -> columnNamesMap.keySet().containsAll(field.getFieldNames());
                default -> columnNamesMap.containsKey(field.columnName());
            };
        } else {
            return columnNamesMap.containsKey(field.columnName());
        }
    }


    public String getValueFromCell(String[] line, Map<String, Integer> columnNamesMap, String columnName) {
        if (columnNamesMap.get(columnName) == null) {
            return null;
        }
        return line[columnNamesMap.get(columnName)];
    }

    public void setParameter(String[] line, Map<String, Integer> columnNamesMap, Query query, ImportColumn field) {
        if (field.format() != null) {
            switch (field.format()) {
                case DOUBLE -> {
                    var value = line[columnNamesMap.get(field.columnName())];
                    query.setParameter(field.dbFieldName(), StringUtils.isNotBlank(value) ? Double.parseDouble(value.replace(",", ".")) : null);
                }
                case DATE_AS_STRING -> {
                    String dateValue = extractDateValue(line, columnNamesMap, field.columnName());
                    query.setParameter(field.dbFieldName(), StringUtils.isNotBlank(dateValue) ? dateValue : null);
                }
                case DATE -> {
                    String dateValue = extractDateValue(line, columnNamesMap, field.columnName());
                    query.setParameter(field.dbFieldName(), StringUtils.isNotBlank(dateValue) ? DateUtil.getLocaleDate(dateValue) : null);
                }
                case YEAR -> {
                    var value = line[columnNamesMap.get(field.columnName())];
                    query.setParameter(field.dbFieldName(), StringUtils.isNotBlank(value) ? DateUtil.getLocaleDate(value) : null);
                }
                case INTERVAL -> {
                    String dateValue = extractIntervalValue(line, columnNamesMap, field.columnName());
                    query.setParameter(field.dbFieldName(), StringUtils.isNotBlank(dateValue) ? dateValue : null);
                }
                case BOOLEAN -> {
                    var value = line[columnNamesMap.get(field.columnName())];
                    if (StringUtils.isBlank(value)) {
                        query.setParameter(field.dbFieldName(), null);
                    } else {
                        boolean booleanValue = extractBooleanValue(line, columnNamesMap, field.columnName());
                        query.setParameter(field.dbFieldName(), booleanValue);
                    }
                }
                case REQUIRED_BOOLEAN -> {
                    boolean booleanValue = extractBooleanValue(line, columnNamesMap, field.columnName());
                    query.setParameter(field.dbFieldName(), booleanValue);
                }
            }
        } else {
            var value = line[columnNamesMap.get(field.columnName())];
            query.setParameter(field.dbFieldName(), StringUtils.isNotBlank(value) ? value : null);
        }
    }

    public void addErrorInMap(ImportErrorEnum code, int lineIndex, Map<ImportErrorEnum, Integer[]> map) {
        if (map.get(code) != null) {
            // On décale l'index pour être cohérent avec Excel
            map.replace(code, ArrayUtils.add(map.get(code), lineIndex + 1));
        } else {
            map.put(code, new Integer[]{lineIndex + 1});
        }
    }

    public List<ImportErrorDTO> mapAsListOfErrors(Map<ImportErrorEnum, Integer[]> map) {
        return map.entrySet().stream().map(entry -> {
            var dto = new ImportErrorDTO();
            dto.setCode(entry.getKey().name());
            dto.setLines(Arrays.asList(entry.getValue()));
            return dto;
        }).toList();
    }

    public Double getDoubleValue(String value) {
        String newValue = value.replace(",", ".");
        try {
            return Double.parseDouble(newValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

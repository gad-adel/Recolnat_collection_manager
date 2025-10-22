package org.recolnat.collection.manager.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recolnat.collection.manager.service.imports.ImportHelper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ImportHelperTest {

    @InjectMocks
    private ImportHelper importHelper;

    @Test
    void extractDateValue_ok() {
        String[] line = new String[]{"3", "7", "2024"};
        Map<String, Integer> columnNames = Map.of(
                "DETERMINATION_JOUR", 0,
                "DETERMINATION_MOIS", 1,
                "DETERMINATION_ANNEE", 2
        );
        String columnName = "DETERMINATION";
        var value = importHelper.extractDateValue(line, columnNames, columnName);

        assertThat(value).isEqualTo("2024-07-03");
    }

    @Test
    void extractDateValue_missing_day_field() {
        String[] line = new String[]{"3", "7", "2024"};
        Map<String, Integer> columnNames = Map.of(
                "DETERMINATION_JOUR2", 0,
                "DETERMINATION_MOIS", 1,
                "DETERMINATION_ANNEE", 2
        );
        String columnName = "DETERMINATION";
        var value = importHelper.extractDateValue(line, columnNames, columnName);

        assertThat(value).isNull();
    }

    @Test
    void extractDateValue_missing_month_field() {
        String[] line = new String[]{"3", "7", "2024"};
        Map<String, Integer> columnNames = Map.of(
                "DETERMINATION_JOUR", 0,
                "DETERMINATION_MOIS2", 1,
                "DETERMINATION_ANNEE", 2
        );
        String columnName = "DETERMINATION";
        var value = importHelper.extractDateValue(line, columnNames, columnName);

        assertThat(value).isNull();
    }

    @Test
    void extractDateValue_missing_year_field() {
        String[] line = new String[]{"3", "7", "2024"};
        Map<String, Integer> columnNames = Map.of(
                "DETERMINATION_JOUR", 0,
                "DETERMINATION_MOIS", 1,
                "DETERMINATION_ANNEE2", 2
        );
        String columnName = "DETERMINATION";
        var value = importHelper.extractDateValue(line, columnNames, columnName);

        assertThat(value).isNull();
    }


    @Test
    void extractIntervalValue_ok() {
        String[] line = new String[]{"3", "7", "2024", "12", "11", "2024"};
        Map<String, Integer> columnNames = Map.of(
                "COLLECTE_DEBUT_JOUR", 0,
                "COLLECTE_DEBUT_MOIS", 1,
                "COLLECTE_DEBUT_ANNEE", 2,
                "COLLECTE_FIN_JOUR", 3,
                "COLLECTE_FIN_MOIS", 4,
                "COLLECTE_FIN_ANNEE", 5
        );
        String columnName = "COLLECTE";
        var value = importHelper.extractIntervalValue(line, columnNames, columnName);

        assertThat(value).isEqualTo("2024-07-03/2024-11-12");
    }
}

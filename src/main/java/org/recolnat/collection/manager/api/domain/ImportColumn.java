package org.recolnat.collection.manager.api.domain;

import org.recolnat.collection.manager.api.domain.enums.imports.ImportFieldFormatEnum;

import java.util.List;

public record ImportColumn(String dbFieldName, String columnName, ImportFieldFormatEnum format) {
    public ImportColumn(String dbFieldName, String columnName) {
        this(dbFieldName, columnName, null);
    }

    public List<String> getFieldNames() {
        return switch (format) {
            case DATE_AS_STRING, DATE -> List.of(columnName + "_JOUR", columnName + "_MOIS", columnName + "_ANNEE");
            case INTERVAL -> List.of(
                    columnName + "_DEBUT_JOUR", columnName + "_DEBUT_MOIS", columnName + "_DEBUT_ANNEE",
                    columnName + "_FIN_JOUR", columnName + "_FIN_MOIS", columnName + "_FIN_ANNEE"
            );
            default -> List.of(columnName);
        };
    }
}

package org.recolnat.collection.manager.api.domain.enums.imports;

import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;

import java.util.Locale;

/**
 * Liste des roles dans leur ordre d'importance
 */
public enum ImportModeEnum {
    REPLACE,
    IGNORE;

    public static ImportModeEnum fromValue(String value) {
        for (ImportModeEnum b : values()) {
            if (b.name().equals(value.toUpperCase(Locale.ROOT))) {
                return b;
            }
        }
        throw new CollectionManagerBusinessException("IMPORT_MODE", "Unexpected value '" + value + "'");
    }

}

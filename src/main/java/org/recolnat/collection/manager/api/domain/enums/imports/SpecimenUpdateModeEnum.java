package org.recolnat.collection.manager.api.domain.enums.imports;

import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;

import java.util.Locale;

/**
 * Liste des roles dans leur ordre d'importance
 */
public enum SpecimenUpdateModeEnum {
    CREATED,
    UPDATED;

    public static SpecimenUpdateModeEnum fromValue(String value) {
        for (SpecimenUpdateModeEnum b : values()) {
            if (b.name().equals(value.toUpperCase(Locale.ROOT))) {
                return b;
            }
        }
        throw new CollectionManagerBusinessException("SpecimenUpdateModeEnum", "Unexpected value '" + value + "'");
    }

}

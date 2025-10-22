package org.recolnat.collection.manager.api.domain.enums;

import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;

import java.util.EnumSet;

/**
 * Liste des roles dans leur ordre d'importance
 */
public enum RoleEnum {
    SUPER_ADMIN,
    ADMIN,
    USER_INFRA,
    ADMIN_INSTITUTION,
    ADMIN_COLLECTION,
    DATA_ENTRY,
    USER;

    public static RoleEnum fromValue(String value) {
        for (RoleEnum b : values()) {
            if (b.name().equals(value)) {
                return b;
            }
        }
        throw new CollectionManagerBusinessException("ROLE_NFE_CODE", "Unexpected value '" + value + "'");
    }

    public static boolean isFunctionallRole(String value) {
        return EnumSet.allOf(RoleEnum.class).stream().anyMatch(role -> role.name().equalsIgnoreCase(value));
    }

}

package org.recolnat.collection.manager.api.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public interface InstitutionStatisticProjection {
    Long getId();

    UUID getInstitutionId();

    String getInstitutionName();

    Long getComputerizedSpecimens();

    Long getSpecimensCount();

    Long getTypesCount();

    Long getTaxonsCount();

    LocalDateTime getLastUpdate();
}

package org.recolnat.collection.manager.api.domain;

import java.util.UUID;

public interface CollectionDescriptionProjection {
    UUID getUuid();

    String getNameFr();

    String getNameEn();

    String getCollectionCode();

    String getDescriptionFr();

    String getDescriptionEn();
}

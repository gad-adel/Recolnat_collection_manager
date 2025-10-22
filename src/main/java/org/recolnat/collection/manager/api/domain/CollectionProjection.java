package org.recolnat.collection.manager.api.domain;

import java.util.UUID;

public interface CollectionProjection {

    UUID getId();

    String getNameFr();

    String getNameEn();

    String getType();

    String getCode();
}

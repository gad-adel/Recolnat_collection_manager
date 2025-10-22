package org.recolnat.collection.manager.connector.api;

import org.recolnat.collection.manager.connector.api.domain.Doi;

public interface DoiConnector {

    Doi getDoi(String id);
}

package org.recolnat.collection.manager.connector.api;

import java.util.List;

import org.recolnat.collection.manager.connector.api.domain.TaxonRef;

public interface TaxonRefConnector {
    List<TaxonRef> findByScientifiName(String scientifiName);
    List<String> suggestByScientifiName(String scientifiName);
}

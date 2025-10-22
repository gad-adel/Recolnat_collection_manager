package org.recolnat.collection.manager.service;

import java.util.List;

public interface TaxonService {

	List<String> getFamilyListByPrefix(String prefix, int size);

	List<String> getGenusListByPrefixAndFamily(String query, String family, int size);

	List<String> getSpecificEpithetByPrefix(String query, String genus, Integer size);

}

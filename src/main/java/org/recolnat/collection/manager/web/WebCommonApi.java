package org.recolnat.collection.manager.web;

import org.recolnat.collection.manager.service.CollectionIdentifier;
import org.springframework.http.HttpHeaders;

public interface WebCommonApi {

	default HttpHeaders buildHeader(CollectionIdentifier collectionIdentifier) {
		var header = new HttpHeaders();
		header.add("specimenId", collectionIdentifier.getSpecimenId().toString());
		header.add("collectionId", collectionIdentifier.getCollectionId().toString());
		return header;
	}
}

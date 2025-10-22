package org.recolnat.collection.manager.service;

import io.recolnat.model.DatationResponseDTO;

public interface DatationService {
	DatationResponseDTO retrieveAllDatation(String eonothem, String eratheme, String system, String epoch, String age);
}

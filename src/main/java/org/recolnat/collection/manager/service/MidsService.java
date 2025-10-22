package org.recolnat.collection.manager.service;

import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.web.dto.MidsDTO;

public interface MidsService {

    MidsDTO processMids(Specimen specimen);
}

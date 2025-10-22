package org.recolnat.collection.manager.service;

import org.recolnat.collection.manager.api.domain.enums.OperationTypeEnum;

public interface AuthorisationService {
    void authorize(OperationTypeEnum addCollection, int instId);
}

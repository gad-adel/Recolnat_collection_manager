package org.recolnat.collection.manager.connector.api;

import io.recolnat.model.UserDashboardPageResponseDTO;

import java.util.UUID;

public interface AuthorisationConnector {
    UserDashboardPageResponseDTO getUsers(UUID institutionId, Integer page, Integer size, String searchTerm);
}

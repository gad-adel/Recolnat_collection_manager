package org.recolnat.collection.manager.connector.api.domain;

import java.util.UUID;

public record User(UUID id, String name, String institutionName, String role, String email) {
}

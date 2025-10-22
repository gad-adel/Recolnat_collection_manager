package org.recolnat.collection.manager.api.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ConnectedUser {
    private UUID userId;
    private String userName;
    private String email;
}

package org.recolnat.collection.manager.api.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Builder
@Data
public class UserAttributes {

    private String ui;
    private Integer institution;
    private String role;
    private List<UUID> collections;

    private String jwtUser;

}

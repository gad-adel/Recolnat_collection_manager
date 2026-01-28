package org.recolnat.collection.manager.connector.api.domain;

import io.recolnat.model.InstitutionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class User {

    private UUID uid;

    private String email;

    private String firstName;

    private String lastName;

    private String username;

    private UserRole role;

    private InstitutionDTO institution;

    private List<String> collections;
}

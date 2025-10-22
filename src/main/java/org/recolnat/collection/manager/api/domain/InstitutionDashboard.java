package org.recolnat.collection.manager.api.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class InstitutionDashboard {
    private UUID id;
    @NotBlank(message = "Code institution may not be empty")
    private String code;
    @NotBlank(message = "Name institution may not be empty")
    private String name;
    @NotBlank(message = "partner type may not be empty")
    private String partnerType;
    private Long specimenCount;

}

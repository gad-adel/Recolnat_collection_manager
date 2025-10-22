package org.recolnat.collection.manager.api.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class CollectionCreate {
    @NotBlank(message = "domain is required")
    private String domain;
    @NotBlank(message = "nameFr is required")
    private String collectionNameFr;
    private String collectionNameEn;
    private String descriptionFr;
    private String descriptionEn;
    @NotNull(message = "institutionId is required")
    private UUID institutionId;
    private String collectionCode;
}

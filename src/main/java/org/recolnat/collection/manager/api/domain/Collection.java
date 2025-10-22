package org.recolnat.collection.manager.api.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class Collection extends DomainId {
    private String typeCollection;
    @NotBlank(message = "typeCollection is required")
    private String collectionNameFr;
    private String collectionName;
    private String collectionNameEn;
    private String descriptionFr;
    private String descriptionEn;
    @NotNull(message = "institutionId is required")
    private Integer institutionId;
    @ToString.Include
    private Set<Specimen> specimens;


}

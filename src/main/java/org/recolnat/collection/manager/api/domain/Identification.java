package org.recolnat.collection.manager.api.domain;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder

public class Identification extends DomainId {

    private Boolean currentDetermination;
    private String errorMessage;
    private String verbatimIdentification;
    private Boolean identificationVerificationStatus;
    private String identifiedByID;
    private LocalDate dateIdentified;
    private LocalDate dateIdentifiedEnd;
    private String dateIdentifiedFormat;
    private String typeStatus;
    private String identificationRemarks;
    @Valid
    @NotNull(groups = NormalCheck.class, message = "Taxon information is required")
    private List<Taxon> taxon;

}

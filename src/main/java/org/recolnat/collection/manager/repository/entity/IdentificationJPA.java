package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity(name = "Identification")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false)
public class IdentificationJPA extends AbstractEntity {
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

    @OneToMany(fetch = LAZY, cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "fk_id_identification", nullable = false)
    private List<TaxonJPA> taxon;

}

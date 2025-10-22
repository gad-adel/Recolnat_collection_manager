package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity(name = "geologicalContext")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class GeologicalContextJPA extends AbstractEntity {
    private String verbatimEpoch;

    private String ageAbsolute;

    private String range;

    private String earliestAgeOrLowestStage;

    private String latestAgeOrHighestStage;

    private String earliestEpochOrLowestSeries;

    private String latestEpochOrHighestSeries;

    private String earliestPeriodOrLowestSystem;

    private String latestPeriodOrHighestSystem;

    private String earliestEraOrLowestErathem;

    private String latestEraOrHighestErathem;

    private String earliestEonOrLowestEonothem;

    private String latestEonOrHighestEonothem;

    private String lowestBiostratigraphicZone;

    private String highestBiostratigraphicZone;

    private String formation;

    private String member;

    private String bed;
    @Column(name = "geo_group")
    private String group;

    private String otherLithostratigraphicTerms;

    @OneToOne(mappedBy = "geologicalContext")
    private SpecimenJPA specimen;

}

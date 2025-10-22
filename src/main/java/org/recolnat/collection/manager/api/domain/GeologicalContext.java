package org.recolnat.collection.manager.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class GeologicalContext extends DomainId {

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

    private String group;

    private String formation;

    private String member;

    private String bed;

    private String otherLithostratigraphicTerms;

}

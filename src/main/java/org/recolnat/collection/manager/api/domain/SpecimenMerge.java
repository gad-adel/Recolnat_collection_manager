package org.recolnat.collection.manager.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecimenMerge {

    private String catalogNumber;
    private String recordNumber;
    private String basisOfRecord;
    private String preparations;
    private String individualCount;
    private String sex;
    private String lifeStage;
    private String occurrenceRemarks;
    private String legalStatus;
    private String donor;
    private GeologicalContext geologicalContext;
    private CollectionEvent collectionEvent;
    private Other other;
}

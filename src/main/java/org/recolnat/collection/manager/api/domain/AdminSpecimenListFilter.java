package org.recolnat.collection.manager.api.domain;

import io.recolnat.model.OperationTypeDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
public class AdminSpecimenListFilter {
    private String searchTerm;
    private OperationTypeDTO state;
    private Boolean currentDetermination;
    private Boolean levelType;
    private UUID institutionId;
    private UUID collectionId;
    private String collectionCode;
    private String family;
    private String genus;
    private String specificEpithet;
    private String startDate;
    private String endDate;
    private String collector;
    private List<String> continent;
    private String country;
    private String nominativeCollection;
    private UUID importId;
    private String storageName;
}

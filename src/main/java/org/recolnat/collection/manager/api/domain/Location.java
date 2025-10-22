package org.recolnat.collection.manager.api.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class Location {

    private String municipality;
    private String stateProvince;
    private String region;
    private String county;
    private String country;
    private String continent;
    private String countryCode;
    private String waterBody;
    private String islandGroup;
    private String island;
    private String locality;
    private String locationRemarks;

}

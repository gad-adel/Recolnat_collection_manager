package org.recolnat.collection.manager.repository.entity;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LocationJPA {
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

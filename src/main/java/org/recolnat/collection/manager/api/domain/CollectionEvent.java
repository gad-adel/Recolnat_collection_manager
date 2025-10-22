package org.recolnat.collection.manager.api.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
@EqualsAndHashCode
@Slf4j
public class CollectionEvent extends DomainId {
    private Boolean noCollectionInformation;
    private String eventDate;
    private Boolean interpretedDate;
    private String recordedBy;
    private String fieldNumber;
    private String fieldNotes;
    private String eventRemarks;
    private String verbatimLocality;
    private Boolean sensitiveLocation;
    private Double minimumElevationInMeters;
    private Double maximumElevationInMeters;
    private Boolean interpretedAltitude;
    private Double minimumDepthInMeters;
    private Double maximumDepthInMeters;
    private Boolean interpretedDepth;
    private Double decimalLatitude;
    private Double decimalLongitude;
    private String geodeticDatum;
    private String georeferenceSources;
    private Location location;
    private String habitat;

}

package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.CascadeType.ALL;

@Entity(name = "collectionEvent")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class CollectionEventJPA extends AbstractEntity {

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

    private String habitat;
    @Embedded
    private LocationJPA location;

    @OneToOne(mappedBy = "collectionEvent", cascade = ALL, optional = false)
    private SpecimenJPA specimenJPA;

}

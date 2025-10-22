package org.recolnat.collection.manager.connector.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TaxonRef {
    private String taxonId;
    private String scientificName;
    private String scientificNameAuthorship;
    private String kingdom;
    private String phylum;
    @JsonProperty(value = "class")
    private String taxonClass;
    @JsonProperty(value = "order")
    private String taxonOrder;
    private String subOrder;
    private String family;
    private String genus;
    private String subGenus;
    private String specificEpithet;
    private String infraspecificEpithet;
    @JsonProperty(value = "vernacularNameFr")
    private String vernacularName;
    private String taxonRemarks;
    private String url;
    private String rank;
    private String referentialName;
    private String referentialVersion;
    private String species;
    private String subFamily;
    private String tribu;


}

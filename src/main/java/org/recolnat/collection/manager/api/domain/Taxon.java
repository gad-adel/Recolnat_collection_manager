package org.recolnat.collection.manager.api.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@SuperBuilder
@Getter
@Setter
public class Taxon extends DomainId {

    @NotBlank(message = "scientificName is required", groups = NormalCheck.class)
    private String scientificName;
    private String scientificNameAuthorship;
    private String kingdom;
    private String phylum;
    private String taxonClass;
    private String taxonOrder;
    private String subOrder;
    private String family;
    private String subFamily;
    private String genus;
    private String subGenus;
    private String specificEpithet;
    private String infraspecificEpithet;
    private String vernacularName;
    private String taxonRemarks;
    private String referentialName;
    private String referentialVersion;
    private String referentialTaxonId;
    private LevelTypeEnum levelType;

}

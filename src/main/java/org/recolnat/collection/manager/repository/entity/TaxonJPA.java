package org.recolnat.collection.manager.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum;

@Entity(name = "Taxon")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class TaxonJPA extends AbstractEntity {
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
    @Enumerated(EnumType.STRING)
    private LevelTypeEnum levelType;
}

package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.PublicTaxonDTO;
import io.recolnat.model.TaxonDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.recolnat.collection.manager.connector.api.domain.TaxonRef;
import org.recolnat.collection.manager.repository.entity.TaxonJPA;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = {UUID.class})
public interface TaxonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "levelType", ignore = true)
    @Mapping(target = "referentialTaxonId", source = "taxonId")
    TaxonDTO toDto(TaxonRef taxonRef);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "scientificName", source = "scientificName")
    @Mapping(target = "scientificNameAuthorship", source = "scientificNameAuthorship")
    @Mapping(target = "kingdom", source = "kingdom")
    @Mapping(target = "phylum", source = "phylum")
    @Mapping(target = "taxonClass", source = "taxonClass")
    @Mapping(target = "taxonOrder", source = "taxonOrder")
    @Mapping(target = "subOrder", source = "subOrder")
    @Mapping(target = "family", source = "family")
    @Mapping(target = "subFamily", source = "subFamily")
    @Mapping(target = "genus", source = "genus")
    @Mapping(target = "subGenus", source = "subGenus")
    @Mapping(target = "vernacularName", source = "vernacularName")
    @Mapping(target = "taxonRemarks", source = "taxonRemarks")
    @Mapping(target = "specificEpithet", source = "specificEpithet")
    @Mapping(target = "infraspecificEpithet", source = "infraspecificEpithet")
    @Mapping(target = "levelType", source = "levelType")
    @Mapping(target = "referentialTaxonId", source = "referentialTaxonId")
    @Mapping(target = "referentialVersion", source = "referentialVersion")
    @Mapping(target = "referentialName", source = "referentialName")
    PublicTaxonDTO toPublicDTO(TaxonJPA taxonJPA);
}

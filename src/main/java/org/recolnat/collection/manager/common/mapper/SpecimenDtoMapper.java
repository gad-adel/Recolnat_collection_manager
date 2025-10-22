package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.AdminSpecimenDTO;
import io.recolnat.model.SpecimensListResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.Taxon;
import org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum;

import java.util.Objects;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, CollectionIdentifierMapper.class, IdentificationMapper.class, LiteratureMapper.class})
public interface SpecimenDtoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "scientificName", source = "specimen", qualifiedByName = "scientificNameFromSpecimen")
    @Mapping(target = "scientificNameAuthorship", source = "specimen", qualifiedByName = "scientificNameAuthorshipFromSpecimen")
    @Mapping(target = "catalogNumber", source = "catalogNumber")
    @Mapping(target = "collectionId", source = "collectionId")
    @Mapping(target = "collectionName", source = "collectionName")
    @Mapping(target = "modifiedAt", source = "modifiedAt")
    @Mapping(target = "state", source = "state")
    SpecimensListResponseDTO specimensListToSpecimenListDto(Specimen specimen);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "institutionId", source = "institutionId")
    @Mapping(target = "collectionId", source = "collectionId")
    @Mapping(target = "catalogNumber", source = "catalogNumber")
    @Mapping(target = "nominativeCollection", source = "nominativeCollection")
    @Mapping(target = "recordNumber", source = "recordNumber")
    @Mapping(target = "basisOfRecord", source = "basisOfRecord")
    @Mapping(target = "preparations", source = "preparations")
    @Mapping(target = "individualCount", source = "individualCount")
    @Mapping(target = "sex", source = "sex")
    @Mapping(target = "lifeStage", source = "lifeStage")
    @Mapping(target = "occurrenceRemarks", source = "occurrenceRemarks")
    @Mapping(target = "legalStatus", source = "legalStatus")
    @Mapping(target = "donor", source = "donor")
    @Mapping(target = "identifications", source = "identifications")
    @Mapping(target = "medias", source = "medias")
    @Mapping(target = "other", source = "other")
    @Mapping(target = "geologicalContext", source = "geologicalContext")
    @Mapping(target = "literatures", source = "literatures")
    @Mapping(target = "management", source = "management")
    @Mapping(target = "collectionEvent", source = "collectionEvent")
    AdminSpecimenDTO specimenToAdminSpecimenDto(Specimen specimen);


    @Named("scientificNameFromSpecimen")
    default String scientificNameFromSpecimen(Specimen specimen) {
        return specimen.getIdentifications().stream()
                .filter(Identification::getCurrentDetermination)
                .findFirst()
                .map(Identification::getTaxon)
                .flatMap(taxons -> taxons.stream()
                        .filter(taxon -> taxon.getLevelType().equals(LevelTypeEnum.MASTER))
                        .findFirst().stream().map(Taxon::getScientificName).findFirst()
                ).orElse("");

    }

    @Named("scientificNameAuthorshipFromSpecimen")
    default String scientificNameAuthorshipFromSpecimen(Specimen specimen) {
        return specimen.getIdentifications().stream()
                .filter(Identification::getCurrentDetermination)
                .findFirst()
                .map(Identification::getTaxon)
                .flatMap(taxons -> taxons.stream()
                        .filter(taxon -> taxon.getLevelType().equals(LevelTypeEnum.MASTER))
                        .findFirst().stream().map(Taxon::getScientificNameAuthorship).filter(Objects::nonNull).findFirst()
                ).orElse("");

    }
}

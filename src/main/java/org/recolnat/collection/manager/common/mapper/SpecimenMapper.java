package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.PublicSpecimenDTO;
import io.recolnat.model.SpecimenIntegrationRequestDTO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class, UUID.class},
        uses = {MediaMapper.class, CollectionIdentifierMapper.class, GeologicalContextMapper.class, CollectionEventMapper.class, IdentificationMapper.class,
                LiteratureMapper.class})
public interface SpecimenMapper {
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "user")
    @Mapping(target = "modifiedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "modifiedBy", source = "modifiedBy", qualifiedByName = "user")
    @Mapping(target = "dataChangeTs", ignore = true)
    @Mapping(target = "collection", ignore = true)
    SpecimenJPA mapToSpecimenJpa(Specimen specimen, @Context String uid);


    @Mapping(target = "collection.id", source = "collectionId", qualifiedByName = "StringToUUID")
    @Mapping(target = "modifiedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "modifiedBy", source = "modifiedBy", qualifiedByName = "modifiedByUser")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "checkCreatedAt")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "createdByUser")
    @Mapping(target = "dataChangeTs", ignore = true)
    SpecimenJPA mapToSpecimenJpaForUpdate(Specimen specimen, @Context String uid);

    @Named("StringToUUID")
    default UUID getIdUUID(String id) {
        return UUID.fromString(id);
    }

    @Named("modifiedByUser")
    default String getUidModified(String modifiedBy, @Context String uid) {
        return uid;
    }

    @Named("createdByUser")
    default String getUidCreated(String createdBy, @Context String uid) {
        if (Objects.isNull(createdBy)) {
            return uid;
        }
        return createdBy;
    }

    @Named("collectionCode")
    default String getCollectionCode(SpecimenJPA specimenJPA) {
        return StringUtils.isNotBlank(specimenJPA.getCollection().getCollectionCode()) ? specimenJPA.getCollection()
                .getCollectionCode() : specimenJPA.getCollectionCode();
    }

    @Named("checkCreatedAt")
    default LocalDateTime checkCreatedDate(LocalDateTime createdAt) {
        if (Objects.isNull(createdAt)) {
            return LocalDateTime.now();
        }
        return createdAt;
    }

    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    Specimen mapDtoToSpecimen(SpecimenIntegrationRequestDTO specimen);

    @Mapping(target = "collectionId", source = "collection.id", qualifiedByName = "UUIDtoString")
    @Mapping(target = "collectionName", source = "collection.collectionNameFr")
    // On prend par défaut le code de la collection s'il existe et celui du spécimen sinon
    @Mapping(target = "collectionCode", expression = "java(specimenJpa.getCollection().getCollectionCode() != null ? specimenJpa.getCollection().getCollectionCode() : specimenJpa.getCollectionCode())")
    @Mapping(target = "institutionId", source = "collection.institution.institutionId")
    Specimen mapJpaToSpecimen(SpecimenJPA specimenJpa);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "catalogNumber", source = "catalogNumber")
    @Mapping(target = "collectionCode", source = "specimenJPA", qualifiedByName = "collectionCode")
    @Mapping(target = "collectionName", source = "collection.collectionNameFr")
    @Mapping(target = "collectionId", source = "collection.id")
    @Mapping(target = "institutionCode", source = "collection.institution.code")
    @Mapping(target = "institutionName", source = "collection.institution.name")
    @Mapping(target = "nominativeCollection", source = "nominativeCollection")
    @Mapping(target = "recordNumber", source = "recordNumber")
    @Mapping(target = "basisOfRecord", source = "basisOfRecord")
    @Mapping(target = "individualCount", source = "individualCount")
    @Mapping(target = "sex", source = "sex")
    @Mapping(target = "lifeStage", source = "lifeStage")
    @Mapping(target = "occurrenceRemarks", source = "occurrenceRemarks")
    @Mapping(target = "legalStatus", source = "legalStatus")
    @Mapping(target = "donor", source = "donor")
    @Mapping(target = "preparations", source = "preparations")
    @Mapping(target = "identifications", source = "identifications")
    @Mapping(target = "medias", source = "medias")
    @Mapping(target = "collectionEvent", source = "collectionEvent")
    @Mapping(target = "other", source = "other")
    @Mapping(target = "geologicalContext", source = "geologicalContext")
    @Mapping(target = "literatures", source = "literatures")
    PublicSpecimenDTO toPublicDTO(SpecimenJPA specimenJPA);

    @Mapping(target = "collectionId", source = "collection.id", qualifiedByName = "UUIDtoString")
    @Mapping(target = "collectionName", source = "collection.collectionNameFr")
    @Mapping(target = "collectionEvent", ignore = true)
    @Mapping(target = "literatures", ignore = true)
    @Mapping(target = "geologicalContext", ignore = true)
    @Mapping(target = "medias", ignore = true)
    @Mapping(target = "other", ignore = true)
    Specimen mapJpaToSpecimenBasic(SpecimenJPA specimenJpa);

}

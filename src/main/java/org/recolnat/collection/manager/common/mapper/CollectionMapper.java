package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.CollectionDashboardDTO;
import io.recolnat.model.CollectionDescriptionDTO;
import io.recolnat.model.CollectionDetailDTO;
import io.recolnat.model.CollectionDetailPublicDTO;
import io.recolnat.model.CollectionIntegrationRequestDTO;
import io.recolnat.model.CollectionOptionDTO;
import io.recolnat.model.CollectionPublicDTO;
import io.recolnat.model.CollectionResponseDTO;
import io.recolnat.model.CollectionWithCodeDTO;
import io.recolnat.model.DomainSpecimenCountDTO;
import io.recolnat.model.NominativeCollectionDashboardDTO;
import io.recolnat.model.UserCollectionDTO;
import jakarta.validation.Valid;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.recolnat.collection.manager.api.domain.Collection;
import org.recolnat.collection.manager.api.domain.CollectionCreate;
import org.recolnat.collection.manager.api.domain.CollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.CollectionDescriptionProjection;
import org.recolnat.collection.manager.api.domain.CollectionProjection;
import org.recolnat.collection.manager.api.domain.DomainSpecimenCountProjection;
import org.recolnat.collection.manager.api.domain.NominativeCollectionDashboardProjection;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface CollectionMapper {

    @Mapping(target = "specimens", ignore = true)
    @Mapping(target = "collectionName", source = "collectionJpa", qualifiedByName = "getCollectionName")
    Collection collectionJPAtoCollection(CollectionJPA collectionJpa);

    @Mapping(target = "domain", source = "typeCollection")
    CollectionDetailDTO collectionJPAtoCollectionDetail(CollectionJPA collectionJpa);

    CollectionCreate toCollectionCreate(CollectionIntegrationRequestDTO requestDTO);

    @Mapping(target = "name", source = "collection", qualifiedByName = "getCollectionDashboardNameFromLang")
    @Mapping(target = "type", source = "type")
    CollectionDashboardDTO collectionToCollectionDashboardResponseDTO(CollectionDashboardProjection collection, @Context boolean isFr);

    List<CollectionDashboardDTO> collectionsToCollectionsDashboardResponseDTO(List<CollectionDashboardProjection> collections, @Context boolean isFr);

    CollectionResponseDTO collectionToCollectionResponseDTO(Collection collection);

    @Mapping(target = "institutionCode", ignore = true)
    CollectionPublicDTO collectionToCollectionPublicDTO(Collection collection);

    @Mapping(target = "dataChangeTs", ignore = true)
    @Mapping(target = "specimens", ignore = true)
    @Mapping(target = "institution", ignore = true)
    @Mapping(target = "institutionId", ignore = true)
    @Mapping(target = "typeCollection", source = "domain")
    @Mapping(target = "collectionCode", source = "collectionCode")
    CollectionJPA collectionTocollectionJPA(CollectionCreate collection);

    List<CollectionDetailPublicDTO> collectionsToCollectionDetailPublicDTOs(List<Collection> collections);

    @Named("getCollectionName")
    default String getCollectionName(CollectionJPA col) {
        return col.getCollectionNameFr() != null ? col.getCollectionNameFr() : col.getCollectionNameEn();
    }

    @Named("getCollectionDashboardNameFromLang")
    default String getCollectionDashboardNameFromLang(CollectionDashboardProjection col, @Context boolean isFr) {
        return Optional.ofNullable(isFr ? col.getNameFr() : col.getNameEn()).orElse(col.getNameFr());
    }


    @Named("getCollectionNameFromLang")
    default String getCollectionNameFromLang(CollectionProjection col, @Context boolean isFr) {
        return Optional.ofNullable(isFr ? col.getNameFr() : col.getNameEn()).orElse(col.getNameFr());
    }

    @Named("getCollectionDescriptionFromLang")
    default String getCollectionDescriptionFromLang(CollectionDescriptionProjection col, @Context boolean isFr) {
        return Optional.ofNullable(isFr ? col.getDescriptionFr() : col.getDescriptionEn()).orElse(col.getDescriptionFr());
    }

    @Mapping(source = "uuid", target = "id")
    @Mapping(
            target = "name",
            expression = "java(isFr ? projection.getNameFr() : projection.getNameEn())"
    )
    @Mapping(
            target = "description",
            expression = "java(isFr ? projection.getDescriptionFr() : projection.getDescriptionEn())"
    )
    CollectionDescriptionDTO toDto(CollectionDescriptionProjection projection, @Context boolean isFr);

    List<CollectionDescriptionDTO> toLocalizedCollectionDescriptions(List<CollectionDescriptionProjection> projections, @Context boolean isFr);

    @Mapping(target = "name", source = "collection", qualifiedByName = "getCollectionNameFromLang")
    CollectionOptionDTO toCollectionOptionDTO(CollectionProjection collection, @Context boolean isFr);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "collection", qualifiedByName = "getCollectionNameFromLang")
    @Mapping(target = "code", source = "code")
    CollectionWithCodeDTO toCollectionWithCodeDTO(CollectionProjection collection, @Context boolean isFr);

    List<CollectionOptionDTO> collectionToCollectionOptionDTO(List<CollectionProjection> collections, @Context boolean isFr);

    List<CollectionWithCodeDTO> collectionToCollectionWithCodeDTO(List<CollectionProjection> collections, @Context boolean isFr);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "nameFr", source = "nameFr")
    @Mapping(target = "nameEn", source = "nameEn")
    @Mapping(target = "type", source = "type")
    UserCollectionDTO toUserCollectionDto(CollectionProjection collectionProjection);

    List<UserCollectionDTO> toUserCollectionDtos(List<CollectionProjection> allOptionsByInstitutionId);

    List<@Valid NominativeCollectionDashboardDTO> toNominativeCollectionsDashboardResponseDTO(List<NominativeCollectionDashboardProjection> data);

    DomainSpecimenCountDTO projectionToDto(DomainSpecimenCountProjection projection);

    List<DomainSpecimenCountDTO> projectionsToDtos(List<DomainSpecimenCountProjection> projections);
}

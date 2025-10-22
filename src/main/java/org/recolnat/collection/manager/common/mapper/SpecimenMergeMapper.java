package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.SpecimenIntegrationMergeRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.recolnat.collection.manager.api.domain.SpecimenMerge;

@Mapper(componentModel = "spring")
public interface SpecimenMergeMapper {

    @Mapping(target = "other", ignore = true)
    SpecimenMerge dtoTo(SpecimenIntegrationMergeRequestDTO specimenIntegrationMergeRequestDTO);
}

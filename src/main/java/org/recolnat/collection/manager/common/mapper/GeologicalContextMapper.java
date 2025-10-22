package org.recolnat.collection.manager.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.recolnat.collection.manager.api.domain.GeologicalContext;
import org.recolnat.collection.manager.repository.entity.GeologicalContextJPA;


@Mapper(componentModel = "spring")
public interface GeologicalContextMapper {

	@Mapping(target = "specimen", ignore = true)
	GeologicalContextJPA mapToGeologicalContextJpa(GeologicalContext geo); 
}

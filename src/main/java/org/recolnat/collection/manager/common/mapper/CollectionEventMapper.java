package org.recolnat.collection.manager.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.repository.entity.CollectionEventJPA;

@Mapper(componentModel = "spring")
public interface CollectionEventMapper {
	
	@Mapping(target = "specimenJPA", ignore = true)
	CollectionEventJPA mapToCollectionEventJpa(CollectionEvent colevents);
}

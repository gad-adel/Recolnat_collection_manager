package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.CollectionSpecificationIdPairsDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.recolnat.collection.manager.service.CollectionIdentifier;

/**
 * add disableBuilder cause @aftremapping is disabled if lombock builder is used ( case setIdentificationDTOIdentification)
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {IdentificationMapper.class})
public interface CollectionIdentifierMapper {
    CollectionIdentifier toCollectionIdentifier(CollectionSpecificationIdPairsDTO dto);

}

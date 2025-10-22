package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.MediaDTO;
import io.recolnat.model.PublicMediaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.recolnat.collection.manager.api.domain.Media;
import org.recolnat.collection.manager.repository.entity.MediaJPA;

@Mapper(componentModel = "spring")
public interface MediaMapper {

    @Mapping(target = "mediaName", source = "fileName")
    Media mediaDTOToMedia(MediaDTO mediaDto);

    @Mapping(target = "fileName", source = "mediaName")
    MediaDTO mediaToMediaDTO(Media media);

    @Mapping(target = "fileName", source = "mediaName")
    MediaDTO mediaJpaToMediaDTO(MediaJPA media);

    @Mapping(target = "fileName", source = "mediaName")
    PublicMediaDTO mediaJpaToPublicMediaDTO(MediaJPA media);
}

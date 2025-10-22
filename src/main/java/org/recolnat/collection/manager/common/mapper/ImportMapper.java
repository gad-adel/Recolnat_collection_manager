package org.recolnat.collection.manager.common.mapper;


import io.recolnat.model.ImportDTO;
import io.recolnat.model.ImportFileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.recolnat.collection.manager.repository.entity.ImportFileJPA;
import org.recolnat.collection.manager.repository.entity.ImportJPA;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImportMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fillingDate", source = "timestamp")
    @Mapping(target = "user", source = "email")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "files", source = "files")
    @Mapping(target = "addedSpecimenCount", source = "addedSpecimenCount")
    @Mapping(target = "addedIdentificationCount", source = "addedIdentificationCount")
    @Mapping(target = "addedLiteratureCount", source = "addedLiteratureCount")
    @Mapping(target = "updatedSpecimenCount", source = "updatedSpecimenCount")
    ImportDTO toDTO(ImportJPA importJPA);

    List<ImportDTO> toDTOs(List<ImportJPA> files);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "fileName")
    ImportFileDTO toFileDTO(ImportFileJPA file);

    List<ImportFileDTO> toFileDTOs(List<ImportFileJPA> files);

}

package org.recolnat.collection.manager.common.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.recolnat.collection.manager.connector.api.domain.Doi;
import org.recolnat.collection.manager.web.dto.DoiDTO;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DoiMapper {

    @Mapping(target = "title", source = "title")
    @Mapping(target = "author", source = "doi", qualifiedByName = "authorFromDoi")
    @Mapping(target = "year", source = "doi", qualifiedByName = "yearFromDoi")
    DoiDTO toDTO(Doi doi);


    @Named("authorFromDoi")
    default String authorFromDoi(Doi doi) {
        return doi.getAuthor().stream().map(author -> author.getFamily() + " " + author.getGiven()).collect(Collectors.joining(";"));
    }

    @Named("yearFromDoi")
    default Integer yearFromDoi(Doi doi) {
        return doi.getPublished().getParts().get(0).get(0);
    }
}

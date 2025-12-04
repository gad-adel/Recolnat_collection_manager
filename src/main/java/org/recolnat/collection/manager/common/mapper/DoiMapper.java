package org.recolnat.collection.manager.common.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.recolnat.collection.manager.connector.api.domain.Doi;
import org.recolnat.collection.manager.web.dto.DoiDTO;

import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface DoiMapper {

    @Mapping(target = "title", source = "title")
    @Mapping(target = "author", source = "doi", qualifiedByName = "authorFromDoi")
    @Mapping(target = "year", source = "doi", qualifiedByName = "yearFromDoi")
    DoiDTO toDTO(Doi doi);


    @Named("authorFromDoi")
    default String authorFromDoi(Doi doi) {
        if (doi.getAuthor() == null) {
            return "";
        }
        return doi.getAuthor().stream()
                .map(author -> Stream.of(author.getFamily(), author.getGiven()).filter(StringUtils::isNotBlank).toList())
                .filter(values -> !values.isEmpty())
                .map(values -> String.join(";", values)).findFirst().orElse("");
    }

    @Named("yearFromDoi")
    default Integer yearFromDoi(Doi doi) {
        return doi.getPublished().getParts().get(0).get(0);
    }
}

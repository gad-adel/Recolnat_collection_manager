package org.recolnat.collection.manager.common.mapper;


import io.recolnat.model.LiteratureDTO;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.recolnat.collection.manager.api.domain.Literature;
import org.recolnat.collection.manager.repository.entity.LiteratureJPA;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface LiteratureMapper {

    @Mapping(target = "date", source = "date", qualifiedByName = "toStartOfYear")
    LiteratureJPA mapToLiteratureJpa(Literature literature);

    @Mapping(target = "date", source = "date", qualifiedByName = "toYear")
    LiteratureDTO mapJpaToLiteratureDTO(Literature literature);

    /**
     * Retourne une date correspondant au premier jour de l'année en paramètre.
     *
     * @param year annéé au format YYYY
     * @return une date locale
     */
    @Named("toStartOfYear")
    default LocalDate toStartOfYear(String year) {
        if (StringUtils.isBlank(year)) {
            return null;
        }
        return LocalDate.of(Integer.parseInt(year), 1, 1);
    }

    /**
     * Retourne l'année de la date passée en paramètre.
     *
     * @param date date au format YYYY-01-01
     * @return l'année en chaine de caractères
     */
    @Named("toYear")
    default String toYear(String date) {
        return StringUtils.isBlank(date) ? null : date.split("-")[0];
    }
}

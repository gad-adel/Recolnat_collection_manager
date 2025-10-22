package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.IdentificationDTO;
import io.recolnat.model.PublicIdentificationDTO;
import io.recolnat.model.PublicTaxonDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.common.util.DateUtil;
import org.recolnat.collection.manager.repository.entity.IdentificationJPA;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IdentificationMapper {


    @Mapping(target = "id", source = "id")
    @Mapping(target = "currentDetermination", source = "currentDetermination")
    @Mapping(target = "identifiedByID", source = "identifiedByID")
    @Mapping(target = "dateIdentified", source = "dateIdentified")
    @Mapping(target = "typeStatus", source = "typeStatus")
    @Mapping(target = "identificationRemarks", source = "identificationRemarks")
    @Mapping(target = "verbatimIdentification", source = "verbatimIdentification")
    @Mapping(target = "taxons", source = "taxon")
    PublicIdentificationDTO toPublicDto(IdentificationJPA ident);

    @Mapping(target = "dateIdentified", ignore = true)
    IdentificationDTO identificationJPAToIdentificationDTO(IdentificationJPA ident);

    @Mapping(target = "dateIdentified", ignore = true)
    IdentificationDTO identificationToIdentificationDTO(Identification ident);

    @Mapping(target = "dateIdentifiedFormat", ignore = true)
    @Mapping(target = "dateIdentified", source = "dateIdentified", qualifiedByName = "transformStringDateIdentified")
    @Mapping(target = "dateIdentifiedEnd", source = "dateIdentified", qualifiedByName = "transformStringDateIdentifiedEnd")
    Identification identificationDTOToIdentification(IdentificationDTO dto);

    @Named("transformStringDateIdentified")
    default LocalDate getTransformStringDateIdentified(String dateIdentified) {
        if (Objects.isNull(dateIdentified) || dateIdentified.isEmpty()) {
            return null;
        }
        var splitDate = dateIdentified.split("/");
        return DateUtil.getLocaleDate(splitDate[0]);

    }

    @Named("transformStringDateIdentifiedEnd")
    default LocalDate getTransformStringDateIdentifiedEnd(String dateIdentified) {
        if (Objects.isNull(dateIdentified) || dateIdentified.isEmpty()) {
            return null;
        }
        var splitDate = dateIdentified.split("/");
        if (splitDate.length == 2) {
            return DateUtil.getLocaleDate(splitDate[1]);
        }
        return null;
    }

    @AfterMapping
    default void setIdentificationDTOIdentification(@MappingTarget Identification identification, IdentificationDTO identificationDTO) {
        var dateIdentified = identificationDTO.getDateIdentified();
        if (Objects.isNull(dateIdentified) || dateIdentified.isEmpty()) {
            return;
        }
        var splitDate = dateIdentified.split("/");
        if (splitDate.length == 2) {
            identification.setDateIdentifiedFormat(String.format("%s/%s", getTypeDate(splitDate[0].length()), getTypeDate(splitDate[1].length())));
        } else {
            identification.setDateIdentifiedFormat(getTypeDate(splitDate[0].length()));
        }

    }

    private String getTypeDate(int sizeDate) {
        switch (sizeDate) {
            case 4 -> {
                return "y";
            }
            case 7 -> {
                return "m";
            }
            default -> {
                return "d";
            }
        }
    }

    @AfterMapping
    default void setIdentificationIdentificationDTO(@MappingTarget IdentificationDTO identificationDTO, Identification identification) {
        setDateIdentified(identificationDTO, identification.getDateIdentifiedFormat(), identification.getDateIdentified(), identification.getDateIdentifiedEnd());
    }

    @AfterMapping
    default void setIdentificationJPAIdentificationDTO(@MappingTarget IdentificationDTO identificationDTO, IdentificationJPA identification) {
        setDateIdentified(identificationDTO, identification.getDateIdentifiedFormat(), identification.getDateIdentified(), identification.getDateIdentifiedEnd());
    }

    private void setDateIdentified(IdentificationDTO identificationDTO, String dateIdentifiedFormat, LocalDate dateIdentified, LocalDate dateIdentifiedEnd) {
        String dateStart;
        String dateEnd;

        if (!Objects.isNull(dateIdentifiedFormat) && !dateIdentifiedFormat.isEmpty()) {
            var types = dateIdentifiedFormat.split("/");
            dateStart = DateUtil.getDateByType(types[0], dateIdentified);
            if (types.length == 2) {
                dateEnd = DateUtil.getDateByType(types[1], dateIdentifiedEnd);
            } else {
                dateEnd = DateUtil.getDate(dateIdentifiedEnd);
            }
        } else {
            dateStart = DateUtil.getDate(dateIdentified);
            dateEnd = DateUtil.getDate(dateIdentifiedEnd);
        }

        settingDateIdentified(identificationDTO, dateStart, dateEnd);
    }

    private void settingDateIdentified(IdentificationDTO identificationDTO, String dateIdentified, String dateIdentifiedEnd) {
        if (dateIdentified != null && !dateIdentified.isEmpty()) {
            if (dateIdentifiedEnd != null && !dateIdentifiedEnd.isEmpty()) {
                identificationDTO.setDateIdentified(String.format("%s/%s", dateIdentified, dateIdentifiedEnd));
            } else {
                identificationDTO.setDateIdentified(dateIdentified);
            }
        }
    }

    @Named("UUIDtoString")
    default String getIdString(UUID id) {
        return id.toString();
    }

    @Named("user")
    default String getUid(String createdBy, @Context String uid) {
        return uid;
    }

    @Named("getTaxonListToSet")
    default PublicIdentificationDTO getTaxonListToTaxonSet(IdentificationJPA identificationJPA, @Context TaxonMapper taxonMapper) {
        PublicIdentificationDTO identificationDTO = toPublicDto(identificationJPA);
        identificationDTO.setId(identificationJPA.getId());
        List<PublicTaxonDTO> taxonList = identificationJPA.getTaxon().stream()
                .map(taxonMapper::toPublicDTO).distinct().toList();
        identificationDTO.setTaxons(taxonList);
        return identificationDTO;
    }

    IdentificationJPA identificationToJPA(Identification identification);
}

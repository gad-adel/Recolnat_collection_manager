package org.recolnat.collection.manager.common.mapper;

import io.recolnat.model.InstitutionDTO;
import io.recolnat.model.InstitutionDashboardResponseDTO;
import io.recolnat.model.InstitutionDetailPublicResponseDTO;
import io.recolnat.model.InstitutionDetailResponseDTO;
import io.recolnat.model.InstitutionOptionDTO;
import io.recolnat.model.InstitutionRequestDTO;
import io.recolnat.model.InstitutionResponseDTO;
import io.recolnat.model.InstitutionStatisticsDTO;
import io.recolnat.model.InstitutionsProgramResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.api.domain.InstitutionDashboard;
import org.recolnat.collection.manager.api.domain.InstitutionDetail;
import org.recolnat.collection.manager.api.domain.InstitutionProjection;
import org.recolnat.collection.manager.api.domain.InstitutionPublicResult;
import org.recolnat.collection.manager.api.domain.InstitutionStatisticProjection;
import org.recolnat.collection.manager.api.domain.Result;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.recolnat.collection.manager.repository.entity.InstitutionJPA;

import java.util.List;

import static java.util.Objects.isNull;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    InstitutionDetailPublicResponseDTO toInstDetailsPublicResponseDTO(Institution inst);

    InstitutionDetailResponseDTO toInstDetailsResponseDTO(InstitutionDetail inst);

    InstitutionDashboardResponseDTO toInstitutionDashboardResponseDTO(Result<InstitutionDashboard> institution);

    @Mapping(target = "numberOfElements", expression = "java(institution.getInstitutions() != null ? institution.getInstitutions().size():0)")
    InstitutionResponseDTO toInstitutionResponseDTO(InstitutionPublicResult institution);

    List<InstitutionDTO> institutionToInstitutionDTO(List<Institution> institutions);

    InstitutionOptionDTO toInstitutionOptionDTO(InstitutionProjection institution);

    List<InstitutionOptionDTO> institutionToInstitutionOptionDTO(List<InstitutionProjection> institutions);

    @Mapping(target = "partnerTypeEn", source = "partnerType", qualifiedByName = "PartnerTypeEn")
    @Mapping(target = "partnerTypeFr", source = "partnerType", qualifiedByName = "PartnerTypeFr")
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "assignable", ignore = true)
    Institution toInstitution(InstitutionJPA inst);

    @Mapping(target = "partnerType", source = "partnerType", qualifiedByName = "StringToEnum")
    @Mapping(target = "dataChangeTs", ignore = true)
    InstitutionJPA toInstitutionJPA(Institution inst);

    @Mapping(target = "partnerTypeEn", ignore = true)
    @Mapping(target = "partnerTypeFr", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "assignable", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "institutionId", ignore = true)
    Institution dtoToInstitution(InstitutionRequestDTO institutionDto);

    @Mapping(target = "partnerType", ignore = true)
    @Mapping(target = "mandatoryDescription", ignore = true)
    @Mapping(target = "optionalDescription", ignore = true)
    @Mapping(target = "partnerTypeEn", ignore = true)
    @Mapping(target = "partnerTypeFr", ignore = true)
    @Mapping(target = "collections", ignore = true)
    @Mapping(target = "assignable", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    List<InstitutionsProgramResponseDTO> dtoToInstitutionsProgramResponseDTO(List<Institution> institutionDto);

    @Named("StringToEnum")
    default PartnerType mappartnerType(String label) {
        return PartnerType.getpartnerType(label);
    }

    @Named("PartnerTypeEn")
    default String mapPartnerTypeEn(PartnerType partnerType) {
        return isNull(partnerType) ? null : partnerType.getPartnerEn();
    }

    @Named("PartnerTypeFr")
    default String mapPartnerTypeFr(PartnerType partnerType) {
        return isNull(partnerType) ? null : partnerType.getPartnerFr();
    }

    InstitutionStatisticsDTO toDto(InstitutionStatisticProjection projection);
}

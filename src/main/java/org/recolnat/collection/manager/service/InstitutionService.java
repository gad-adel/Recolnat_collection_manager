package org.recolnat.collection.manager.service;

import io.recolnat.model.InstitutionStatisticsDTO;
import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.api.domain.InstitutionDashboard;
import org.recolnat.collection.manager.api.domain.InstitutionDetail;
import org.recolnat.collection.manager.api.domain.InstitutionProjection;
import org.recolnat.collection.manager.api.domain.InstitutionPublicResult;
import org.recolnat.collection.manager.api.domain.Result;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface InstitutionService {
    Institution getInstitutionById(int id, String lng);

    InstitutionDetail getInstitutionByUUID(UUID idUUID, String lng);

    Institution getInstitutionPublicByUUID(UUID idUUID, String lng);

    Result<InstitutionDashboard> findAll(int page, int size, String searchTerm, String partnerType);

    List<InstitutionProjection> findAllOptions();

    InstitutionPublicResult findAllByPartnerType(int page, int size, PartnerType partnerType);

    UUID addInstitution(Institution institution);

    UUID updateInstitution(UUID id, Institution institution);

    UUID addLogoIntitution(UUID id, MultipartFile img);

    List<Institution> getInstitutionsByCodes(List<String> codes);

    List<Institution> getInstitutionsByIds(List<UUID> ids);

    boolean checkAccessToInstitution(UUID id);

    InstitutionStatisticsDTO getInstitutionStatistics(UUID institutionId);

    void refreshStatisticView();

    List<Long> getInstitutionMids(UUID institutionId);
}

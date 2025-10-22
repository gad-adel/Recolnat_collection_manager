package org.recolnat.collection.manager.web;

import io.recolnat.api.PublicInstitutionApi;
import io.recolnat.model.CollectionDescriptionDTO;
import io.recolnat.model.DomainSpecimenCountDTO;
import io.recolnat.model.InstitutionDetailPublicResponseDTO;
import io.recolnat.model.InstitutionResponseDTO;
import io.recolnat.model.InstitutionStatisticsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.enums.LanguageEnum;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.InstitutionMapper;
import org.recolnat.collection.manager.service.CollectionRetrieveService;
import org.recolnat.collection.manager.service.InstitutionService;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class InstitutionPublicResource implements PublicInstitutionApi {
    private final InstitutionService institutionService;
    private final CollectionRetrieveService collectionRetrieveService;
    private final InstitutionMapper institutionMapper;
    private final SpecimenIntegrationService specimenIntegrationService;

    @Override
    public ResponseEntity<InstitutionResponseDTO> getPublicInstitutions(Integer page, Integer size, String partnerType) {
        try {
            final var institutions = institutionService.findAllByPartnerType(page, size, PartnerType.getpartnerType(partnerType));
            final var result = institutionMapper.toInstitutionResponseDTO(institutions);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<InstitutionDetailPublicResponseDTO> getPublicInstitution(UUID institutionUUID) {
        try {
            final var institution = institutionService.getInstitutionPublicByUUID(institutionUUID, LanguageEnum.FR.name());
            final var institutionToInstitutionResponseDTO = institutionMapper.toInstDetailsPublicResponseDTO(institution);
            return new ResponseEntity<>(institutionToInstitutionResponseDTO, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<InstitutionStatisticsDTO> getInstitutionStatistics(UUID institutionId) {
        try {
            InstitutionStatisticsDTO stats = institutionService.getInstitutionStatistics(institutionId);
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (CollectionManagerBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ERROR_APPLICATIVE, "Une erreur serveur est survenue.");
        }
    }

    @Override
    public ResponseEntity<List<String>> getNominativeCollectionsByInstitutionId(UUID institutionId) {
        try {
            List<String> nominativeCollections = specimenIntegrationService.getNominativeCollectionsByInstitutionId(institutionId);

            return new ResponseEntity<>(nominativeCollections, HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<DomainSpecimenCountDTO>> getDomainSpecimenCounts(UUID institutionId) {
        var count = collectionRetrieveService.getDomainSpecimenCounts(institutionId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CollectionDescriptionDTO>> getCollectionsDescriptions(UUID institutionId, String lng) {
        var desc = collectionRetrieveService.getCollectionsDescriptions(institutionId, lng);
        try {
            return new ResponseEntity<>(desc, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<Long>> getInstitutionMids(UUID institutionId) {
        var desc = institutionService.getInstitutionMids(institutionId);
        return new ResponseEntity<>(desc, HttpStatus.OK);
    }
}

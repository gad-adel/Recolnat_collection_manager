package org.recolnat.collection.manager.web;

import io.recolnat.api.InstitutionApi;
import io.recolnat.model.InstitutionDashboardResponseDTO;
import io.recolnat.model.InstitutionDetailResponseDTO;
import io.recolnat.model.InstitutionOptionDTO;
import io.recolnat.model.InstitutionRequestDTO;
import io.recolnat.model.PartnerResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.enums.LanguageEnum;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.InstitutionMapper;
import org.recolnat.collection.manager.service.InstitutionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class InstitutionResource implements InstitutionApi {

    private final InstitutionService institutionService;
    private final InstitutionMapper institutionMapper;

    @Override
    public ResponseEntity<InstitutionDashboardResponseDTO> getInstitutions(Integer page, Integer size, String searchTerm, String partnerType) {
        try {
            final var result = institutionMapper.toInstitutionDashboardResponseDTO(institutionService.findAll(page, size, searchTerm, partnerType));
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<InstitutionOptionDTO>> getInstitutionOptions() {
        try {
            final var result = institutionMapper.institutionToInstitutionOptionDTO(institutionService.findAllOptions());
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<InstitutionDetailResponseDTO> getInstitutionDetails(UUID institutionId) {
        try {
            final var institution = institutionService.getInstitutionByUUID(institutionId, LanguageEnum.FR.name());
            final var institutionToInstitutionResponseDTO = institutionMapper.toInstDetailsResponseDTO(institution);
            return new ResponseEntity<>(institutionToInstitutionResponseDTO, HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> addInstitution(InstitutionRequestDTO institutionRequestDTO) {
        try {
            var institution = institutionMapper.dtoToInstitution(institutionRequestDTO);
            var institutionId = institutionService.addInstitution(institution);
            return new ResponseEntity<>(buildHeader(institutionId), HttpStatus.CREATED);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> updateInstitution(UUID institutionId, InstitutionRequestDTO institutionRequestDTO) {

        try {
            var institution = institutionMapper.dtoToInstitution(institutionRequestDTO);
            var id = institutionService.updateInstitution(institutionId, institution);
            return new ResponseEntity<>(buildHeader(id), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> addLogoInstitution(UUID institutionId, MultipartFile logo) {
        try {
            UUID id = institutionService.addLogoIntitution(institutionId, logo);
            return new ResponseEntity<>(buildHeader(id), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<PartnerResponseDTO> getInstitutionPartner() {
        try {
            return new ResponseEntity<>(buildInstitutionPartner(), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    private HttpHeaders buildHeader(UUID institutionId) {
        var header = new HttpHeaders();
        header.add("institutionId", institutionId.toString());
        return header;
    }

    private PartnerResponseDTO buildInstitutionPartner() {
        var partnerResponseDTO = new PartnerResponseDTO();

        for (PartnerType value : PartnerType.values()) {
            partnerResponseDTO
                    .addPartnerItem(value.toString());
        }
        return partnerResponseDTO;
    }
}

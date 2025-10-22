package org.recolnat.collection.manager.web;

import io.recolnat.api.VisibilitePublicApi;
import io.recolnat.model.ArticleResponseDTO;
import io.recolnat.model.ArticleResultPageResponseDTO;
import io.recolnat.model.CollectionDetailPublicDTO;
import io.recolnat.model.CollectionPublicDTO;
import io.recolnat.model.GetPublicInstitutionProgramsRequestDTO;
import io.recolnat.model.IndexElasticExist200ResponseDTO;
import io.recolnat.model.InstitutionDTO;
import io.recolnat.model.InstitutionsProgramResponseDTO;
import io.recolnat.model.PublicSpecimenDTO;
import io.recolnat.model.StatisticsResultDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.api.domain.ArticleSearchResult;
import org.recolnat.collection.manager.api.domain.Collection;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.ArticleMapper;
import org.recolnat.collection.manager.common.mapper.CollectionMapper;
import org.recolnat.collection.manager.common.mapper.InstitutionMapper;
import org.recolnat.collection.manager.common.mapper.StatisticsResultMapper;
import org.recolnat.collection.manager.service.ArticleService;
import org.recolnat.collection.manager.service.CollectionRetrieveService;
import org.recolnat.collection.manager.service.InstitutionService;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.recolnat.collection.manager.service.SpecimenStatisticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class PublicResource implements VisibilitePublicApi {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    private final InstitutionService institutionService;
    private final InstitutionMapper institutionMapper;

    private final SpecimenIntegrationService specimenIntegrationService;

    private final SpecimenStatisticsService specimenStatisticsService;
    private final StatisticsResultMapper statisticsResultMapper;

    private final CollectionRetrieveService collectionRetrieveService;
    private final CollectionMapper collectionMapper;


    @Override
    public ResponseEntity<ArticleResponseDTO> getPublicArticleById(UUID id) {
        try {
            var dto = articleMapper.toDTO(articleService.getPublicArticleById(id));
            return new ResponseEntity<>(dto, OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ArticleResultPageResponseDTO> getPublicArticles(Integer page, Integer size) {
        try {
            ArticleSearchResult articleSearchResult = articleService.findAll(PageRequest.of(page, size));
            var dto = articleMapper.toResultArticleDTO(articleSearchResult);
            return new ResponseEntity<>(dto, OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<PublicSpecimenDTO> getPublicSpecimen(UUID specimenId) {
        try {
            return new ResponseEntity<>(specimenIntegrationService.findDetailSpecimen(specimenId), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<CollectionPublicDTO> retrievePublicCollectionById(String id) {
        try {
            Collection collection = collectionRetrieveService.findCollectionById(UUID.fromString(id));
            return new ResponseEntity<>(collectionMapper.collectionToCollectionPublicDTO(collection), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<StatisticsResultDTO> getStatistic() {
        try {
            var homePageStatistics = specimenStatisticsService.getHomePageStatistics();
            var statisticsResultDTO = statisticsResultMapper.toDto(homePageStatistics);
            return new ResponseEntity<>(statisticsResultDTO, OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> updateStatistics() {
        try {
            specimenStatisticsService.clearHomePageStatisticsTTL();
            specimenStatisticsService.getHomePageStatistics();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<InstitutionsProgramResponseDTO>> getPublicInstitutionPrograms(
            GetPublicInstitutionProgramsRequestDTO getPublicInstitutionProgramsRequestDTO) {
        try {
            final var institutions = institutionService.getInstitutionsByCodes(getPublicInstitutionProgramsRequestDTO.getCodeinstitutions());
            return new ResponseEntity<>(institutionMapper.dtoToInstitutionsProgramResponseDTO(institutions), HttpStatus.OK);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> validElasticConnection() {
        try {
            boolean result = specimenIntegrationService.pingElastic();
            if (result) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (CollectionManagerBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<IndexElasticExist200ResponseDTO> indexElasticExist(String indexName) {
        try {
            boolean result = specimenIntegrationService.indexElasticExist(indexName);
            return new ResponseEntity<>(new IndexElasticExist200ResponseDTO().exist(result), HttpStatus.OK);
        } catch (CollectionManagerBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }


    @Override
    public ResponseEntity<List<InstitutionDTO>> getPublicListInstitutions(List<UUID> values) {
        try {
            final var institutions = institutionService.getInstitutionsByIds(values);
            return new ResponseEntity<>(institutionMapper.institutionToInstitutionDTO(institutions), HttpStatus.OK);
        } catch (CollectionManagerBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }


    @Override
    public ResponseEntity<List<CollectionDetailPublicDTO>> getPublicListCollections(List<UUID> values) {
        try {
            var listCollection = collectionRetrieveService.findCollectionsByIds(values);
            return new ResponseEntity<>(collectionMapper.collectionsToCollectionDetailPublicDTOs(listCollection), HttpStatus.OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }
}

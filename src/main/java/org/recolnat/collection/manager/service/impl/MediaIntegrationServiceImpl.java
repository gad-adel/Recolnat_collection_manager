package org.recolnat.collection.manager.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.SpecimenIndex;
import org.recolnat.collection.manager.common.check.service.ControlAttribut;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.connector.api.MediathequeService;
import org.recolnat.collection.manager.repository.entity.MediaJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.jpa.MediaJPARepository;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.ElasticService;
import org.recolnat.collection.manager.service.MediaCreatedEvent;
import org.recolnat.collection.manager.service.MediaIntegrationService;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaIntegrationServiceImpl implements MediaIntegrationService {

    private final MediathequeService mediathequeService;
    private final AuthenticationService authenticationService;
    private final MediaJPARepository mediaJPARepository;
    private final ApplicationEventPublisher publisher;
    private final SpecimenIntegrationRule specimenIntegrationRule;
    private final ControlAttribut checkAttribut;
    private final ElasticService elasticService;
    private final SpecimenIntegrationService specimenIntegrationService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = CollectionManagerBusinessException.class)
    public List<String> add(UUID specimenId, List<MultipartFile> files) {
        final var specimenJPA = specimenIntegrationRule.checkSpecimenExist(specimenId);
        var collectionId = specimenJPA.getCollection().getId();
        checkAttribut.checkUserAuthAttributesForRoleAdminInst(collectionId);
        final var mediaSaved = addMedia(specimenId, files);
        List<String> listMediaUrls = mediaSaved.stream().map(MediaJPA::getMediaUrl).toList();
        if (!listMediaUrls.isEmpty() && elasticService.specimenExistInIndex(specimenId.toString())) {
            var mediaUrl = Optional.of(mediaSaved)
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.stream()
                            .filter(m -> Boolean.TRUE.equals(m.getIsCover()))
                            .findFirst()
                            .orElse(list.get(0))
                    )
                    .map(MediaJPA::getMediaUrl)
                    .filter(StringUtils::isNotEmpty)
                    .orElse(null);

            elasticService.updatePartialSpecimenToRefElastic(
                    SpecimenIndex.builder().id(specimenId.toString())
                            .collectionId(collectionId.toString())
                            .mediaUrl(mediaUrl).build()
            );
        }

        return listMediaUrls;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public List<String> addDraft(UUID specimenId, List<MultipartFile> fileName) {
        final var specimenJPA = specimenIntegrationRule.checkSpecimenExist(specimenId);
        var collectionId = specimenJPA.getCollection().getId();
        checkAttribut.checkUserRightsOnCollection(collectionId);
        final var mediaSaved = addMedia(specimenId, fileName);
        return mediaSaved.stream().map(MediaJPA::getMediaUrl).toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public List<String> addReviewed(UUID specimenId, List<MultipartFile> fileName) {
        final var specimenJPA = specimenIntegrationRule.checkSpecimenExist(specimenId);
        var collectionId = specimenJPA.getCollection().getId();
        // Tout le monde peut soumettre à publication donc on vérifie juste que l'utilisateur a les droits sur la collection
        checkAttribut.checkUserRightsOnCollection(collectionId);
        final var mediaSaved = addMedia(specimenId, fileName);
        return mediaSaved.stream().map(MediaJPA::getMediaUrl).toList();
    }

    public List<MediaJPA> addMedia(UUID specimenId, List<MultipartFile> fileName) {
        final var specimenJPA = specimenIntegrationRule.checkSpecimenExist(specimenId);
        var collectionId = specimenJPA.getCollection().getId();
        specimenIntegrationRule.checkCollectionAndSpecimenExist(collectionId, specimenId);
        var oldSpecMedias = specimenJPA.getMedias();
        List<MediaJPA> listwihtoutUrl = oldSpecMedias.stream().filter(old -> StringUtils.isAllBlank(old.getMediaUrl())).toList();
        if (oldSpecMedias.isEmpty()) {
            throw new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(),
                    "No media found for this Specimen: " + specimenId);
        }
        return buildAndSaveMediaJPAS(fileName, specimenJPA, listwihtoutUrl);
    }

    private List<MediaJPA> buildAndSaveMediaJPAS(List<MultipartFile> files, SpecimenJPA oldSpec, List<MediaJPA> oldSpecMedias) {
        final var uid = authenticationService.findUserAttributes().getUi();
        var mediasTosave = files.stream().filter(file -> !file.isEmpty()).map(file -> {
            try {
                var savePicture = mediathequeService.savePicture(file);
                return MediaCreatedEvent.builder().mediaName(file.getOriginalFilename())
                        .mediaUrl(Objects.requireNonNull(savePicture.getBody()).getMedia().getUrl()).build();
            } catch (IOException e) {
                log.error("Error when try add Media", e);
                throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR.name(),
                        "Could not store media {}");
            }
        }).map(mediaCreatedEvent -> {
            var mediaFound = oldSpecMedias.stream().filter(mediaJPA -> StringUtils.equalsIgnoreCase(mediaJPA.getMediaName(),
                            mediaCreatedEvent.getMediaName())).findFirst()
                    .orElseThrow(() -> new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(),
                            "the media sent is not the same name: " + mediaCreatedEvent.getMediaName()));
            if (Objects.nonNull(mediaFound)) {
                return MediaJPA.builder().id(mediaFound.getId())
                        .mediaUrl(mediaCreatedEvent.getMediaUrl())
                        .mediaName(mediaCreatedEvent.getMediaName().length() > 45 ? mediaCreatedEvent.getMediaName()
                                .substring(0, 44) : mediaCreatedEvent.getMediaName())
                        .isCover(mediaFound.getIsCover())
                        .contributor(mediaFound.getContributor())
                        .description(mediaFound.getDescription())
                        .creator(mediaFound.getCreator())
                        .license(mediaFound.getLicense())
                        .source(mediaFound.getSource()).build();
            }
            return null;
        }).filter(Objects::nonNull).toList();
        mediaJPARepository.saveAll(mediasTosave);
        // update spec prefer builder
        oldSpec.setModifiedBy(uid);
        oldSpec.setModifiedAt(LocalDateTime.now());
        var savedSpecimen = specimenIntegrationService.saveSpecimenJPAAndUpdateMids(oldSpec);

        //publish event
        publisher.publishEvent(mediasTosave);
        return savedSpecimen.getMedias().stream().toList();
    }

}

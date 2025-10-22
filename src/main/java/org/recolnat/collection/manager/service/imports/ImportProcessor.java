package org.recolnat.collection.manager.service.imports;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportFileType;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportStatusEnum;
import org.recolnat.collection.manager.common.util.FileUtil;
import org.recolnat.collection.manager.repository.entity.ImportJPA;
import org.recolnat.collection.manager.repository.jpa.ImportFileJPARepository;
import org.recolnat.collection.manager.repository.jpa.ImportJPARepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportProcessor {

    private final ImportSpecimenProcessor importSpecimenProcessor;
    private final ImportIdentificationProcessor importIdentificationProcessor;
    private final ImportPublicationProcessor importPublicationProcessor;

    private final ImportFileJPARepository importFichierJPARepository;
    private final ImportJPARepository importJPARepository;

    @Value("${filesystem.base-directory}")
    private String baseDirectory;

    @Value("${import.directory}")
    private String importDirectory;

    @Transactional
    public Optional<ImportJPA> findFirstPending() {
        var importOpt = importJPARepository.findFirstPending();
        if (importOpt.isPresent()) {
            var importJPA = importOpt.get();
            importJPA.setStatus(ImportStatusEnum.RUNNING);
            return Optional.of(importJPARepository.save(importJPA));
        }
        return Optional.empty();
    }

    public boolean handleImport(ImportJPA importJPA) {
        if (log.isInfoEnabled()) {
            log.info("[{}] Début du traitement de l'import", importJPA.getId());
        }

        // Map pour stocker les collections recherchées (groupées par institution)
        Map<UUID, Map<String, UUID>> collectionCache = new HashMap<>();

        long addedSpecimenCount = 0;
        long addedIdentificationCount = 0;
        long addedLiteratureCount = 0;
        long updatedSpecimenCount = 0;

        var directoryName = Path.of(baseDirectory, importDirectory, importJPA.getId().toString()).toString();
        var files = importFichierJPARepository.findAllByImportJPA_Id(importJPA.getId());
        try {
            var specimenFile = files.stream().filter(f -> f.getFileType() == ImportFileType.SPECIMEN).findFirst();
            if (specimenFile.isPresent()) {
                var file = FileUtil.getFileFromDirectory(directoryName, specimenFile.get().getFileName());
                collectionCache.put(importJPA.getInstitutionId(), new HashMap<>());
                var result = importSpecimenProcessor.handleFile(file, importJPA.getUserName(), collectionCache, importJPA.getInstitutionId(), specimenFile.get()
                        .getMode(), importJPA.getId());
                addedSpecimenCount = result.addedSpecimenCount();
                updatedSpecimenCount = result.updatedSpecimenCount();
            }

            var determinationFile = files.stream().filter(f -> f.getFileType() == ImportFileType.IDENTIFICATION).findFirst();
            if (determinationFile.isPresent()) {
                var file = FileUtil.getFileFromDirectory(directoryName, determinationFile.get().getFileName());
                addedIdentificationCount = importIdentificationProcessor.handleFile(file, importJPA.getInstitutionId());
            }

            var publicationFile = files.stream().filter(f -> f.getFileType() == ImportFileType.LITERATURE).findFirst();
            if (publicationFile.isPresent()) {
                var file = FileUtil.getFileFromDirectory(directoryName, publicationFile.get().getFileName());
                addedLiteratureCount = importPublicationProcessor.handleFile(file, importJPA.getInstitutionId());
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("[{}] Erreur lors du traitement de l'import", importJPA.getId(), e);
            }
            importJPA.setStatus(ImportStatusEnum.ERROR);
            importJPARepository.save(importJPA);
            if (log.isInfoEnabled()) {
                log.info("[{}] Fin du traitement de l'import : ERROR", importJPA.getId());
            }
            return false;
        }

        importJPA.setStatus(ImportStatusEnum.PROCESSED);
        importJPA.setAddedSpecimenCount(addedSpecimenCount);
        importJPA.setUpdatedSpecimenCount(updatedSpecimenCount);
        importJPA.setAddedIdentificationCount(addedIdentificationCount);
        importJPA.setAddedLiteratureCount(addedLiteratureCount);

        importJPARepository.save(importJPA);
        if (log.isInfoEnabled()) {
            log.info("[{}] Fin du traitement de l'import : OK", importJPA.getId());
        }
        return true;
    }

}

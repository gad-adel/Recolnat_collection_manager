package org.recolnat.collection.manager.service.impl;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.ImportStructureErrorDTO;
import io.recolnat.model.TemplateFieldDTO;
import io.recolnat.model.TemplateStructureDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.service.ImportPublicationService;
import org.recolnat.collection.manager.service.ImportService;
import org.recolnat.collection.manager.service.imports.ImportHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportPublicationServiceImpl implements ImportPublicationService {

    private static final List<String> PUBLICATION_REQUIRED_FIELDS = List.of(
            ImportPublicationColumnEnum.COLLECTION_NAME.getColumnName(),
            ImportPublicationColumnEnum.CATALOG_NUMBER.getColumnName(),
            ImportPublicationColumnEnum.CITATION.getColumnName());

    private final ImportService importService;
    private final ImportHelper importHelper;

    @Override
    public ImportCheckDataResponseDTO checkPublications(MultipartFile file, UUID institutionId) {
        var response = importService.checkFileCommonProperties(file, institutionId);

        // Vérification de la présence des colonnes obligatoires dans le fichier
        if (Boolean.TRUE.equals(response.getFormat())) {
            response.setStructureErrors(checkPublicationFileStructure(file));
        } else {
            response.setStructureErrors(new ArrayList<>());
        }

        return response;
    }

    private List<ImportStructureErrorDTO> checkPublicationFileStructure(MultipartFile file) {
        List<ImportStructureErrorDTO> errors = new ArrayList<>();
        long start = System.nanoTime();
        var lines = importHelper.extractDataWithOpenCsv(file);
        var header = lines.get(0);

        // Si le séparateur n'est pas correct
        if (header.length == 1) {
            throw new CollectionManagerBusinessException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), "wrong_separator");
        }

        importService.checkRequiredColumnNames(header, errors, PUBLICATION_REQUIRED_FIELDS);
        importService.hasAllColumnsInList(header, errors, column -> ImportPublicationColumnEnum.fromValue(column) == null);
        importService.checkDuplicateColumns(header, errors);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        log.info("ImportSerch::checkPublicationFileStructure : {} ms", timeElapsed);

        return errors;
    }

    @Override
    public List<TemplateStructureDTO> getTemplateStructure() {
        return List.of(
                TemplateStructureDTO.builder().name("Specimen")
                        .children(List.of(
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.COLLECTION_NAME)).required(true).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.CATALOG_NUMBER)).required(true).build()
                        )).build(),
                TemplateStructureDTO.builder().name("Publication")
                        .children(List.of(
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.IDENTIFIER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.URL)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.CITATION)).required(true).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.TITLE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.AUTHORS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.DATE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.LANGUAGE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.KEYWORDS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.DESCRIPTION)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.REMARKS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.REVIEW)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.VOLUME)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.NUMBER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.PAGES)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.BOOK_TITLE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.PUBLISHER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.PUBLICATION_PLACE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.EDITORS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportPublicationColumnEnum.PAGE_NUMBER)).build()
                        )).build()
        );
    }

    @Override
    public String generatePublicationTemplate(List<String> columns) {
        List<String> cols = new ArrayList<>();

        columns.forEach(column -> {
            var v = ImportPublicationColumnEnum.valueOf(column);
            cols.add(v.getColumnName());
        });

        return String.join(";", cols);
    }
}

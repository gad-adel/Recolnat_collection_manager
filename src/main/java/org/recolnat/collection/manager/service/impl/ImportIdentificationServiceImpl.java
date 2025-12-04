package org.recolnat.collection.manager.service.impl;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.ImportStructureErrorDTO;
import io.recolnat.model.TemplateFieldDTO;
import io.recolnat.model.TemplateStructureDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportIdentificationColumnEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.service.ImportIdentificationService;
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
public class ImportIdentificationServiceImpl implements ImportIdentificationService {

    private static final List<String> DETERMINATION_REQUIRED_FIELDS = List.of(
            ImportIdentificationColumnEnum.COLLECTION_NAME.getColumnName(),
            ImportIdentificationColumnEnum.CATALOG_NUMBER.getColumnName(),
            ImportIdentificationColumnEnum.SCIENTIFIC_NAME.getColumnName());

    private final ImportService importService;
    private final ImportHelper importHelper;

    @Override
    public ImportCheckDataResponseDTO checkDeterminations(MultipartFile file, UUID institutionId) {
        var response = importService.checkFileCommonProperties(file, institutionId);

        // Vérification de la présence des colonnes obligatoires dans le fichier
        if (Boolean.TRUE.equals(response.getFormat())) {
            response.setStructureErrors(checkDeterminationFileStructure(file));
        } else {
            response.setStructureErrors(new ArrayList<>());
        }

        return response;
    }

    @Override
    public String generateIdentificationTemplate(List<String> columns) {
        List<String> cols = new ArrayList<>();

        columns.forEach(column -> {
            if (column.equals("DATE_IDENTIFIED")) {
                cols.add(ImportIdentificationColumnEnum.DATE_IDENTIFIED_YEAR.getColumnName());
                cols.add(ImportIdentificationColumnEnum.DATE_IDENTIFIED_MONTH.getColumnName());
                cols.add(ImportIdentificationColumnEnum.DATE_IDENTIFIED_DAY.getColumnName());
            } else {
                var v = ImportIdentificationColumnEnum.valueOf(column);
                cols.add(v.getColumnName());
            }
        });

        return String.join(";", cols);
    }

    @Override
    public List<TemplateStructureDTO> getTemplateStructure() {
        return List.of(
                TemplateStructureDTO.builder().name("Specimen")
                        .children(List.of(
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.COLLECTION_NAME)).required(true).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.CATALOG_NUMBER)).required(true).build()
                        )).build(),
                TemplateStructureDTO.builder().name("Determination")
                        .children(List.of(
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.CURRENT_DETERMINATION)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.VERBATIM_IDENTIFICATION)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.IDENTIFICATION_VERIFICATION_STATUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.IDENTIFICATION_REMARKS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.TYPE_STATUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.IDENTIFIED_BYID)).build(),
                                TemplateFieldDTO.builder().key("DATE_IDENTIFIED").build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.SCIENTIFIC_NAME)).required(true).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.SCIENTIFIC_NAME_AUTHORSHIP)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.VERNACULAR_NAME)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.FAMILY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.SUB_FAMILY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.GENUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.SUB_GENUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.SPECIFIC_EPITHET)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.INFRASPECIFIC_EPITHET)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.KINGDOM)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.PHYLUM)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.TAXON_ORDER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.TAXON_CLASS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.SUB_ORDER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportIdentificationColumnEnum.TAXON_REMARKS)).build()
                        )).build()
        );
    }

    private List<ImportStructureErrorDTO> checkDeterminationFileStructure(MultipartFile file) {
        List<ImportStructureErrorDTO> errors = new ArrayList<>();
        long start = System.nanoTime();
        var lines = importHelper.extractDataWithOpenCsv(file);
        var header = lines.get(0);

        // Si le séparateur n'est pas correct
        if (header.length == 1) {
            throw new CollectionManagerBusinessException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), "wrong_separator");
        }

        importService.checkRequiredColumnNames(header, errors, DETERMINATION_REQUIRED_FIELDS);
        importService.hasAllColumnsInList(header, errors, column -> ImportIdentificationColumnEnum.fromValue(column) == null);
        importService.checkDuplicateColumns(header, errors);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        if (log.isInfoEnabled()) {
            log.info("ImportSerch::checkDeterminationFileStructure : {} ms", timeElapsed);
        }

        return errors;
    }
}

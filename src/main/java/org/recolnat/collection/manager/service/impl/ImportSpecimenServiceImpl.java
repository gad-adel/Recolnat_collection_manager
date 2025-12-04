package org.recolnat.collection.manager.service.impl;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.ImportStructureErrorDTO;
import io.recolnat.model.TemplateFieldDTO;
import io.recolnat.model.TemplateStructureDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.service.ImportService;
import org.recolnat.collection.manager.service.ImportSpecimenService;
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
public class ImportSpecimenServiceImpl implements ImportSpecimenService {

    private static final List<String> SPECIMEN_REQUIRED_FIELDS = List.of(
            ImportSpecimenColumnEnum.COLLECTION_NAME.getColumnName(),
            ImportSpecimenColumnEnum.CATALOG_NUMBER.getColumnName(),
            ImportSpecimenColumnEnum.SCIENTIFIC_NAME.getColumnName());

    private final ImportService importService;
    private final ImportHelper importHelper;

    @Override
    public ImportCheckDataResponseDTO checkSpecimens(MultipartFile file, UUID institutionId) {
        var response = importService.checkFileCommonProperties(file, institutionId);

        // Vérification de la présence des colonnes obligatoires dans le fichier
        if (Boolean.TRUE.equals(response.getFormat())) {
            response.setStructureErrors(checkSpecimenFileStructure(file));
        } else {
            response.setStructureErrors(new ArrayList<>());
        }

        return response;
    }

    @Override
    public String generateSpecimenTemplate(List<String> columns) {
        List<String> cols = new ArrayList<>();

        columns.forEach(column -> {
            switch (column) {
                case "EVENT_DATE": {
                    cols.add(ImportSpecimenColumnEnum.EVENT_DATE_START_DAY.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.EVENT_DATE_START_MONTH.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.EVENT_DATE_START_YEAR.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.EVENT_DATE_END_DAY.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.EVENT_DATE_END_MONTH.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.EVENT_DATE_END_YEAR.getColumnName());
                    break;
                }
                case "LATITUDE_LONGITUDE": {
                    cols.add(ImportSpecimenColumnEnum.DECIMAL_LATITUDE.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.DECIMAL_LONGITUDE.getColumnName());
                    break;
                }
                case "ELEVATION": {
                    cols.add(ImportSpecimenColumnEnum.MINIMUM_ELEVATION_IN_METERS.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.MAXIMUM_ELEVATION_IN_METERS.getColumnName());
                    break;
                }
                case "DEPTH": {
                    cols.add(ImportSpecimenColumnEnum.MINIMUM_DEPTH_IN_METERS.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.MAXIMUM_DEPTH_IN_METERS.getColumnName());
                    break;
                }
                case "DATE_IDENTIFIED": {
                    cols.add(ImportSpecimenColumnEnum.DATE_IDENTIFIED_YEAR.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.DATE_IDENTIFIED_MONTH.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.DATE_IDENTIFIED_DAY.getColumnName());
                    break;
                }
                case "AGE_STAGE": {
                    cols.add(ImportSpecimenColumnEnum.EARLIEST_AGE_OR_LOWEST_STAGE.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.LATEST_AGE_OR_HIGHEST_STAGE.getColumnName());
                    break;
                }
                case "EPOCH_SERIES": {
                    cols.add(ImportSpecimenColumnEnum.EARLIEST_EPOCH_OR_LOWEST_SERIES.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.LATEST_EPOCH_OR_HIGHEST_SERIES.getColumnName());
                    break;
                }
                case "PERIOD_SYSTEM": {
                    cols.add(ImportSpecimenColumnEnum.EARLIEST_PERIOD_OR_LOWEST_SYSTEM.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.LATEST_PERIOD_OR_HIGHEST_SYSTEM.getColumnName());
                    break;
                }
                case "ERA_ERATHEM": {
                    cols.add(ImportSpecimenColumnEnum.EARLIEST_ERA_OR_LOWEST_ERATHEM.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.LATEST_ERA_OR_HIGHEST_ERATHEM.getColumnName());
                    break;
                }
                case "EON_EONOTHEM": {
                    cols.add(ImportSpecimenColumnEnum.EARLIEST_EON_OR_LOWEST_EONOTHEM.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.LATEST_EON_OR_HIGHEST_EONOTHEM.getColumnName());
                    break;
                }
                case "BIOSTRATIGRAPHIC_ZONE": {
                    cols.add(ImportSpecimenColumnEnum.LOWEST_BIOSTRATIGRAPHIC_ZONE.getColumnName());
                    cols.add(ImportSpecimenColumnEnum.HIGHEST_BIOSTRATIGRAPHIC_ZONE.getColumnName());
                    break;
                }
                default: {
                    var v = ImportSpecimenColumnEnum.valueOf(column);
                    cols.add(v.getColumnName());
                }
            }
        });

        return String.join(";", cols);
    }

    @Override
    public List<TemplateStructureDTO> getTemplateStructure() {
        return List.of(
                TemplateStructureDTO.builder().name("Specimen")
                        .children(List.of(
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.COLLECTION_NAME)).required(true).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.CATALOG_NUMBER)).required(true).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.NOMINATIVE_COLLECTION)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.RECORD_NUMBER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.BASIS_OF_RECORD)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.PREPARATIONS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.PREPARATION_DETAIL)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.SEX)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.LIFE_STAGE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.INDIVIDUAL_COUNT)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.OCCURRENCE_REMARKS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.LEGAL_STATUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.DONOR)).build()
                        )).build(),
                TemplateStructureDTO.builder().name("Collecte")
                        .children(List.of(
                                TemplateFieldDTO.builder().key("EVENT_DATE").build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.INTERPRETED_DATE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.RECORDED_BY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.FIELD_NUMBER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.FIELD_NOTES)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.EVENT_REMARKS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.VERBATIM_LOCALITY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.SENSITIVE_LOCATION)).build(),
                                TemplateFieldDTO.builder().key("LATITUDE_LONGITUDE").build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.GEODETIC_DATUM)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.GEOREFERENCE_SOURCES)).build(),
                                TemplateFieldDTO.builder().key("ELEVATION").build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.INTERPRETED_ALTITUDE)).build(),
                                TemplateFieldDTO.builder().key("DEPTH").build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.INTERPRETED_DEPTH)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.LOCALITY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.MUNICIPALITY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.COUNTY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.REGION_STATE_PROVINCE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.COUNTRY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.COUNTRY_CODE)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.CONTINENT)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.ISLAND)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.ISLAND_GROUP)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.WATER_BODY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.HABITAT)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.LOCATION_REMARKS)).build()
                        )).build(),
                TemplateStructureDTO.builder().name("Determination")
                        .children(List.of(
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.VERBATIM_IDENTIFICATION)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.IDENTIFICATION_VERIFICATION_STATUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.IDENTIFICATION_REMARKS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.TYPE_STATUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.IDENTIFIED_BYID)).build(),
                                TemplateFieldDTO.builder().key("DATE_IDENTIFIED").build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.SCIENTIFIC_NAME)).required(true).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.SCIENTIFIC_NAME_AUTHORSHIP)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.VERNACULAR_NAME)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.FAMILY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.SUB_FAMILY)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.GENUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.SUB_GENUS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.SPECIFIC_EPITHET)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.INFRASPECIFIC_EPITHET)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.KINGDOM)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.PHYLUM)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.TAXON_ORDER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.TAXON_CLASS)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.SUB_ORDER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.TAXON_REMARKS)).build()
                        )).build(),
                TemplateStructureDTO.builder().name("Datation")
                        .children(List.of(
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.VERBATIM_EPOCH)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.AGE_ABSOLUTE)).build(),
                                TemplateFieldDTO.builder().key("AGE_STAGE").build(),
                                TemplateFieldDTO.builder().key("EPOCH_SERIES").build(),
                                TemplateFieldDTO.builder().key("PERIOD_SYSTEM").build(),
                                TemplateFieldDTO.builder().key("ERA_ERATHEM").build(),
                                TemplateFieldDTO.builder().key("EON_EONOTHEM").build(),
                                TemplateFieldDTO.builder().key("BIOSTRATIGRAPHIC_ZONE").build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.GEO_GROUP)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.FORMATION)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.MEMBER)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.BED)).build(),
                                TemplateFieldDTO.builder().key(String.valueOf(ImportSpecimenColumnEnum.OTHER_LITHOSTRATIGRAPHIC_TERMS)).build()
                        )).build()
        );
    }

    private List<ImportStructureErrorDTO> checkSpecimenFileStructure(MultipartFile file) {
        List<ImportStructureErrorDTO> errors = new ArrayList<>();
        long start = System.nanoTime();
        var lines = importHelper.extractDataWithOpenCsv(file);
        var header = lines.get(0);

        // Si le séparateur n'est pas correct
        if (header.length == 1) {
            throw new CollectionManagerBusinessException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), "wrong_separator");
        }

        importService.checkRequiredColumnNames(header, errors, SPECIMEN_REQUIRED_FIELDS);
        importService.hasAllColumnsInList(header, errors, column -> ImportSpecimenColumnEnum.fromValue(column) == null);
        importService.checkDuplicateColumns(header, errors);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        if (log.isInfoEnabled()) {
            log.info("ImportSerch::checkSpecimenFileStructure : {} ms", timeElapsed);
        }

        return errors;
    }


}

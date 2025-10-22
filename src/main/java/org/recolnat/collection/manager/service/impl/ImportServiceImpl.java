package org.recolnat.collection.manager.service.impl;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.ImportCheckResponseDTO;
import io.recolnat.model.ImportPageResponseDTO;
import io.recolnat.model.ImportStructureErrorDTO;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.Mail;
import org.recolnat.collection.manager.api.domain.enums.MailStatusEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportFileType;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportIdentificationColumnEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportModeEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportStatusEnum;
import org.recolnat.collection.manager.api.domain.imports.ImportCheckSpecimen;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.mapper.ImportMapper;
import org.recolnat.collection.manager.common.util.FileUtil;
import org.recolnat.collection.manager.repository.entity.ImportFileJPA;
import org.recolnat.collection.manager.repository.entity.ImportJPA;
import org.recolnat.collection.manager.repository.jpa.ImportFileJPARepository;
import org.recolnat.collection.manager.repository.jpa.ImportJPARepository;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.ElasticService;
import org.recolnat.collection.manager.service.ImportService;
import org.recolnat.collection.manager.service.InstitutionService;
import org.recolnat.collection.manager.service.MailService;
import org.recolnat.collection.manager.service.imports.ImportHelper;
import org.recolnat.collection.manager.service.imports.ImportIdentificationFileChecker;
import org.recolnat.collection.manager.service.imports.ImportProcessor;
import org.recolnat.collection.manager.service.imports.ImportPublicationFileChecker;
import org.recolnat.collection.manager.service.imports.ImportSpecimenFileChecker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.DUPLICATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.INCORRECT;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportServiceImpl implements ImportService {

    public static final int MAX_FILE_SIZE = 1024 * 1024 * 50;
    public static final String ACCESS_DENIED = "Access denied";

    // Nom des colonnes
    private static final List<String> SPECIMEN_REQUIRED_FIELDS = List.of(
            ImportSpecimenColumnEnum.COLLECTION_NAME.getColumnName(),
            ImportSpecimenColumnEnum.CATALOG_NUMBER.getColumnName(),
            ImportSpecimenColumnEnum.SCIENTIFIC_NAME.getColumnName());
    private static final List<String> DETERMINATION_REQUIRED_FIELDS = List.of(
            ImportIdentificationColumnEnum.COLLECTION_NAME.getColumnName(),
            ImportIdentificationColumnEnum.CATALOG_NUMBER.getColumnName(),
            ImportIdentificationColumnEnum.SCIENTIFIC_NAME.getColumnName());
    private static final List<String> PUBLICATION_REQUIRED_FIELDS = List.of(
            ImportPublicationColumnEnum.COLLECTION_NAME.getColumnName(),
            ImportPublicationColumnEnum.CATALOG_NUMBER.getColumnName(),
            ImportPublicationColumnEnum.CITATION.getColumnName());

    private final InstitutionService institutionService;
    private final AuthenticationService authenticationService;
    private final ElasticService elasticService;
    private final MailService mailService;

    private final ImportJPARepository importJPARepository;
    private final ImportFileJPARepository importFileJPARepository;

    private final ImportProcessor importProcessor;
    private final ImportHelper importHelper;

    private final ImportMapper importMapper;

    private final ImportSpecimenFileChecker importSpecimenFileChecker;
    private final ImportIdentificationFileChecker importIdentificationFileChecker;
    private final ImportPublicationFileChecker importPublicationFileChecker;

    @Value("${filesystem.base-directory}")
    private String baseDirectory;

    @Value("${import.directory}")
    private String importDirectory;

    @Override
    public void run() {
        long start = System.nanoTime();
        var importOpt = importProcessor.findFirstPending();

        if (importOpt.isPresent()) {
            ImportJPA importJPA = importOpt.get();
            boolean importOk = importProcessor.handleImport(importJPA);

            DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter();
            DateTimeFormatter hourFormatter = new DateTimeFormatterBuilder().appendPattern("HH:mm:ss").toFormatter();

            if (log.isInfoEnabled()) {
                log.info("[{}] Import traité : {}/{}", importJPA.getId(), importOk ? 1 : 0, 1);
            }
            long finish = System.nanoTime();
            long timeElapsed = finish - start;
            if (log.isInfoEnabled()) {
                log.info("[{}] Import terminé en {} ms", importJPA.getId(), timeElapsed / 1_000_000);
            }

            sendImportMail(importOk, importJPA, dateFormatter, hourFormatter);

            try {
                elasticService.updateSpecimenFromImport(importJPA.getId(), importJPA.getInstitutionId());
            } catch (Exception e) {
                log.error("Erreurs lors de la mise à jour de l'index ES", e);
            }
        }
    }

    private void sendImportMail(boolean importOk, ImportJPA importJPA, DateTimeFormatter dateFormatter, DateTimeFormatter hourFormatter) {
        // TODO prévoir l'anglais
        String subject = importOk ? "Import exécuté avec succès" : "Erreur lors de l’exécution de l’import";

        String content = importOk ? """
                Bonjour,
                <br><br>
                Nous vous confirmons que votre demande d'import effectuée le %s à %s a été réalisée avec succès.
                <br><br>
                Récapitulatif de l'import :
                <br><br>
                Nombre de spécimens ajoutés : %d
                <br>
                Nombre de spécimens modifiés : %d
                <br>
                Nombre de déterminations ajoutées : %d
                <br>
                Nombre de publications ajoutées : %d
                <br><br>
                Vous pouvez visualiser le détail de l’import ainsi que la liste des spécimens ajoutés ou modifiés en vous connectant à votre espace Recolnat, dans la section <b>Mon institution</b>, onglet <b>Imports</b>.
                <br><br>
                Cordialement,
                <br><br>
                L'équipe Recolnat.
                """.formatted(importJPA.getTimestamp().format(dateFormatter), importJPA.getTimestamp()
                .format(hourFormatter), importJPA.getAddedSpecimenCount(), importJPA.getUpdatedSpecimenCount(), importJPA.getAddedIdentificationCount(), importJPA.getAddedLiteratureCount())
                : """
                Bonjour,
                <br><br>
                Une erreur s’est produite lors de l’exécution de l’import effectué le %s à %s.
                <br><br>
                Vous pouvez contacter l’administrateur de Recolnat pour plus d’information.
                <br><br>
                Cordialement,
                <br><br>
                L'équipe Recolnat.
                """.formatted(importJPA.getTimestamp().format(dateFormatter), importJPA.getTimestamp().format(hourFormatter));

        Mail mail = Mail.builder().id(UUID.randomUUID())
                .from("noreply@recolnat.fr")
                .to(importJPA.getEmail())
                .subject(subject)
                .content(content)
                .createdAt(LocalDateTime.now())
                .state(MailStatusEnum.PENDING)
                .build();
        mailService.create(mail);
    }

    @Override
    public void validate(UUID institutionId, MultipartFile specimenFile, MultipartFile determinationFile, MultipartFile publicationFile, String importMode) {
        // Vérification que l'utilisateur courant a bien les droits de faire un import sur l'institution
        if (institutionService.checkAccessToInstitution(institutionId)) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ACCESS_DENIED);
        }

        var currentUser = authenticationService.getConnected();

        if (Arrays.stream(ImportModeEnum.values()).noneMatch(v -> v.name().equals(importMode.toUpperCase(Locale.ROOT)))) {
            throw new CollectionManagerBusinessException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), "Wrong import type");
        }

        var files = new ArrayList<ImportFileJPA>();
        var multipartFiles = new ArrayList<MultipartFile>();

        if (specimenFile != null) {
            var lines = importHelper.extractDataWithOpenCsv(specimenFile);
            files.add(ImportFileJPA.builder()
                    .fileName(specimenFile.getOriginalFilename())
                    .mode(ImportModeEnum.valueOf(importMode))
                    .fileType(ImportFileType.SPECIMEN)
                    .lineCount(lines.size() - 1L)
                    .build());
            multipartFiles.add(specimenFile);
        }
        if (determinationFile != null) {
            var lines = importHelper.extractDataWithOpenCsv(determinationFile);
            files.add(ImportFileJPA.builder()
                    .fileName(determinationFile.getOriginalFilename())
                    .fileType(ImportFileType.IDENTIFICATION)
                    .lineCount(lines.size() - 1L)
                    .build());
            multipartFiles.add(determinationFile);
        }
        if (publicationFile != null) {
            var lines = importHelper.extractDataWithOpenCsv(publicationFile);
            files.add(ImportFileJPA.builder()
                    .fileName(publicationFile.getOriginalFilename())
                    .fileType(ImportFileType.LITERATURE)
                    .lineCount(lines.size() - 1L)
                    .build());
            multipartFiles.add(publicationFile);
        }

        // Enregistrement de la "demande d'import en base"
        var importId = createImport(files, currentUser.getEmail(), currentUser.getUserName(), institutionId);

        // Sauvegarde des fichiers présents sur le FS
        saveFiles(importId, multipartFiles);
    }

    @Override
    public ImportCheckDataResponseDTO checkSpecimens(MultipartFile file, UUID institutionId) {
        var response = checkFileCommonProperties(file, institutionId);

        // Vérification de la présence des colonnes obligatoires dans le fichier
        if (Boolean.TRUE.equals(response.getFormat())) {
            response.setStructureErrors(checkSpecimenFileStructure(file));
        } else {
            response.setStructureErrors(new ArrayList<>());
        }

        return response;
    }

    @Override
    public ImportCheckDataResponseDTO checkDeterminations(MultipartFile file, UUID institutionId) {
        var response = checkFileCommonProperties(file, institutionId);

        // Vérification de la présence des colonnes obligatoires dans le fichier
        if (Boolean.TRUE.equals(response.getFormat())) {
            response.setStructureErrors(checkDeterminationFileStructure(file));
        } else {
            response.setStructureErrors(new ArrayList<>());
        }

        return response;
    }

    @Override
    public ImportCheckDataResponseDTO checkPublications(MultipartFile file, UUID institutionId) {
        var response = checkFileCommonProperties(file, institutionId);

        // Vérification de la présence des colonnes obligatoires dans le fichier
        if (Boolean.TRUE.equals(response.getFormat())) {
            response.setStructureErrors(checkPublicationFileStructure(file));
        } else {
            response.setStructureErrors(new ArrayList<>());
        }

        return response;
    }

    private ImportCheckDataResponseDTO checkFileCommonProperties(MultipartFile file, UUID institutionId) {
        var response = new ImportCheckDataResponseDTO();
        // Vérification que l'utilisateur courant a bien les droits de faire un import sur l'institution
        if (institutionService.checkAccessToInstitution(institutionId)) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ACCESS_DENIED);
        }

        // Vérification du format du fichier (CSV)
        response.setFormat(Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv"));

        // Vérification de la taille du fichier (10Mo)
        response.setSize(file.getSize() < MAX_FILE_SIZE);
        return response;
    }

    @Override
    public ImportCheckResponseDTO check(UUID institutionId, MultipartFile specimenFile, MultipartFile identificationFile, MultipartFile publicationFile) {
        long start = System.nanoTime();
        var response = new ImportCheckResponseDTO();
        ImportCheckSpecimen specimensChecks = null;

        if (specimenFile != null) {
            specimensChecks = importSpecimenFileChecker.checkFileData(specimenFile, institutionId);
            response.setSpecimen(specimensChecks.getResponse());
        }

        if (identificationFile != null) {
            response.setIdentification(importIdentificationFileChecker.checkFileData(identificationFile, institutionId, specimensChecks));
        }

        if (publicationFile != null) {
            response.setPublication(importPublicationFileChecker.checkFileData(publicationFile, institutionId, specimensChecks));
        }

        long finish = System.nanoTime();
        long timeElapsed = finish - start;

        if (log.isInfoEnabled()) {
            log.info("ImportSerch::check : {} ms", timeElapsed / 1_000_000);
        }

        return response;
    }

    private UUID createImport(List<ImportFileJPA> files, String email, String userName, UUID institutionId) {
        var importJpa = ImportJPA.builder()
                .email(email)
                .files(files)
                .status(ImportStatusEnum.PENDING)
                .timestamp(LocalDateTime.now())
                .institutionId(institutionId)
                .userName(userName)
                .build();

        files.forEach(f -> f.setImportJPA(importJpa));

        var created = importJPARepository.save(importJpa);
        return created.getId();
    }

    private void saveFiles(UUID importId, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            try {
                var filename = Path.of(baseDirectory, importDirectory, importId.toString(), Objects.requireNonNull(file.getOriginalFilename()));
                FileUtil.createFile(filename, file);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Erreur lors de la sauvegarde du fichier : {}", file.getOriginalFilename());
                }
            }
        }
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

        checkRequiredColumnNames(header, errors, PUBLICATION_REQUIRED_FIELDS);
        hasAllColumnsInList(header, errors, column -> ImportPublicationColumnEnum.fromValue(column) == null);
        checkDuplicateColumns(header, errors);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        log.info("ImportSerch::checkPublicationFileStructure : {} ms", timeElapsed);

        return errors;
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

        checkRequiredColumnNames(header, errors, DETERMINATION_REQUIRED_FIELDS);
        hasAllColumnsInList(header, errors, column -> ImportIdentificationColumnEnum.fromValue(column) == null);
        checkDuplicateColumns(header, errors);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        if (log.isInfoEnabled()) {
            log.info("ImportSerch::checkDeterminationFileStructure : {} ms", timeElapsed);
        }

        return errors;
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

        checkRequiredColumnNames(header, errors, SPECIMEN_REQUIRED_FIELDS);
        hasAllColumnsInList(header, errors, column -> ImportSpecimenColumnEnum.fromValue(column) == null);
        checkDuplicateColumns(header, errors);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        if (log.isInfoEnabled()) {
            log.info("ImportSerch::checkSpecimenFileStructure : {} ms", timeElapsed);
        }

        return errors;
    }

    private void checkDuplicateColumns(String[] columns, List<ImportStructureErrorDTO> errors) {
        var duplicateColumns = new ArrayList<String>();
        Set<String> uniqueColumns = new HashSet<>();

        for (String column : columns) {
            if (uniqueColumns.contains(column)) {
                duplicateColumns.add(column);
            } else {
                uniqueColumns.add(column);
            }
        }

        if (!duplicateColumns.isEmpty()) {
            var error = new ImportStructureErrorDTO();
            error.setCode(DUPLICATE.name());
            error.setColumns(duplicateColumns);
            errors.add(error);
        }
    }

    /**
     * Vérifie que chaque colonne du fichier fait partie de la liste des colonnes autorisées
     *
     * @param columns tableau de chaines de caractères contenant le nom des colonnes du fichier
     * @param errors  liste des erreurs
     */
    private void hasAllColumnsInList(String[] columns, List<ImportStructureErrorDTO> errors, Predicate<String> predicate) {
        var incorrectColumns = new ArrayList<String>();

        for (String column : columns) {
            if (predicate.test(column)) {
                incorrectColumns.add(column);
            }
        }

        if (!incorrectColumns.isEmpty()) {
            var error = new ImportStructureErrorDTO();
            error.setCode(INCORRECT.name());
            error.setColumns(incorrectColumns);
            errors.add(error);
        }
    }


    /**
     * Vérifie la présence des colonnes obligatoires
     *
     * @param columns        tableau de chaines de caractères contenant le nom des colonnes du fichier
     * @param errors         liste des erreurs
     * @param requiredFields liste des champs requis
     */
    private void checkRequiredColumnNames(String[] columns, List<ImportStructureErrorDTO> errors, List<String> requiredFields) {
        var cols = List.of(columns);

        var nonPresentColumns = new ArrayList<String>();

        for (String required : requiredFields) {
            if (!cols.contains(required)) {
                nonPresentColumns.add(required);
            }
        }

        if (!nonPresentColumns.isEmpty()) {
            var error = new ImportStructureErrorDTO();
            error.setCode(REQUIRED.name());
            error.setColumns(nonPresentColumns);
            errors.add(error);
        }
    }

    @Override
    public ImportPageResponseDTO getAllImports(UUID institutionId, Integer page, Integer size) {
        // Vérification que l'utilisateur courant a bien les droits de faire un import sur l'institution
        if (institutionService.checkAccessToInstitution(institutionId)) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ACCESS_DENIED);
        }
        var imports = importJPARepository.findAllByInstitutionId(institutionId, PageRequest.of(page, size));

        var importCount = importJPARepository.countByInstitutionId(institutionId);

        ImportPageResponseDTO response = new ImportPageResponseDTO();
        response.setData(importMapper.toDTOs(imports.getContent()));
        response.setTotalPages(imports.getTotalPages());
        response.setNumberOfElements(importCount);

        return response;
    }

    @Override
    public FileUtil.FileResource getImportFile(UUID fileId) {
        var fileOpt = importFileJPARepository.findByIdFetchImport(fileId);
        if (fileOpt.isPresent()) {
            var file = fileOpt.get();
            var importJPA = file.getImportJPA();
            var institutionId = importJPA.getInstitutionId();
            // Vérification que l'utilisateur courant a bien les droits de faire un import sur l'institution
            if (institutionService.checkAccessToInstitution(institutionId)) {
                throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(), ACCESS_DENIED);
            }

            final var filePath = getDirectoryPath().resolve(importJPA.getId().toString()).resolve(file.getFileName());
            try {
                var inputStream = new InputStreamResource(Files.newInputStream(filePath.toFile().toPath()));
                return new FileUtil.FileResource(file.getFileName(), inputStream);
            } catch (final IOException ioe) {
                throw new CollectionManagerBusinessException(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(), "Impossible de trouver le fichier sur le chemin " + filePath);
            }
        }
        return null;
    }

    private @NotNull Path getDirectoryPath() {
        return Path.of(baseDirectory, importDirectory);
    }
}

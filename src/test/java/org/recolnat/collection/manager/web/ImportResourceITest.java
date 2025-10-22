package org.recolnat.collection.manager.web;

import io.recolnat.model.ImportCheckIdentificationResponseDTO;
import io.recolnat.model.ImportCheckPublicationResponseDTO;
import io.recolnat.model.ImportCheckResponseDTO;
import io.recolnat.model.ImportCheckSpecimenResponseDTO;
import io.recolnat.model.ImportErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.ConnectedUser;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportFileType;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportModeEnum;
import org.recolnat.collection.manager.api.domain.enums.imports.ImportStatusEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.repository.entity.ImportJPA;
import org.recolnat.collection.manager.repository.jpa.ImportJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.COLLECTION_DATE_NOT_SAME_FORMAT;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.COLLECTION_NOT_EXISTS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.DATE_FORMAT_COLLECTE_DEBUT;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.DATE_FORMAT_COLLECTE_FIN;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.DATE_FORMAT_DETERMINATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.DUPLICATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.FLOAT_FORMAT_ALTITUDE_MAX;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.FLOAT_FORMAT_ALTITUDE_MIN;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.FLOAT_FORMAT_LATITUDE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.FLOAT_FORMAT_LONGITUDE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.FLOAT_FORMAT_PROFONDEUR_MAX;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.FLOAT_FORMAT_PROFONDEUR_MIN;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.INCORRECT_COLLECTION_DATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.INCORRECT_DEPTH;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.INCORRECT_ELEVATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.NEGATIVE_DEPTH;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_CITATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED_SCIENTIFIC_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.SPECIMEN_NOT_EXISTS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.USER_RIGHTS;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.YEAR_FORMAT_ANNEE_PUBLICATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFileType.IDENTIFICATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFileType.LITERATURE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportFileType.SPECIMEN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("int")
@Slf4j
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ImportResourceITest extends AbstractResourceDBTest {

    public static final UserAttributes ADMIN = UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
            .role(RoleEnum.ADMIN.name())
            .build();
    public static final UserAttributes ADMIN_INST = UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
            .role(RoleEnum.ADMIN_INSTITUTION.name())
            .institution(1)
            .build();
    public static final UserAttributes ADMIN_COLLECTION = UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
            .role(RoleEnum.ADMIN_COLLECTION.name())
            .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331")))
            .institution(1)
            .build();
    public static final String SPECIMEN_HEADER = "NOM_COLLECTION;NUMERO_INVENTAIRE;NUMERO_SPECIMEN;NATURE_SPECIMEN;REMARQUES_SPECIMEN;COLLECTE_DEBUT_JOUR;COLLECTE_DEBUT_MOIS;COLLECTE_DEBUT_ANNEE;COLLECTE_FIN_JOUR;COLLECTE_FIN_MOIS;COLLECTE_FIN_ANNEE;NOM_COLLECTEUR;NUMERO_COLLECTE;NOTES_TERRAIN;REMARQUES_COLLECTE;LOCALISATION_VERBATIM;LOCALISATION_SENSIBLE;LATITUDE;LONGITUDE;SYSTEME_GEODESIQUE;ALTITUDE_MIN;ALTITUDE_MAX;PROFONDEUR_MIN;PROFONDEUR_MAX;LOCALITE;COMMUNE;DEPARTEMENT;REGION;ETAT_PROVINCE;PAYS;CODE_ISO;CONTINENT;TYPE;AUTEUR_DETERMINATION;DETERMINATION_ANNEE;DETERMINATION_MOIS;DETERMINATION_JOUR;NOM_SCIENTIFIQUE;AUTEURS_TAXON;NOM_VERNACULAIRE;FAMILLE;SOUS_FAMILLE;GENRE;SOUS_GENRE;EPITHETE_SPECIFIQUE;EPITHETE_INFRA_SPECIFIQUE;REGNE;EMBRANCHEMENT;ORDRE";
    public static final String SPECIMEN_TEST_LINE = "\nUCB Lyon 1;%s;;PreservedSpecimen;;;;;;;;;;;;;false;45.808408;5.790681;WGS84;;;;;;Chanaz;Savoie;;Auvergne-Rhône-Alpes;france;FR;Europe;Figuré;;;;;Hecticoceras zieteni;Tsytovitch, 1911;;Oppeliidae;;Hecticoceras;;zieteni;;Animalia;Mollusca;Ammonitida";

    public static final String IDENTIFICATION_HEADER = "NOM_COLLECTION;NUMERO_INVENTAIRE;VERBATIM_DETERMINATION;DOUTE_DETERMINATION;TYPE;AUTEUR_DETERMINATION;DETERMINATION_ANNEE;DETERMINATION_MOIS;DETERMINATION_JOUR;NOM_SCIENTIFIQUE;AUTEURS_TAXON;NOM_VERNACULAIRE;FAMILLE;SOUS_FAMILLE;GENRE;SOUS_GENRE;EPITHETE_SPECIFIQUE;EPITHETE_INFRA_SPECIFIQUE;REGNE;EMBRANCHEMENT;ORDRE";
    public static final String IDENTIFICATION_TEST_LINE = "\nUCB Lyon 1;%s;Hecticoceras zieteni;false;Holotype;Jacques Franklin;2020;;;Hecticoceras zieteni;Tsytovitch, 1911;Lorem;Oppeliidae;;Hecticoceras;;zieteni;;Animalia;Mollusca;Ammonitida";

    public static final String PUBLICATION_HEADER = "NOM_COLLECTION;NUMERO_INVENTAIRE;DOI;URL;CITATION;TITRE_PUBLICATION;AUTEURS_PUBLICATION;ANNEE_PUBLICATION;LANGUE_PUBLICATION;MOTS_CLE;DESCRIPTION_PUBLICATION;REMARQUES_PUBLICATION;TITRE_REVUE;VOLUME_REVUE;NUMERO_REVUE;PAGES_CITATION;TITRE_OUVRAGE;MAISON_EDITION_OUVRAGE;LIEU_EDITION_OUVRAGE;EDITEURS_OUVRAGE;NB_PAGES_OUVRAGE";
    public static final String PUBLICATION_TEST_LINE = "\nUCB Lyon 1;%s;10.1127/pala/2019/0083 ;https://digitalcommons.unl.edu/insectamundi/1570/;Lingafelter SW, Woodley NE. 2024. New species, new combinations, synonymies, and nomenclatural discussion for Hispaniolan longhorned beetles (Coleoptera: Disteniidae, Cerambycidae). Insecta Mundi 1069: 1–41.;New Species, New Combinations, Synonymies, and Nomenclatural Discussion for Hispaniolan Longhorned Beetles (Coleoptera: Disteniidae, Cerambycidae);Lingafelter, S. W. & Woodley, N. E;1069;fr;botanique;Plantarum sinensium ecloge tertia;p. 258, pl. 79, fig. 6;Insecta Mundi;1;2;1-41;Titre ouvrage;maison edition;lieu edition;editeurs;234";
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    @Autowired
    private ImportJPARepository importJPARepository;

    public static ConnectedUser getConnectedUser() {
        return ConnectedUser.builder()
                .userId(UUID.fromString("a416ce96-bc14-48bc-850e-80aa95ca7221"))
                .email("admin@test.fr")
                .userName("admin").build();
    }

    // region check

    private static @NotNull ByteArrayInputStream generateSpecimenFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(SPECIMEN_HEADER);
        for (int i = 0; i < 100_000; i++) {
            sb.append(SPECIMEN_TEST_LINE.formatted(i));
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    private static @NotNull ByteArrayInputStream generateIndentificationFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(IDENTIFICATION_HEADER);
        for (int i = 0; i < 100_000; i++) {
            sb.append(IDENTIFICATION_TEST_LINE.formatted(i));
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    private static @NotNull ByteArrayInputStream generatePublicationFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(PUBLICATION_HEADER);
        for (int i = 0; i < 100_000; i++) {
            sb.append(PUBLICATION_TEST_LINE.formatted(i));
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    // region specimen

    @Disabled("A lancer pour des benchs pas en CI")
    @Test
    void check_100000() throws Exception {
        try (var specimenInputStream = generateSpecimenFile(); var identificationInputStream = generateIndentificationFile(); var publicationInputStream = generatePublicationFile()) {
            ImportCheckResponseDTO dto = testCheck(new CheckPayloadInputStream(specimenInputStream, identificationInputStream, publicationInputStream));

            ImportCheckSpecimenResponseDTO specimen = dto.getSpecimen();
            assertThat(specimen.getBlockingErrors()).isEmpty();
            assertThat(specimen.getLines()).isEqualTo(100_000);

            ImportCheckIdentificationResponseDTO identification = dto.getIdentification();
            assertThat(identification.getBlockingErrors()).isEmpty();
            assertThat(identification.getLines()).isEqualTo(100_000);

            ImportCheckPublicationResponseDTO publication = dto.getPublication();
            assertThat(publication.getBlockingErrors()).isEmpty();
            assertThat(publication.getLines()).isEqualTo(100_000);
        }
    }

    // endregion specimen

    // region determination

    @Test
    void validate() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(ADMIN);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        File specimenfile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "import/specimen/data/specimen_full_fields.csv");
        MockMultipartFile mockMultipartFileSpecimen =
                new MockMultipartFile("specimen", specimenfile.getName(), MULTIPART_FORM_DATA,
                        IOUtils.toByteArray(Files.newInputStream(specimenfile.toPath())));

        File identificationFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "import/identification/structure/ok.csv");
        MockMultipartFile mockMultipartFileDetermination =
                new MockMultipartFile("identification", identificationFile.getName(), MULTIPART_FORM_DATA,
                        IOUtils.toByteArray(Files.newInputStream(identificationFile.toPath())));

        File publicationFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "import/publication/data/publication_full_fields.csv");
        MockMultipartFile mockMultipartFilePublication =
                new MockMultipartFile("publication", publicationFile.getName(), MULTIPART_FORM_DATA,
                        IOUtils.toByteArray(Files.newInputStream(publicationFile.toPath())));

        var builder = multipart("/v1/import/validate");
        builder.with(request -> {
            request.setMethod("POST");
            return request;
        });

        var count = importJPARepository.count();
        assertThat(count).isZero();

        var response = mvc.perform(builder
                        .file(mockMultipartFileSpecimen)
                        .file(mockMultipartFileDetermination)
                        .file(mockMultipartFilePublication)
                        .param("institutionId", "d0ee2788-9aa0-4c5b-a596-53c8efc1a573")
                        .param("importMode", "REPLACE")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(response.getResponse().getStatus()).isEqualTo(200);

        // Vérification que les données sont créées en base
        UUID institutionId = UUID.fromString("d0ee2788-9aa0-4c5b-a596-53c8efc1a573");
        var imports = importJPARepository.findAllByInstitutionId(institutionId, PageRequest.of(0, 5));
        assertThat(imports).isNotNull();
        assertThat(imports.getContent()).hasSize(1);
        ImportJPA importJPA = imports.getContent().get(0);
        assertThat(importJPA.getStatus()).isEqualTo(ImportStatusEnum.PENDING);
        assertThat(importJPA.getEmail()).isEqualTo("admin@test.fr");
        assertThat(importJPA.getInstitutionId()).isEqualTo(institutionId);
        assertThat(importJPA.getFiles()).hasSize(3);
        assertThat(importJPA.getFiles().get(0).getFileName()).isEqualTo("specimen_full_fields.csv");
        assertThat(importJPA.getFiles().get(0).getFileType()).isEqualTo(SPECIMEN);
        assertThat(importJPA.getFiles().get(0).getMode()).isEqualTo(ImportModeEnum.REPLACE);
        assertThat(importJPA.getFiles().get(1).getFileName()).isEqualTo("ok.csv");
        assertThat(importJPA.getFiles().get(1).getFileType()).isEqualTo(IDENTIFICATION);
        assertThat(importJPA.getFiles().get(1).getMode()).isNull();
        assertThat(importJPA.getFiles().get(2).getFileName()).isEqualTo("publication_full_fields.csv");
        assertThat(importJPA.getFiles().get(2).getFileType()).isEqualTo(LITERATURE);
        assertThat(importJPA.getFiles().get(2).getMode()).isNull();
    }
    // endregion determination

    // region publication

    private ImportCheckResponseDTO testCheck(@NotNull String filePath, UserAttributes user, ImportFileType type) throws Exception {
        return switch (type) {
            case SPECIMEN -> testCheck(user, new CheckPayload(filePath, null, null));
            case IDENTIFICATION -> testCheck(user, new CheckPayload(null, filePath, null));
            case LITERATURE -> testCheck(user, new CheckPayload(null, null, filePath));
        };
    }

    // endregion publication

    private ImportCheckResponseDTO testCheck(CheckPayloadInputStream payload) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(ADMIN);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        var builder = multipart("/v1/import/check");
        builder.with(request -> {
            request.setMethod("POST");
            return request;
        });

        try (InputStream specimen = payload.specimenFile()) {
            if (specimen != null) {
                MockMultipartFile mockMultipartFile =
                        new MockMultipartFile("specimen", "specimen.csv", MULTIPART_FORM_DATA,
                                IOUtils.toByteArray(specimen));

                builder.file(mockMultipartFile);
            }
        }

        try (InputStream identification = payload.identificationFile()) {
            if (identification != null) {
                MockMultipartFile mockMultipartFile =
                        new MockMultipartFile("identification", "identification.csv", MULTIPART_FORM_DATA,
                                IOUtils.toByteArray(identification));

                builder.file(mockMultipartFile);
            }
        }

        try (InputStream publication = payload.publicationFile()) {
            if (publication != null) {
                MockMultipartFile mockMultipartFile =
                        new MockMultipartFile("publication", "publication.csv", MULTIPART_FORM_DATA,
                                IOUtils.toByteArray(publication));

                builder.file(mockMultipartFile);
            }
        }

        var response = mvc.perform(builder.param("institutionId", "d0ee2788-9aa0-4c5b-a596-53c8efc1a573")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(response.getResponse().getContentAsString(), ImportCheckResponseDTO.class);
    }

    // endregion check

    private ImportCheckResponseDTO testCheck(UserAttributes user, CheckPayload payload) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(user);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        var builder = multipart("/v1/import/check");
        builder.with(request -> {
            request.setMethod("POST");
            return request;
        });

        if (payload.specimenFilePath() != null) {
            File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + payload.specimenFilePath());
            MockMultipartFile mockMultipartFile =
                    new MockMultipartFile("specimen", file.getName(), MULTIPART_FORM_DATA,
                            IOUtils.toByteArray(Files.newInputStream(file.toPath())));

            builder.file(mockMultipartFile);
        }

        if (payload.identificationFilePath() != null) {
            File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + payload.identificationFilePath());
            MockMultipartFile mockMultipartFile =
                    new MockMultipartFile("identification", file.getName(), MULTIPART_FORM_DATA,
                            IOUtils.toByteArray(Files.newInputStream(file.toPath())));

            builder.file(mockMultipartFile);
        }

        if (payload.publicationFilePath() != null) {
            File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + payload.publicationFilePath());
            MockMultipartFile mockMultipartFile =
                    new MockMultipartFile("publication", file.getName(), MULTIPART_FORM_DATA,
                            IOUtils.toByteArray(Files.newInputStream(file.toPath())));

            builder.file(mockMultipartFile);
        }

        var response = mvc.perform(builder.param("institutionId", "d0ee2788-9aa0-4c5b-a596-53c8efc1a573")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(response.getResponse().getContentAsString(), ImportCheckResponseDTO.class);
    }

    record CheckPayload(String specimenFilePath, String identificationFilePath, String publicationFilePath) {
    }

    record CheckPayloadInputStream(InputStream specimenFile, InputStream identificationFile, InputStream publicationFile) {
    }

    @Nested
    class Specimen {
        @Test
        @DisplayName("TS08 : 2 lignes portent la même combinaison COLLECTION+NUMERO_INVENTAIRE")
        void TS08() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T08.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(DUPLICATE));
            error.setLines(List.of(3));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS09 : Une ligne non vide n’a pas de données dans la colonne COLLECTION")
        void TS09() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T09.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_COLLECTION_NAME));
            error.setLines(List.of(12));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS10 : Une ligne non vide n’a pas de données dans la colonne NUMERO_INVENTAIRE")
        void TS10() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T10.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_CATALOG_NUMBER));
            error.setLines(List.of(12));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS11 : Une ligne non vide n’a pas de données dans la colonne NOM_SCIENTIFIQUE")
        void TS11() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T11.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_SCIENTIFIC_NAME));
            error.setLines(List.of(12));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS12 : La donnée présente dans la colonne LATITUDE n’est pas au format numérique")
        void TS12() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T12.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(FLOAT_FORMAT_LATITUDE));
            error.setLines(List.of(5));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS13 : La donnée présente dans la colonne LONGITUDE n’est pas au format numérique")
        void TS13() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T13.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(FLOAT_FORMAT_LONGITUDE));
            error.setLines(List.of(5));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS14 : La donnée présente dans la colonne ALTITUDE_MIN n’est pas au format numérique")
        void TS14() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T14.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(FLOAT_FORMAT_ALTITUDE_MIN));
            error.setLines(List.of(3));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS15 : La donnée présente dans la colonne ALTITUDE_MAX n’est pas au format numérique")
        void TS15() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T15.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(FLOAT_FORMAT_ALTITUDE_MAX));
            error.setLines(List.of(3));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS16 : La donnée présente dans la colonne PROFONDEUR_MIN n’est pas au format numérique")
        void TS16() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T16.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(FLOAT_FORMAT_PROFONDEUR_MIN));
            error.setLines(List.of(4));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS17 : La donnée présente dans la colonne PROFONDEUR_MAX n’est pas au format numérique")
        void TS17() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T17.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(FLOAT_FORMAT_PROFONDEUR_MAX));
            error.setLines(List.of(4));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS18 : Les données présentes dans les colonnes COLLECTE_DEBUT_JOUR, COLLECTE_DEBUT_MOIS, COLLECTE_DEBUT_ANNEE forment une date cohérente.")
        void TS18() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T18.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(DATE_FORMAT_COLLECTE_DEBUT));
            error.setLines(List.of(2));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS19 : Les données présentes dans les colonnes COLLECTE_FIN_JOUR, COLLECTE_FIN_MOIS, COLLECTE_FIN_ANNEE forment une date cohérente.")
        void TS19() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T19.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(DATE_FORMAT_COLLECTE_FIN));
            error.setLines(List.of(4));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS20 : La date de fin de collecte est ultérieure à la date de début.")
        void TS20() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T20.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(INCORRECT_COLLECTION_DATE));
            error.setLines(List.of(3, 4));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS21 : Les données présentes dans les colonnes DETERMINATION_JOUR, DETERMINATION_MOIS, DETERMINATION_ANNEE forment une date cohérente.")
        void TS21() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T21.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(DATE_FORMAT_DETERMINATION));
            error.setLines(List.of(3));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS22 : Le libellé de la colonne COLLECTION ne correspond à une aucune collection de l’institution dans la base de donnée RECOLNAT")
        void TS22() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T22.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(COLLECTION_NOT_EXISTS));
            error.setLines(List.of(5));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS23 : L’utilisateur n’est pas autorisé à modifier la collection correspondant au libellé de la colonne COLLECTION.")
        void TS23() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T23.csv", ADMIN_COLLECTION, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(USER_RIGHTS));
            error.setLines(List.of(8));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS24 : La profondeur max est supérieure à la profondeur min")
        void TS24() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T24.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(INCORRECT_DEPTH));
            error.setLines(List.of(2));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS25 : La profondeur ne peut pas être négative")
        void TS25() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T25.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(NEGATIVE_DEPTH));
            error.setLines(List.of(4, 5));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS26 : L'altitude max est supérieure à l'altitude min")
        void TS26() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T26.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(INCORRECT_ELEVATION));
            error.setLines(List.of(7));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TS27 : Les dates d'un intervalle doivent être au même format")
        void TS27() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/specimen/data/T27.csv", ADMIN, SPECIMEN);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(COLLECTION_DATE_NOT_SAME_FORMAT));
            error.setLines(List.of(7));
            assertThat(dto.getSpecimen().getBlockingErrors()).contains(error);
        }
    }

    @Nested
    class Identification {

        @Test
        @DisplayName("TD09 : Une ligne non vide n’a pas de données dans la colonne COLLECTION")
        void TD09() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/identification/data/TD09.csv", ADMIN, IDENTIFICATION);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_COLLECTION_NAME));
            error.setLines(List.of(10));
            assertThat(dto.getIdentification().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TD10 : Une ligne non vide n’a pas de données dans la colonne NUMERO_INVENTAIRE")
        void TD10() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/identification/data/TD10.csv", ADMIN, IDENTIFICATION);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_CATALOG_NUMBER));
            error.setLines(List.of(3));
            assertThat(dto.getIdentification().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TD11 : Une ligne non vide n’a pas de données dans la colonne NOM_SCIENTIFIQUE")
        void TD11() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/identification/data/TD11.csv", ADMIN, IDENTIFICATION);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_SCIENTIFIC_NAME));
            error.setLines(List.of(9));
            assertThat(dto.getIdentification().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TD21 : Les données présentes dans les colonnes DETERMINATION_JOUR, DETERMINATION_MOIS, DETERMINATION_ANNEE forment une date incohérente.")
        void TD21() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/identification/data/TD21.csv", ADMIN, IDENTIFICATION);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(DATE_FORMAT_DETERMINATION));
            error.setLines(List.of(2));
            assertThat(dto.getIdentification().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TD22 : Le libellé de la colonne COLLECTION ne correspond à une aucune collection de l’institution dans la base de donnée RECOLNAT")
        void TD22() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/identification/data/TD22.csv", ADMIN, IDENTIFICATION);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(COLLECTION_NOT_EXISTS));
            error.setLines(List.of(6));
            assertThat(dto.getIdentification().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TD23 : L’utilisateur n’est pas autorisé à modifier la collection correspondant au libellé de la colonne COLLECTION.")
        void TD23() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/identification/data/TD23.csv", ADMIN_COLLECTION, IDENTIFICATION);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(USER_RIGHTS));
            error.setLines(List.of(7));
            assertThat(dto.getIdentification().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TD28 : L’identifiant du spécimen (NOM_COLLECTION+NUMERO_INVENTAIRE) n’est pas retrouvé dans la base Recolnat OU dans le fichier spécimen.")
        void TD28() throws Exception {
            ImportCheckResponseDTO dto = testCheck(ADMIN, new CheckPayload(
                    "import/data/not_in_db.csv", "import/identification/data/TD28.csv", null));
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(SPECIMEN_NOT_EXISTS));
            error.setLines(List.of(2));
            assertThat(dto.getIdentification().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TD29 : L’identifiant du spécimen (NOM_COLLECTION+NUMERO_INVENTAIRE) n’est pas retrouvé dans la base Recolnat MAIS présent dans le fichier spécimen.")
        void TD29() throws Exception {
            ImportCheckResponseDTO dto = testCheck(ADMIN, new CheckPayload(
                    "import/data/not_in_db.csv", "import/identification/data/TD29.csv", null));
            assertThat(dto.getIdentification().getBlockingErrors()).isEmpty();
        }

        @Sql(scripts = "classpath:import/data/before_update.sql")
        @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
        @Test
        @DisplayName("TD30 : L’identifiant du spécimen (NOM_COLLECTION+NUMERO_INVENTAIRE) est présent dans la base Recolnat MAIS absent dans le fichier spécimen.")
        void TD30() throws Exception {
            ImportCheckResponseDTO dto = testCheck(ADMIN, new CheckPayload(
                    null, "import/identification/data/TD30.csv", null));
            assertThat(dto.getIdentification().getBlockingErrors()).isEmpty();
        }
    }

    @Nested
    class Publication {
        @Test
        @DisplayName("TP09 : Une ligne non vide n’a pas de données dans la colonne COLLECTION")
        void TP09() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/publication/data/TP09.csv", ADMIN, LITERATURE);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_COLLECTION_NAME));
            error.setLines(List.of(5));
            assertThat(dto.getPublication().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TP10 : Une ligne non vide n’a pas de données dans la colonne NUMERO_INVENTAIRE")
        void TP10() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/publication/data/TP10.csv", ADMIN, LITERATURE);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_CATALOG_NUMBER));
            error.setLines(List.of(8));
            assertThat(dto.getPublication().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TP11 : Une ligne non vide n’a pas de données dans la colonne CITATION")
        void TP11() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/publication/data/TP11.csv", ADMIN, LITERATURE);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(REQUIRED_CITATION));
            error.setLines(List.of(8));
            assertThat(dto.getPublication().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TP21 : La valeur présente dans la colonne ANNEE_PUBLICATION n’est pas un entier numérique de 4 caractères.")
        void TP21() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/publication/data/TP21.csv", ADMIN, LITERATURE);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(YEAR_FORMAT_ANNEE_PUBLICATION));
            error.setLines(List.of(3));
            assertThat(dto.getPublication().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TP22 : Le libellé de la colonne COLLECTION ne correspond à une aucune collection de l’institution dans la base de donnée RECOLNAT")
        void TP22() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/publication/data/TP22.csv", ADMIN, LITERATURE);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(COLLECTION_NOT_EXISTS));
            error.setLines(List.of(7));
            assertThat(dto.getPublication().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TP23 : L’utilisateur n’est pas autorisé à modifier la collection correspondant au libellé de la colonne COLLECTION.")
        void TP23() throws Exception {
            ImportCheckResponseDTO dto = testCheck("import/publication/data/TP23.csv", ADMIN_COLLECTION, LITERATURE);
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(USER_RIGHTS));
            error.setLines(List.of(7));
            assertThat(dto.getPublication().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TP28 : L’identifiant du spécimen (NOM_COLLECTION+NUMERO_INVENTAIRE) n’est pas retrouvé dans la base Recolnat OU dans le fichier spécimen.")
        void TP28() throws Exception {
            ImportCheckResponseDTO dto = testCheck(ADMIN, new CheckPayload(
                    "import/data/not_in_db.csv", null, "import/publication/data/TP28.csv"));
            ImportErrorDTO error = new ImportErrorDTO();
            error.setCode(String.valueOf(SPECIMEN_NOT_EXISTS));
            error.setLines(List.of(4));
            assertThat(dto.getPublication().getBlockingErrors()).contains(error);
        }

        @Test
        @DisplayName("TP29 : L’identifiant du spécimen (NOM_COLLECTION+NUMERO_INVENTAIRE) n’est pas retrouvé dans la base Recolnat MAIS présent dans le fichier spécimen.")
        void TP29() throws Exception {
            ImportCheckResponseDTO dto = testCheck(ADMIN, new CheckPayload(
                    "import/data/not_in_db.csv", null, "import/publication/data/TP29.csv"));
            assertThat(dto.getPublication().getBlockingErrors()).isEmpty();
        }

        @Sql(scripts = "classpath:import/data/before_update.sql")
        @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
        @Test
        @DisplayName("TP30 : L’identifiant du spécimen (NOM_COLLECTION+NUMERO_INVENTAIRE) est présent dans la base Recolnat MAIS absent dans le fichier spécimen.")
        void TP30() throws Exception {
            ImportCheckResponseDTO dto = testCheck(ADMIN, new CheckPayload(
                    null, null, "import/publication/data/TP30.csv"));
            assertThat(dto.getPublication().getBlockingErrors()).isEmpty();
        }
    }
}

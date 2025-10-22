package org.recolnat.collection.manager.web;

import io.recolnat.model.ImportCheckDataResponseDTO;
import io.recolnat.model.ImportStructureErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.ConnectedUser;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.DUPLICATE;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.INCORRECT;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportErrorEnum.REQUIRED;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.BASIS_OF_RECORD;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportSpecimenColumnEnum.SCIENTIFIC_NAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Slf4j
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ImportSpecimenResourceITest extends AbstractResourceDBTest {

    public static final UserAttributes ADMIN = UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
            .role(RoleEnum.ADMIN.name())
            .build();

    public static final String HEADER = "NOM_COLLECTION;NUMERO_INVENTAIRE;NUMERO_SPECIMEN;NATURE_SPECIMEN;REMARQUES_SPECIMEN;COLLECTE_DEBUT_JOUR;COLLECTE_DEBUT_MOIS;COLLECTE_DEBUT_ANNEE;COLLECTE_FIN_JOUR;COLLECTE_FIN_MOIS;COLLECTE_FIN_ANNEE;NOM_COLLECTEUR;NUMERO_COLLECTE;NOTES_TERRAIN;REMARQUES_COLLECTE;LOCALISATION_VERBATIM;LOCALISATION_SENSIBLE;LATITUDE;LONGITUDE;SYSTEME_GEODESIQUE;ALTITUDE_MIN;ALTITUDE_MAX;PROFONDEUR_MIN;PROFONDEUR_MAX;LOCALITE;COMMUNE;DEPARTEMENT;REGION;ETAT_PROVINCE;PAYS;CODE_ISO;CONTINENT;TYPE;AUTEUR_DETERMINATION;DETERMINATION_ANNEE;DETERMINATION_MOIS;DETERMINATION_JOUR;NOM_SCIENTIFIQUE;AUTEURS_TAXON;NOM_VERNACULAIRE;FAMILLE;SOUS_FAMILLE;GENRE;SOUS_GENRE;EPITHETE_SPECIFIQUE;EPITHETE_INFRA_SPECIFIQUE;REGNE;EMBRANCHEMENT;ORDRE";
    public static final String TEST_LINE = "\nUCB Lyon 1;%s;;PreservedSpecimen;;;;;;;;;;;;;false;45.808408;5.790681;WGS84;;;;;;Chanaz;Savoie;;Auvergne-Rhône-Alpes;france;FR;Europe;Figuré;;;;;Hecticoceras zieteni;Tsytovitch, 1911;;Oppeliidae;;Hecticoceras;;zieteni;;Animalia;Mollusca;Ammonitida";


    public static ConnectedUser getConnectedUser() {
        return ConnectedUser.builder()
                .userId(UUID.fromString("a416ce96-bc14-48bc-850e-80aa95ca7221"))
                .userName("admin").build();
    }

    @Test
    @DisplayName("TSOO : Le fichier est correctement renseigné")
    void T00() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckSpecimen("import/specimen/structure/Test_specimen_OK.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        assertThat(dto.getStructureErrors()).isEmpty();
    }

    @Test
    @DisplayName("TS01 : Taille fichier supérieur à 50 Mo")
    void T01() throws Exception {
        try (var inputStream = new ByteArrayInputStream((HEADER + TEST_LINE.formatted("UCBL-FSL 15234").repeat(210_000)).getBytes())) {
            ImportCheckDataResponseDTO dto = testCheckSpecimen(inputStream);

            assertThat(dto.getSize()).isFalse();
            assertThat(dto.getFormat()).isTrue();
            assertThat(dto.getStructureErrors()).isEmpty();
        }
    }

    @Test
    @DisplayName("TS02 : Format fichier autre que CSV")
    void T02() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckSpecimen("import/specimen/structure/T02.txt");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isFalse();
        assertThat(dto.getStructureErrors()).isEmpty();
    }

    @Test
    @DisplayName("TS03 : Un libellé de colonne ne correspond pas à un des libellés de colonne du fichier template")
    void T03() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckSpecimen("import/specimen/structure/T03.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(INCORRECT));
        error.setColumns(List.of("specimen", "stade"));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("TS04 : 2 colonnes portent le même libellé")
    void T04() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckSpecimen("import/specimen/structure/T04.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(DUPLICATE));
        error.setColumns(List.of(BASIS_OF_RECORD.getColumnName()));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("TS05 : La colonne COLLECTION est absente du fichier")
    void T05() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckSpecimen("import/specimen/structure/T05.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(REQUIRED));
        error.setColumns(List.of(COLLECTION_NAME.getColumnName()));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("TS06 : La colonne NUMERO_INVENTAIRE est absente du fichier")
    void T06() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckSpecimen("import/specimen/structure/T06.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(REQUIRED));
        error.setColumns(List.of(CATALOG_NUMBER.getColumnName()));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("TS07 : La colonne NOM_SCIENTIFIQUE est absente du fichier")
    void T07() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckSpecimen("import/specimen/structure/T07.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(REQUIRED));
        error.setColumns(List.of(SCIENTIFIC_NAME.getColumnName()));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("Le séparateur est incorrect")
    void wrongSeparator() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(ADMIN);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "import/specimen/structure/wrong-separator.csv");
        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", file.getName(), "multipart/form-data",
                        IOUtils.toByteArray(Files.newInputStream(file.toPath())));

        var builder = multipart("/v1/import/specimen/check");
        builder.with(request -> {
            request.setMethod("POST");
            return request;
        });
        mvc.perform(builder.file(mockMultipartFile).param("institutionId", "d0ee2788-9aa0-4c5b-a596-53c8efc1a573")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // Then - verify the output
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("wrong_separator"))
                .andReturn();
    }

    private ImportCheckDataResponseDTO testCheckSpecimen(InputStream inputStream) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(ADMIN);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", "T01.csv", "multipart/form-data",
                        IOUtils.toByteArray(inputStream));

        var builder = multipart("/v1/import/specimen/check");
        builder.with(request -> {
            request.setMethod("POST");
            return request;
        });
        var response = mvc.perform(builder.file(mockMultipartFile).param("institutionId", "d0ee2788-9aa0-4c5b-a596-53c8efc1a573")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // Then - verify the output
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(response.getResponse().getContentAsString(), ImportCheckDataResponseDTO.class);
    }

    private ImportCheckDataResponseDTO testCheckSpecimen(@NotNull String filePath) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(ADMIN);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);
        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", file.getName(), "multipart/form-data",
                        IOUtils.toByteArray(Files.newInputStream(file.toPath())));

        var builder = multipart("/v1/import/specimen/check");
        builder.with(request -> {
            request.setMethod("POST");
            return request;
        });
        var response = mvc.perform(builder.file(mockMultipartFile).param("institutionId", "d0ee2788-9aa0-4c5b-a596-53c8efc1a573")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // Then - verify the output
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(response.getResponse().getContentAsString(), ImportCheckDataResponseDTO.class);
    }
}

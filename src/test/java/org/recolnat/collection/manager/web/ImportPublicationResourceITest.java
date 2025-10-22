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
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportIdentificationColumnEnum.CATALOG_NUMBER;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportIdentificationColumnEnum.COLLECTION_NAME;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.CITATION;
import static org.recolnat.collection.manager.api.domain.enums.imports.ImportPublicationColumnEnum.EDITORS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Slf4j
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_import_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_import_data.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ImportPublicationResourceITest extends AbstractResourceDBTest {

    public static final UserAttributes ADMIN = UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
            .role(RoleEnum.ADMIN.name())
            .build();

    public static final String HEADER = "NOM_COLLECTION;NUMERO_INVENTAIRE;DOI;URL;CITATION;TITRE_PUBLICATION;AUTEURS_PUBLICATION;ANNEE_PUBLICATION;LANGUE_PUBLICATION;MOTS_CLE;DESCRIPTION_PUBLICATION;REMARQUES_PUBLICATION;TITRE_REVUE;VOLUME_REVUE;NUMERO_REVUE;PAGES_CITATION;TITRE_OUVRAGE;MAISON_EDITION_OUVRAGE;LIEU_EDITION_OUVRAGE;EDITEURS_OUVRAGE;NB_PAGES_OUVRAGE";
    public static final String TEST_LINE = "\nUCB Lyon 1;%s;10.1127/pala/2019/0083 ;https://digitalcommons.unl.edu/insectamundi/1570/;Lingafelter SW, Woodley NE. 2024. New species, new combinations, synonymies, and nomenclatural discussion for Hispaniolan longhorned beetles (Coleoptera: Disteniidae, Cerambycidae). Insecta Mundi 1069: 1–41.;New Species, New Combinations, Synonymies, and Nomenclatural Discussion for Hispaniolan Longhorned Beetles (Coleoptera: Disteniidae, Cerambycidae);Lingafelter, S. W. & Woodley, N. E;1069;fr;botanique;Plantarum sinensium ecloge tertia;p. 258, pl. 79, fig. 6;Insecta Mundi;1;2;1-41;Titre ouvrage;maison edition;lieu edition;editeurs;234";

    public static ConnectedUser getConnectedUser() {
        return ConnectedUser.builder()
                .userId(UUID.fromString("a416ce96-bc14-48bc-850e-80aa95ca7221"))
                .userName("admin").build();
    }

    @Test
    @DisplayName("TPOO : Le fichier est correctement renseigné")
    void TP00() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckPublication("import/publication/structure/ok.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        assertThat(dto.getStructureErrors()).isEmpty();
    }

    @Test
    @DisplayName("TP01 : Taille fichier supérieur à 50 Mo")
    void TP01() throws Exception {
        try (var inputStream = new ByteArrayInputStream((HEADER + TEST_LINE.formatted("UCBL-FSL 15234").repeat(300000)).getBytes())) {
            ImportCheckDataResponseDTO dto = testCheckPublication(inputStream);
            assertThat(dto.getSize()).isFalse();
            assertThat(dto.getFormat()).isTrue();
            assertThat(dto.getStructureErrors()).isEmpty();
        }
    }

    @Test
    @DisplayName("TP02 : Format fichier autre que CSV")
    void TP02() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckPublication("import/publication/structure/T02.txt");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isFalse();
        assertThat(dto.getStructureErrors()).isEmpty();
    }

    @Test
    @DisplayName("TP03 : Un libellé de colonne ne correspond pas à un des libellés de colonne du fichier template")
    void TP03() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckPublication("import/publication/structure/T03.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(INCORRECT));
        error.setColumns(List.of("INVENTAIRE", "LANGUE"));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("TP04 : 2 colonnes portent le même libellé")
    void TP04() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckPublication("import/publication/structure/T04.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(DUPLICATE));
        error.setColumns(List.of(EDITORS.getColumnName()));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("TP05 : La colonne COLLECTION est absente du fichier")
    void TP05() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckPublication("import/publication/structure/T05.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(REQUIRED));
        error.setColumns(List.of(COLLECTION_NAME.getColumnName()));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("TP06 : La colonne NUMERO_INVENTAIRE est absente du fichier")
    void TP06() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckPublication("import/publication/structure/T06.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(REQUIRED));
        error.setColumns(List.of(CATALOG_NUMBER.getColumnName()));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("TP07 : La colonne CITATION est absente du fichier")
    void TP07() throws Exception {
        ImportCheckDataResponseDTO dto = testCheckPublication("import/publication/structure/T07.csv");

        assertThat(dto.getSize()).isTrue();
        assertThat(dto.getFormat()).isTrue();
        ImportStructureErrorDTO error = new ImportStructureErrorDTO();
        error.setCode(String.valueOf(REQUIRED));
        error.setColumns(List.of(CITATION.getColumnName()));
        assertThat(dto.getStructureErrors()).contains(error);
    }

    @Test
    @DisplayName("Le séparateur est incorrect")
    void wrongSeparator() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(ADMIN);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "import/publication/structure/wrong-separator.csv");
        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", file.getName(), "multipart/form-data",
                        IOUtils.toByteArray(Files.newInputStream(file.toPath())));

        var builder = multipart("/v1/import/publication/check");
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

    private ImportCheckDataResponseDTO testCheckPublication(InputStream inputStream) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(ADMIN);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", "T01.csv", "multipart/form-data",
                        IOUtils.toByteArray(inputStream));

        var builder = multipart("/v1/import/publication/check");
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

    private ImportCheckDataResponseDTO testCheckPublication(@NotNull String filePath) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(ADMIN);
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + filePath);
        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("file", file.getName(), "multipart/form-data",
                        IOUtils.toByteArray(Files.newInputStream(file.toPath())));

        var builder = multipart("/v1/import/publication/check");
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
